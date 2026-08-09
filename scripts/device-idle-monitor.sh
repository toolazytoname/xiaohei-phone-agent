#!/system/bin/sh
set -eu

duration="${1:-28800}"
interval="${2:-300}"
mode="${3:-unspecified}"
output="${4:-/sdcard/Download/xiaohei-idle-latest.tsv}"

case "$duration:$interval" in *[!0-9:]*|'') exit 2 ;; esac
printf 'monitor started mode=%s duration=%s interval=%s\n' "$mode" "$duration" "$interval" >&2
start_wait=$(date +%s)
while dumpsys battery | grep -Eq 'USB powered: true|AC powered: true|Wireless powered: true'; do
  now=$(date +%s)
  if [ $((now - start_wait)) -ge 600 ]; then
    printf 'FAIL\tcharger_not_removed\n' > "$output"
    printf 'monitor failed: charger_not_removed\n' >&2
    exit 3
  fi
  sleep 5
done

start=$(date +%s)
printf '# xiaohei-idle.v1\tmode=%s\tduration=%s\tinterval=%s\n' "$mode" "$duration" "$interval" > "$output"
printf 'epoch\telapsed_s\tlevel\tstatus\tcurrent_ua\ttemp_deci_c\tthermal_status\tactive_record\txiaohei_wakelock\n' >> "$output"
while :; do
  now=$(date +%s)
  elapsed=$((now - start))
  battery=$(dumpsys battery)
  level=$(printf '%s\n' "$battery" | sed -n 's/.*level: //p' | head -1)
  status=$(printf '%s\n' "$battery" | sed -n 's/.*status: //p' | head -1)
  temp=$(printf '%s\n' "$battery" | sed -n 's/.*temperature: //p' | head -1)
  current=$(cat /sys/class/power_supply/battery/current_now 2>/dev/null || printf 'na')
  thermal=$(dumpsys thermalservice 2>/dev/null | sed -n 's/.*Thermal Status: //p' | head -1)
  active=$(dumpsys media.audio_flinger 2>/dev/null | grep -c 'Active Record Client' || true)
  wakelock=$(dumpsys power 2>/dev/null | grep -i 'Wake Locks:' -A80 | grep -ci xiaohei || true)
  printf '%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n' \
    "$now" "$elapsed" "${level:-na}" "${status:-na}" "$current" "${temp:-na}" \
    "${thermal:-na}" "$active" "$wakelock" >> "$output"
  [ "$elapsed" -ge "$duration" ] && break
  sleep "$interval"
done
printf '# COMPLETE\n' >> "$output"
printf 'monitor complete\n' >&2
