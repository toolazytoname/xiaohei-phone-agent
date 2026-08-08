# OnePlus 8T DSP Companion contract / DSP 伴随组件契约

This profile needs two separately installable Android packages.

```text
Xiaohei App (ordinary app, no DSP permission)
       │ explicit bound service, package signature permission
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

## Gates before enabling real arm

- `ro.product.device=OnePlus8T` and a reviewed ROM fingerprint allowlist.
- Qualcomm SoundTrigger module 0 reports the expected vendor implementation.
- Every local input hash matches the reviewed profile.
- The companion receives only its two SoundTrigger permissions through a partition-matched privapp allowlist.
- Three cold boots and three arm/disarm cycles pass before any acoustic test.

This contract deliberately extracts the useful part of the OEM design—model lifecycle and re-arm—from its obsolete system UID, daemon, cross-user, and Oplus service dependencies.
