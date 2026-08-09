# M3 generic Android Alpha acceptance

Date: 2026-08-09  
Current device: OnePlus 8T / Android 14, with its device profile deliberately disabled for the generic-mode run

## Proven on device

- The source-only APK built to 41,625 bytes with SHA-256 `a7422faebf8c26cfaa2c8872ef928e69042705cf15c075c620f86db2646358dd`.
- With `io.github.toolazytoname.xiaohei.dsp` disabled, the main APK reported “enhancement profile not installed” and both DSP controls were disabled instead of failing or retrying.
- A source-only installation automatically selected the Android system recognizer. This ROM's third-party system recognizer returned a microphone-permission error; Xiaohei surfaced the failure once and returned to `ARMED` without a loop.
- The portable offline-ASR build, still with the OnePlus profile disabled, reported bundled local ASR and completed a visible speech-to-deterministic-action chain. The acoustic fixture was transcribed as “open camera” rather than the intended “open gallery”, so this run proves the generic execution path, not accuracy for that utterance.
- Re-enabling the OnePlus profile restored `DSP DETACHED` and the bundled offline-ASR state without reinstalling the main product.

## Architectural result

The main APK has no root or OnePlus runtime requirement. It probes the optional Companion at runtime. ASR selection is now real rather than cosmetic: bundled builds default to app-owned offline ASR; source-only builds default to the system recognizer; the user may change only that channel without changing Phone Agent or DSP state.

## Open M3 exit gate

This same-device profile-disabled run is not evidence for the required independent non-OnePlus device. A second physical device or Android virtual device must still receive a fresh install, permission-denial/offline tests, the core action regression, and residue check. CPU wakeword remains an explicit experimental option pending power measurements.
