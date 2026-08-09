#!/usr/bin/env bash
set -euo pipefail

[[ ${1:-} == --serial && -n ${2:-} && ${3:-} == --mode && -n ${4:-} ]] || {
  printf 'usage: %s --serial SERIAL --mode LABEL\n' "$0" >&2; exit 2; }
serial=$2
mode=$4
[[ "$mode" =~ ^[a-z0-9_-]{3,40}$ ]] || exit 2
remote="/sdcard/Download/xiaohei-idle-${mode}.tsv"
output="${XIAOHEI_IDLE_OUTPUT:-xiaohei-idle-${mode}.tsv}"
adb -s "$serial" pull "$remote" "$output" >/dev/null
grep -q '^# COMPLETE$' "$output" || { printf 'FAIL monitor incomplete: %s\n' "$output" >&2; exit 1; }
awk -F '\t' '
  NR==1 {
    for (i=1;i<=NF;i++) {
      if ($i ~ /^duration=/) {split($i,a,"="); expected=a[2]}
      if ($i ~ /^requested_at=/) {split($i,a,"="); requested=a[2]}
      if ($i ~ /^sampling_started_at=/) {split($i,a,"="); sampled=a[2]}
      if ($i ~ /^preflight_wait_s=/) {split($i,a,"="); preflight=a[2]}
    }
  }
  NR==3 {start=$2; start_level=$3}
  $1 !~ /^#/ && NR>2 {
    end=$2; end_level=$3; samples++;
    if ($8 != 0) record++;
    if ($9 != 0) wake++;
    if ($10 == "Awake") interactive++;
    if ($11 != 0) call++;
    if ($12 != 0) powered++;
  }
  END {
    if ((expected > 0 && (samples < 2 || end-start < expected)) || (expected == 0 && samples < 1)) bad=1;
    if (record || wake || interactive || call || powered) bad=1;
    printf "%s idle-monitor samples=%d elapsed_s=%d level_delta=%d active_record_samples=%d xiaohei_wakelock_samples=%d interactive_samples=%d call_samples=%d powered_samples=%d requested_at=%s sampling_started_at=%s preflight_wait_s=%s\n",
      bad ? "FAIL" : "PASS", samples, end-start, start_level-end_level, record, wake, interactive, call, powered, requested, sampled, preflight;
    exit bad
  }' "$output"
