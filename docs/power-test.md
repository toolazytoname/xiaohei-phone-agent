# Physical-unplug power and idle procedure

This procedure produces device-side evidence after USB is physically removed. Android battery simulation is allowed only for harness self-test and is never acceptance evidence.

1. Put Xiaohei in the intended mode and record it: `cpu-off-dsp-off`, `cpu-off-dsp-armed`, or `cpu-kws-on-dsp-off`.
2. While USB is still connected, start the monitor. A previously verified Android TLS Wireless-debugging transport may be used as `SERIAL`; do not enable legacy TCP/5555 just for this test:

   ```sh
   bash scripts/start-idle-monitor.sh --serial SERIAL --mode cpu-off-dsp-off --hours 8
   ```

3. Physically unplug USB/power within ten minutes. Sampling begins only after Android reports AC, USB, and wireless power all false.
4. Do not use the phone during the run. Reconnect after the requested duration and collect:

   ```sh
   XIAOHEI_IDLE_OUTPUT=/safe/local/path/run.tsv \
     bash scripts/collect-idle-monitor.sh --serial SERIAL --mode cpu-off-dsp-off
   ```

5. Repeat OFF/ARMED A/B at least three times with comparable starting charge, temperature, radios, screen state, and duration. Preserve raw TSV outside the public repository if it identifies private usage timing.

The monitor samples battery level/status/current where exposed, temperature, thermal status, active recording, and Xiaohei-named wakelocks. It does not keep USB alive and needs no Mac connection after launch. Acceptance requires `# COMPLETE`, the full elapsed duration, no unintended recorder/wakelock samples, and honest reporting when the device hides current sensors.
