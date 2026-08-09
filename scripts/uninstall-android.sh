#!/usr/bin/env bash
set -euo pipefail

serial=
if [[ ${1:-} = --serial && $# -eq 2 ]]; then serial=$2
elif [[ $# -ne 0 ]]; then
  printf 'usage: %s [--serial SERIAL]\n' "$0" >&2
  exit 2
fi
adb_cmd=(adb)
[[ -z "$serial" ]] || adb_cmd+=( -s "$serial" )
"${adb_cmd[@]}" get-state >/dev/null

# A profile that is installed must prove DETACHED before the ordinary app is removed.
if [[ -n "$("${adb_cmd[@]}" shell pm path io.github.toolazytoname.xiaohei.dsp | tr -d '\r')" ]]; then
  stop_result=$("${adb_cmd[@]}" shell content call \
    --uri content://io.github.toolazytoname.xiaohei.dsp.stop --method disarm | tr -d '\r')
  [[ "$stop_result" = *"state=DETACHED"* ]] || {
    printf 'DSP rollback failed; refusing to uninstall: %s\n' "$stop_result" >&2
    exit 1
  }
fi
"${adb_cmd[@]}" shell cmd role remove-role-holder --user 0 \
  android.app.role.ASSISTANT io.github.toolazytoname.xiaohei 0 || true
"${adb_cmd[@]}" uninstall io.github.toolazytoname.xiaohei

holder=$("${adb_cmd[@]}" shell cmd role get-role-holders \
  android.app.role.ASSISTANT | tr -d '\r')
[[ "$holder" != io.github.toolazytoname.xiaohei ]] || {
  printf 'Assistant role residue remains\n' >&2
  exit 1
}
[[ -z "$("${adb_cmd[@]}" shell pm path io.github.toolazytoname.xiaohei | tr -d '\r')" ]] || {
  printf 'APK residue remains\n' >&2
  exit 1
}
printf 'uninstalled: Xiaohei app and Assistant role; device profile package was preserved\n'
