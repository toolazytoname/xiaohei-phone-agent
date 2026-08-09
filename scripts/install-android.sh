#!/usr/bin/env bash
set -euo pipefail

usage() {
  printf 'usage: %s --apk PATH [--companion PATH] [--serial SERIAL]\n' "$0" >&2
  exit 2
}

apk= companion= serial=
while [[ $# -gt 0 ]]; do
  case "$1" in
    --apk) [[ $# -ge 2 ]] || usage; apk=$2; shift 2 ;;
    --companion) [[ $# -ge 2 ]] || usage; companion=$2; shift 2 ;;
    --serial) [[ $# -ge 2 ]] || usage; serial=$2; shift 2 ;;
    *) usage ;;
  esac
done
[[ -s "$apk" ]] || { printf 'missing APK: %s\n' "$apk" >&2; exit 1; }
adb_cmd=(adb)
[[ -z "$serial" ]] || adb_cmd+=( -s "$serial" )
"${adb_cmd[@]}" get-state >/dev/null

if [[ -n "$companion" ]]; then
  [[ -s "$companion" ]] || { printf 'missing Companion APK: %s\n' "$companion" >&2; exit 1; }
  "${adb_cmd[@]}" install -r "$companion"
fi
"${adb_cmd[@]}" install -r "$apk"
"${adb_cmd[@]}" shell pm grant io.github.toolazytoname.xiaohei android.permission.RECORD_AUDIO
"${adb_cmd[@]}" shell cmd role add-role-holder --user 0 \
  android.app.role.ASSISTANT io.github.toolazytoname.xiaohei 0
holder=$("${adb_cmd[@]}" shell cmd role get-role-holders \
  android.app.role.ASSISTANT | tr -d '\r')
[[ "$holder" = io.github.toolazytoname.xiaohei ]] || {
  printf 'Assistant role verification failed: %s\n' "$holder" >&2
  exit 1
}
printf 'installed: Xiaohei APK; assistant role verified\n'
