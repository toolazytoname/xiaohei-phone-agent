#!/usr/bin/env bash
set -euo pipefail

usage() { printf 'usage: %s --serial SERIAL --mode LABEL [--hours N] [--interval SECONDS]\n' "$0" >&2; exit 2; }
serial= mode= hours=8 interval=300
while [[ $# -gt 0 ]]; do
  case "$1" in
    --serial) serial=${2:-}; shift 2 ;;
    --mode) mode=${2:-}; shift 2 ;;
    --hours) hours=${2:-}; shift 2 ;;
    --interval) interval=${2:-}; shift 2 ;;
    *) usage ;;
  esac
done
[[ -n "$serial" && "$mode" =~ ^[a-z0-9_-]{3,40}$ && "$hours" =~ ^[0-9]+$ \
    && "$interval" =~ ^[0-9]+$ ]] || usage
duration=$((hours * 3600))
adb_cmd=(adb -s "$serial")
"${adb_cmd[@]}" get-state >/dev/null

repo_root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
remote_script=/data/local/tmp/xiaohei-idle-monitor.sh
remote_output="/sdcard/Download/xiaohei-idle-${mode}.tsv"
"${adb_cmd[@]}" push "$repo_root/scripts/device-idle-monitor.sh" "$remote_script" >/dev/null
"${adb_cmd[@]}" shell chmod 0755 "$remote_script"
"${adb_cmd[@]}" shell "nohup $remote_script $duration $interval $mode $remote_output >/data/local/tmp/xiaohei-idle-monitor.log 2>&1 </dev/null &"
sleep 1
pid=$("${adb_cmd[@]}" shell pidof sh | tr -d '\r' || true)
printf 'started device-side idle monitor mode=%s duration_s=%s interval_s=%s\n' "$mode" "$duration" "$interval"
printf 'unplug power/USB within 10 minutes; reconnect after the duration and collect %s\n' "$remote_output"
printf 'shell_processes=%s\n' "$pid"
