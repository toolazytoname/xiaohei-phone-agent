#!/usr/bin/env bash
set -euo pipefail

serial="${1:-}"
adb_cmd=(adb)
[[ -z "$serial" ]] || adb_cmd+=( -s "$serial" )
"${adb_cmd[@]}" get-state >/dev/null
"${adb_cmd[@]}" logcat -c

cases=$(mktemp /tmp/xiaohei-surfaces.XXXXXX)
trap 'rm -f "$cases"' EXIT
printf '%s\n' \
  'action|android.settings.SETTINGS|com.android.settings' \
  'action|android.settings.WIFI_SETTINGS|com.android.settings' \
  'action|android.settings.BLUETOOTH_SETTINGS|com.android.settings' \
  'action|android.settings.APPLICATION_SETTINGS|com.android.settings' \
  'action|android.settings.DISPLAY_SETTINGS|com.android.settings' \
  'action|android.settings.SOUND_SETTINGS|com.android.settings' \
  'action|android.settings.SECURITY_SETTINGS|com.android.settings,com.android.permissioncontroller' \
  'action|android.settings.ACCESSIBILITY_SETTINGS|com.android.settings' \
  'action|android.settings.NOTIFICATION_SETTINGS|com.android.settings' \
  'action|android.settings.BATTERY_SAVER_SETTINGS|com.android.settings' \
  'action|android.settings.INTERNAL_STORAGE_SETTINGS|com.android.settings' \
  'action|android.settings.LOCATION_SOURCE_SETTINGS|com.android.settings' \
  'action|android.settings.DATE_SETTINGS|com.android.settings' \
  'action|android.settings.LOCALE_SETTINGS|com.android.settings' \
  'component|com.android.calculator2/.Calculator|com.android.calculator2' \
  'component|com.android.contacts/.activities.PeopleActivity|com.android.contacts,com.android.permissioncontroller' \
  'component|com.android.deskclock/.DeskClock|com.android.deskclock,com.android.permissioncontroller' \
  'component|com.android.dialer/.main.impl.MainActivity|com.android.dialer' \
  'component|com.android.documentsui/.LauncherActivity|com.android.documentsui' \
  'component|org.lineageos.aperture/.CameraLauncher|org.lineageos.aperture' \
  'component|org.lineageos.glimpse/.MainActivity|org.lineageos.glimpse' \
  'component|org.lineageos.jelly/.MainActivity|org.lineageos.jelly' \
  'component|org.lineageos.etar/com.android.calendar.AllInOneActivity|org.lineageos.etar' \
  'component|org.lineageos.recorder/.RecorderActivity|org.lineageos.recorder' \
  'component|org.lineageos.eleven/.ui.activities.HomeActivity|org.lineageos.eleven' > "$cases"

passed=0
failed=0
while IFS='|' read -r kind target expected; do
  if [[ "$kind" == action ]]; then
    output=$("${adb_cmd[@]}" shell am start -W -a "$target" </dev/null 2>&1 || true)
  else
    output=$("${adb_cmd[@]}" shell am start -W -n "$target" </dev/null 2>&1 || true)
  fi
  sleep 0.25
  resumed=$("${adb_cmd[@]}" shell dumpsys activity activities </dev/null | grep -m1 'topResumedActivity' || true)
  # Android 14/Lineage can omit topResumedActivity while the device is sleeping
  # or the notification shade owns focus. mFocusedApp still identifies the
  # launched activity, so use it only as a read-only compatibility fallback.
  if [[ -z "$resumed" ]]; then
    resumed=$("${adb_cmd[@]}" shell dumpsys window </dev/null | grep -m1 'mFocusedApp' || true)
  fi
  matched=0
  IFS=',' read -r -a expected_packages <<< "$expected"
  for expected_package in "${expected_packages[@]}"; do
    if [[ "$resumed" == *"$expected_package"* ]]; then matched=1; break; fi
  done
  if [[ "$output" == *Error:* || "$output" == *Exception* || "$matched" -ne 1 ]]; then
    printf 'FAIL target=%s expected=%s resumed=%s\n' "$target" "$expected" "$resumed" >&2
    failed=$((failed + 1))
  else
    printf 'PASS target=%s package=%s\n' "$target" "$expected"
    passed=$((passed + 1))
  fi
done < "$cases"

fatal=$("${adb_cmd[@]}" logcat -d -v brief | grep -E 'FATAL EXCEPTION|ANR in io.github.toolazytoname.xiaohei' || true)
[[ -z "$fatal" ]] || { printf 'FAIL fatal/anr detected\n%s\n' "$fatal" >&2; exit 1; }
[[ "$failed" -eq 0 ]] || { printf 'FAIL m6-surfaces passed=%s failed=%s\n' "$passed" "$failed" >&2; exit 1; }
printf 'PASS m6-surfaces surfaces=%s failures=0 fatal=0\n' "$passed"
