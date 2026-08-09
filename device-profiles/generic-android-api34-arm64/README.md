# Generic Android 14 ARM64 profile

[简体中文](README.zh-CN.md)

Status: **base product and CPU-KWS lifecycle verified on an independent AOSP virtual device; no DSP claim**.

This profile is the portable reference target. It intentionally contains no OnePlus, Qualcomm, root, Magisk, OEM model, or private-library dependency. It is useful for checking that device-specific enhancement code never becomes a requirement of the downloadable base product.

## Validated environment

| Field | Value |
|---|---|
| Runtime | Android Emulator, clean AOSP API 34 ARM64 AVD `xiaohei-m3-api34` |
| Model | `Android SDK built for arm64` |
| Android | 14 |
| ABI | `arm64-v8a` |
| Build fingerprint | `Android/sdk_phone64_arm64/emu64a:14/UE1A.230829.036.A1/11228894:userdebug/test-keys` |
| Xiaohei candidate | `0.2.0-alpha.2`, `versionCode=3`, internal debug signature |
| Combined candidate SHA-256 | `70e23a097c2c82ba06d7c989a274b20be3f1da5f2874724e6c6f1647d99d1008` |

The candidate was built locally from pinned external ASR and KWS bundle inputs. Those binaries and model weights are not committed and are not approved for public redistribution.

## Verified behavior

- Fresh install showed the honest state `DSP: no enhanced profile installed on this device`.
- Manual, Quick Settings, Assistant, offline ASR, deterministic actions, optional notification access, confirmed drafts, and the visible Phone Agent use the generic product path. Their detailed independent-AOSP evidence is indexed by the M3–M5 acceptance records.
- CPU “Xiaohei Xiaohei” was disabled by default. A visible user action started the microphone foreground service and produced `LISTENING` plus a private ongoing notification with one Stop action.
- The visible Stop action changed CPU KWS to `OFF`, destroyed `CpuWakewordService`, and left zero active recording clients. It did not change DSP state.
- Uninstall left no package, app process, active service, notification-listener grant, accessibility grant, or recording client. System-owned dead binder history in `dumpsys` is not an active Xiaohei service and disappears with normal framework lifecycle cleanup.

The AVD was launched without host audio. This run therefore proves installability, honest capability reporting, lifecycle isolation, stop, and rollback; it does **not** prove acoustic detection, physical-device power, distance, noise, thermal behavior, or a second hardware model.

## Build and repeat

Use the pinned-input instructions in [`apps/android/xiaohei-android/README.md`](../../apps/android/xiaohei-android/README.md). A source-only build remains fully usable through explicit invocation and honestly reports that offline models are unavailable. A local model build requires separate upstream-model rights review before any public binary distribution.

For keyword regression, `scripts/generate-synthetic-kws-corpus.py` creates disposable macOS synthesized audio and `scripts/evaluate-kws-wavs.py` evaluates the pinned model without committing audio. Synthetic voices are a deterministic engineering probe, never a substitute for real speakers or physical-distance testing.

## Public claim boundary

Safe: “The base product and optional foreground CPU wake-word lifecycle are verified on a clean Android 14 ARM64 AOSP profile.”

Unsafe: “This AVD proves all Android hardware supports Xiaohei,” or “Xiaohei Xiaohei is a portable DSP wake word.”
