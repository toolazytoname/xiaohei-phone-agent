# REL-005 generic lifecycle recovery slice

[简体中文](acceptance-rel-005.zh-CN.md) · [Execution backlog](execution-backlog.md) · [Evidence matrix](delivery-evidence-matrix.md)

## Scope

This acceptance is deliberately limited to the generic, no-model APK on an independent Android 14 ARM64 AOSP emulator. It verifies that app process lifecycle events do not leave Xiaohei process, Android service, or AudioFlinger residue. It is not a weak-network, real-relay/model-timeout, OnePlus DSP, or physical-device qualification.

## Reproducible procedure

1. Build a source-only generic debug APK; do not bundle ASR/KWS models or credentials.
2. Start a disposable Android 14 ARM64 AOSP emulator.
3. Run `scripts/test-rel005-emulator-lifecycle.sh emulator-5554 path/to/xiaohei-debug.apk`.

The harness rejects every serial except `emulator-*`, installs the exact supplied APK, launches then force-stops it, cold-launches and force-stops it again, reboots the emulator, checks `sys.boot_completed`, and after every terminal event checks for a Xiaohei PID, `ServiceRecord`, or AudioFlinger package residue. It uninstalls the package on success and via its exit trap on failure.

## Required result

The sole passing result is:

```text
PASS rel005-emulator-lifecycle force_stop=clean cold_start=clean reboot=clean uninstall=clean network_model=not_exercised
```

This result has not yet been produced in the current delivery record, so `REL-005` remains `BACKLOG`. When it is produced, it advances only the generic lifecycle slice to `VERIFY`. A real relay/model request, a deliberately degraded network, device process-kill behavior, OnePlus DSP re-arm, and user-visible recovery still need separate, non-synthetic evidence. No automatic retry is allowed after an unchanged network/model failure.
