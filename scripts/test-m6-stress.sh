#!/usr/bin/env bash
set -euo pipefail

adb_bin="${ADB:-adb}"
adb_cmd() {
  if [[ -n "${ANDROID_SERIAL:-}" ]]; then "$adb_bin" -s "$ANDROID_SERIAL" "$@";
  else "$adb_bin" "$@"; fi
}
package="io.github.toolazytoname.xiaohei"
component="$package/.MainActivity"
commands=(
  '打开相册' '打开设置' '打开WiFi' '打开蓝牙' '打开相机'
  '打开浏览器' '打开拨号盘' '导航到天安门' '把音量调大' '把音量调小'
)

adb_cmd get-state >/dev/null
adb_cmd shell dumpsys package "$package" | grep -q DEBUGGABLE || {
  printf 'refusing: stress transcript injection requires a debuggable acceptance build\n' >&2
  exit 1
}
adb_cmd shell am force-stop "$package"
camera_was_granted=0
if adb_cmd shell dumpsys package "$package" | grep -A20 'runtime permissions:' | \
    grep -q 'android.permission.CAMERA: granted=true'; then camera_was_granted=1; fi
restore_camera_permission() {
  if [[ "$camera_was_granted" -eq 0 ]]; then
    adb_cmd shell pm revoke "$package" android.permission.CAMERA >/dev/null 2>&1 || true
  fi
}
trap restore_camera_permission EXIT
adb_cmd shell pm grant "$package" android.permission.CAMERA
sleep 1
adb_cmd logcat -G 8M
adb_cmd logcat -c
expected=0
for round in $(seq 1 10); do
  for command in "${commands[@]}"; do
    adb_cmd shell am start -n "$component" --es debug_transcript "$command" >/dev/null 2>&1
    expected=$((expected + 1))
    delivered=0
    for attempt in 1 2 3 4 5; do
      delivered="$(adb_cmd logcat -d -s XiaoheiAction:I '*:S' | grep -c 'ok=true' || true)"
      [[ "$delivered" == "$expected" ]] && break
      sleep 0.2
    done
    if [[ "$delivered" != "$expected" ]]; then
      printf 'FAIL stress delivery round=%s command=%s expected=%s actual=%s\n' \
        "$round" "$command" "$expected" "$delivered" >&2
      exit 1
    fi
  done
done

action_log="$(adb_cmd logcat -d -s XiaoheiAction:I '*:S')"
successes="$(printf '%s\n' "$action_log" | grep -c 'ok=true' || true)"
failures="$(printf '%s\n' "$action_log" | grep -c 'ok=false' || true)"
fatal="$( { adb_cmd logcat -d | grep -E "FATAL EXCEPTION.*|ANR in $package|Force finishing activity.*$package" || true; } | wc -l | tr -d ' ')"
recording="$(adb_cmd shell dumpsys media.audio_flinger | grep -c 'Active Record Client' || true)"
if [[ "$successes" != 100 || "$failures" != 0 || "$fatal" != 0 || "$recording" != 0 ]]; then
  printf 'FAIL stress actions=%s failures=%s fatal=%s active_record=%s\n' \
    "$successes" "$failures" "$fatal" "$recording" >&2
  exit 1
fi
printf 'PASS m6-stress actions=100 failures=0 fatal=0 active_record=0\n'
