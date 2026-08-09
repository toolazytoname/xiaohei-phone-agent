# OnePlus 8T DSP Companion contract / DSP 伴随组件契约

This profile needs two separately installable Android packages.

```text
Xiaohei App (ordinary app, no DSP permission)
       │ explicit signature-protected broadcast
       ▼
OnePlus DSP Companion (local privileged profile only)
       │ SoundTrigger attach/load/start/stop
       ▼
Qualcomm module 0 + locally supplied stock SVA model
```

## Companion rules

1. It has a unique package and UID; it is never `android.uid.system` and never a persistent app.
2. It exposes only `status`, `arm`, `disarm`, and a redacted wake event. It has no Android action, shell, model-provider, network, notification-reader, accessibility, or cross-user responsibility.
3. `arm` is rejected unless the exact ROM/device gate, required local input hashes, module 0 identity, and requested phrase metadata all match.
4. A callback produces `wakeword-event.v1`; it contains no PCM, UIM bytes, package list, or user text. The companion then re-arms only after the main app acknowledges event receipt or after a bounded timeout.
5. `disarm`, uninstallation, module removal, and boot failure must unload the model and leave no audio capture or wake lock.

## Private local inputs

The local build/integration process may read the user's legally held OTA to obtain a stock `.uim` and required matching libraries. Those inputs, the generated companion APK, platform keys, device serials, and runtime logs are never committed or released from this repository.

The public module builds without those inputs and remains a preflight-only package. A private lifecycle build must inject the model and the reviewed minimum runtime closure through explicit environment variables. The 32-bit RNN plugin must retain the `vendor_file` SELinux label or the vendor HAL cannot resolve it.

## Gates before enabling real arm

- `ro.product.device=OnePlus8T` and a reviewed ROM fingerprint allowlist.
- Qualcomm SoundTrigger module 0 reports the expected vendor implementation.
- Every local input hash matches the reviewed profile.
- The companion receives only `MANAGE_SOUND_TRIGGER` and `CAPTURE_AUDIO_HOTWORD` through a partition-matched privapp allowlist. Android 14 middleware also requires user-revocable runtime `RECORD_AUDIO` before attach; the Companion does not create a normal AudioRecord stream during standby. Android Assistant session display remains the ordinary app's responsibility.
- Three cold boots and three arm/disarm cycles pass before any acoustic test.

The daily control surface is split deliberately: `DspControlService` is a signature-gated foreground owner for arm/disarm, and `DspStatusProvider` is a signature-gated read-only query. Status reads never start a background service. The debug Activity is not the production lifecycle owner.

## Current gate status (2026-08-09)

- PASS: unique UID, allowlisted permissions, module discovery, attach and detach.
- PASS: locally supplied stock model `load → unload`; HAL load succeeded, middleware returned handle 1, and HAL unload returned status 0.
- PASS: `startRecognition` entered Qualcomm LPI with `captureRequested=false`; a screen-off acoustic trigger reached the Companion callback at second-stage confidence 99.
- PASS: automatic re-arm produced a second independent callback, followed by explicit stop, unload, and detach.
- PASS: callback forwarding into the ordinary app through the selected Android `VoiceInteractionService`; Android 14 allowed the Assistant-owned visible launch while rejecting direct background Activity attempts.
- NOT RUN: cold-boot repetition and power measurements.

This contract deliberately extracts the useful part of the OEM design—model lifecycle and re-arm—from its obsolete system UID, daemon, cross-user, and Oplus service dependencies.
