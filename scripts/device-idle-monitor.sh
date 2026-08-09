#!/system/bin/sh
set -eu

duration="${1:-28800}"
interval="${2:-300}"
mode="${3:-unspecified}"
output="${4:-/sdcard/Download/xiaohei-idle-latest.tsv}"

case "$duration:$interval" in *[!0-9:]*|'') exit 2 ;; esac
printf 'monitor started mode=%s duration=%s interval=%s\n' "$mode" "$duration" "$interval" >&2
start_wait=$(date +%s)
while :; do
  baseline_battery=$(dumpsys battery)
  baseline_wakefulness=$(dumpsys power 2>/dev/null | sed -n 's/.*mWakefulness=//p' | head -1)
  if dumpsys telephony.registry 2>/dev/null | grep -Eq 'mCallState=(1|2)'; then baseline_call=1; else baseline_call=0; fi
  if ! printf '%s\n' "$baseline_battery" | grep -Eq 'USB powered: true|AC powered: true|Wireless powered: true' \
      && [ "${baseline_wakefulness:-na}" != Awake ] && [ "$baseline_call" -eq 0 ]; then
    break
  fi
  now=$(date +%s)
  if [ $((now - start_wait)) -ge 600 ]; then
    printf 'FAIL\tbaseline_not_unplugged_idle\n' > "$output"
    printf 'monitor failed: baseline_not_unplugged_idle\n' >&2
    exit 3
  fi
  sleep 5
done

start=$(date +%s)
preflight_wait=$((start - start_wait))
printf '# xiaohei-idle.v2\tmode=%s\tduration=%s\tinterval=%s\trequested_at=%s\tsampling_started_at=%s\tpreflight_wait_s=%s\n' \
  "$mode" "$duration" "$interval" "$start_wait" "$start" "$preflight_wait" > "$output"
printf 'epoch\telapsed_s\tlevel\tstatus\tcurrent_ua\ttemp_deci_c\tthermal_status\tactive_record\txiaohei_wakelock\twakefulness\tcall_active\tpowered\n' >> "$output"
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
  wakefulness=$(dumpsys power 2>/dev/null | sed -n 's/.*mWakefulness=//p' | head -1)
  if dumpsys telephony.registry 2>/dev/null | grep -Eq 'mCallState=(1|2)'; then call_active=1; else call_active=0; fi
  if printf '%s\n' "$battery" | grep -Eq 'AC powered: true|USB powered: true|Wireless powered: true'; then powered=1; else powered=0; fi
  printf '%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n' \
    "$now" "$elapsed" "${level:-na}" "${status:-na}" "$current" "${temp:-na}" \
    "${thermal:-na}" "$active" "$wakelock" "${wakefulness:-na}" "$call_active" "$powered" >> "$output"
  [ "$elapsed" -ge "$duration" ] && break
  sleep "$interval"
done
printf '# COMPLETE\n' >> "$output"
printf 'monitor complete\n' >&2
