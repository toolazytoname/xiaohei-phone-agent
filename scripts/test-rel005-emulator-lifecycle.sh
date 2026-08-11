#!/usr/bin/env bash
set -euo pipefail

# REL-005 generic lifecycle slice. This script deliberately refuses physical
# serials: rebooting an owner's phone is not an unattended CI operation.
serial="${1:-}"
apk="${2:-}"
mode="${3:-full}"
expected_boot_id="${4:-}"
package="io.github.toolazytoname.xiaohei"

[[ "$serial" == emulator-* ]] || { printf 'usage: %s emulator-5554 generic-debug.apk [full|pre-reboot|post-reboot expected-boot-id]\n' "$0" >&2; exit 2; }
[[ -s "$apk" ]] || { printf 'missing APK: %s\n' "$apk" >&2; exit 2; }
[[ "$mode" == full || "$mode" == pre-reboot || "$mode" == post-reboot ]] || {
  printf 'invalid mode: %s\n' "$mode" >&2; exit 2;
}

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

wait_for_boot() {
  "${adb_cmd[@]}" wait-for-device
  for attempt in $(seq 1 45); do
    if [[ "$("${adb_cmd[@]}" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == 1 ]]; then return; fi
    sleep 1
  done
  printf 'FAIL emulator boot did not complete\n' >&2; exit 1
}

if [[ "$mode" != post-reboot ]]; then
  wait_for_boot
  "${adb_cmd[@]}" install -r "$apk" >/dev/null
  "${adb_cmd[@]}" shell am start -W -n "$package/.MainActivity" >/dev/null
  "${adb_cmd[@]}" shell am force-stop "$package"
  assert_no_xiaohei force-stop
  "${adb_cmd[@]}" shell am start -W -n "$package/.MainActivity" >/dev/null
  "${adb_cmd[@]}" shell am force-stop "$package"
  assert_no_xiaohei cold-start
  before_boot_id="$("${adb_cmd[@]}" shell cat /proc/sys/kernel/random/boot_id | tr -d '\r')"
  [[ -n "$before_boot_id" ]] || { printf 'FAIL emulator boot id unavailable before reboot\n' >&2; exit 1; }
  if [[ "$mode" == pre-reboot ]]; then
    trap - EXIT
    printf 'PASS rel005-pre-reboot force_stop=clean cold_start=clean boot_id=%s\n' "$before_boot_id"
    exit 0
  fi
else
  before_boot_id="$expected_boot_id"
  [[ -n "$before_boot_id" ]] || { printf 'FAIL post-reboot needs expected boot id\n' >&2; exit 2; }
fi

# `adb reboot` can terminate the caller's host command before it observes the
# new device. Issue the reboot inside the guest; the changed boot ID below is
# the only acceptance signal, so a disconnect/error here is not a pass.
if [[ "$mode" == full ]]; then "${adb_cmd[@]}" shell svc power reboot >/dev/null 2>&1 || true; fi
for attempt in $(seq 1 45); do
  after_boot_id="$("${adb_cmd[@]}" shell cat /proc/sys/kernel/random/boot_id 2>/dev/null | tr -d '\r' || true)"
  if [[ -n "$after_boot_id" && "$after_boot_id" != "$before_boot_id" ]]; then break; fi
  sleep 1
done
after_boot_id="$("${adb_cmd[@]}" shell cat /proc/sys/kernel/random/boot_id 2>/dev/null | tr -d '\r' || true)"
[[ -n "$after_boot_id" && "$after_boot_id" != "$before_boot_id" ]] || {
  printf 'FAIL emulator reboot was not observed\n' >&2; exit 1;
}
wait_for_boot
assert_no_xiaohei reboot
"${adb_cmd[@]}" uninstall "$package" >/dev/null
trap - EXIT
printf 'PASS rel005-emulator-lifecycle force_stop=clean cold_start=clean reboot=clean uninstall=clean network_model=not_exercised\n'
