#!/usr/bin/env bash
set -euo pipefail

# REL-005 generic lifecycle slice. This script deliberately refuses physical
# serials: rebooting an owner's phone is not an unattended CI operation.
serial="${1:-}"
apk="${2:-}"
package="io.github.toolazytoname.xiaohei"

[[ "$serial" == emulator-* ]] || { printf 'usage: %s emulator-5554 generic-debug.apk\n' "$0" >&2; exit 2; }
[[ -s "$apk" ]] || { printf 'missing APK: %s\n' "$apk" >&2; exit 2; }

adb_cmd=(adb -s "$serial")
cleanup() { "${adb_cmd[@]}" uninstall "$package" >/dev/null 2>&1 || true; }
trap cleanup EXIT

assert_no_xiaohei() {
  if "${adb_cmd[@]}" shell pidof "$package" 2>/dev/null | tr -d '\r' | grep -q '[0-9]'; then
    printf 'FAIL Xiaohei process remains after %s\n' "$1" >&2; exit 1
  fi
  if "${adb_cmd[@]}" shell dumpsys activity services "$package" 2>/dev/null | grep -q 'ServiceRecord'; then
    printf 'FAIL Xiaohei service remains after %s\n' "$1" >&2; exit 1
  fi
  if "${adb_cmd[@]}" shell dumpsys media.audio_flinger 2>/dev/null | grep -q "$package"; then
    printf 'FAIL Xiaohei audio client remains after %s\n' "$1" >&2; exit 1
  fi
}

"${adb_cmd[@]}" wait-for-device
"${adb_cmd[@]}" install -r "$apk" >/dev/null
"${adb_cmd[@]}" shell am start -W -n "$package/.MainActivity" >/dev/null
"${adb_cmd[@]}" shell am force-stop "$package"
assert_no_xiaohei force-stop

"${adb_cmd[@]}" shell monkey -p "$package" 1 >/dev/null
"${adb_cmd[@]}" shell am force-stop "$package"
assert_no_xiaohei cold-start

"${adb_cmd[@]}" reboot
"${adb_cmd[@]}" wait-for-device
for attempt in $(seq 1 45); do
  if [[ "$("${adb_cmd[@]}" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == 1 ]]; then break; fi
  sleep 1
done
[[ "$("${adb_cmd[@]}" shell getprop sys.boot_completed | tr -d '\r')" == 1 ]] || {
  printf 'FAIL emulator boot did not complete\n' >&2; exit 1;
}
assert_no_xiaohei reboot
"${adb_cmd[@]}" uninstall "$package" >/dev/null
trap - EXIT
printf 'PASS rel005-emulator-lifecycle force_stop=clean cold_start=clean reboot=clean uninstall=clean network_model=not_exercised\n'
