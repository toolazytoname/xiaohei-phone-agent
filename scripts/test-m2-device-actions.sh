#!/usr/bin/env bash
set -euo pipefail

adb_bin="${ADB:-adb}"
adb_cmd() {
  if [[ -n "${ANDROID_SERIAL:-}" ]]; then "$adb_bin" -s "$ANDROID_SERIAL" "$@";
  else "$adb_bin" "$@"; fi
}
package="io.github.toolazytoname.xiaohei"
component="$package/.MainActivity"

adb_cmd get-state >/dev/null
adb_cmd shell dumpsys package "$package" | grep -q 'DEBUGGABLE' || {
  printf 'refusing: the action harness only runs against a debuggable acceptance build\n' >&2
  exit 1
}

commands=(
  '打开相册' '打开设置' '打开WiFi' '打开蓝牙' '打开相机'
  '打开浏览器' '打开拨号盘' '导航到天安门' '把音量调大' '把音量调小'
)

adb_cmd shell am force-stop "$package"
sleep 1
adb_cmd logcat -c
sleep 1
baseline=0
expected=0
for round in 1 2 3; do
  for command in "${commands[@]}"; do
    adb_cmd shell am start -n "$component" --es debug_transcript "$command" >/dev/null
    expected=$((expected + 1))
    delivered=0
    for attempt in 1 2 3 4 5; do
      delivered="$(adb_cmd logcat -d -s XiaoheiAction:I '*:S' | grep -c 'ok=true' || true)"
      [[ "$delivered" == "$((baseline + expected))" ]] && break
      sleep 0.2
    done
    if [[ "$delivered" != "$((baseline + expected))" ]]; then
      printf 'FAIL intent not delivered round=%s command=%s expected=%s actual=%s\n' \
        "$round" "$command" "$expected" "$delivered" >&2
      exit 1
    fi
  done
done

log="$(adb_cmd logcat -d -s XiaoheiAction:I '*:S')"
total="$(printf '%s\n' "$log" | grep -c 'ok=true' || true)"
actual="$((total - baseline))"
failures="$(printf '%s\n' "$log" | grep -c 'ok=false' || true)"
if [[ "$actual" != 30 || "$failures" != 0 ]]; then
  printf 'FAIL action-level regression expected=30 actual=%s failures=%s\n' "$actual" "$failures" >&2
  printf '%s\n' "$log" >&2
  exit 1
fi
printf 'PASS action-level real-device regression actions=%s failures=%s\n' "$actual" "$failures"
