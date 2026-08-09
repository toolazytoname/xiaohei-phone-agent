# M3 generic Android Alpha acceptance

Date: 2026-08-09  
Primary device: OnePlus 8T / Android 14, with its device profile deliberately disabled for the generic-mode run
Independent acceptance device: clean Android 14 AOSP ARM64 virtual device (`emu64a` / `Android SDK built for arm64`), no OnePlus profile or root

## Proven on device

- The source-only APK built to 41,625 bytes with SHA-256 `a7422faebf8c26cfaa2c8872ef928e69042705cf15c075c620f86db2646358dd`.
- With `io.github.toolazytoname.xiaohei.dsp` disabled, the main APK reported “enhancement profile not installed” and both DSP controls were disabled instead of failing or retrying.
- A source-only installation automatically selected the Android system recognizer. This ROM's third-party system recognizer returned a microphone-permission error; Xiaohei surfaced the failure once and returned to `ARMED` without a loop.
- The portable offline-ASR build, still with the OnePlus profile disabled, reported bundled local ASR and completed a visible speech-to-deterministic-action chain. The acoustic fixture was transcribed as “open camera” rather than the intended “open gallery”, so this run proves the generic execution path, not accuracy for that utterance.
- Re-enabling the OnePlus profile restored `DSP DETACHED` and the bundled offline-ASR state without reinstalling the main product.
- The independent AOSP device received a fresh, source-only debug APK (`0.2.0-alpha.2`, 41,625 bytes, SHA-256 `c2702ca914efcc72f9c3469e12246be7ab2f7d13c8120034a6769e0a9d8ad636`). Its onboarding screen requested no permission and correctly reported the DSP profile as unavailable; neither the main package nor the DSP Companion existed before the run.
- On that AOSP device, microphone, camera, and notification permissions were all denied. Tapping the generic push-to-talk control displayed the Android microphone prompt; selecting **Don't allow** returned the product to `ERROR: microphone permission was not granted; command session stopped`, with zero active recording client.
- With Wi-Fi disabled, debug-only fixed transcript injection routed `打开相册` to `OPEN_GALLERY` (`ok=true`) and the system Gallery became the resumed activity. This deliberately proves portable deterministic routing and public-Intent execution without network; it does **not** claim emulator acoustic/ASR accuracy.
- After force-stop and uninstall, neither Xiaohei package nor DSP Companion was listed, Accessibility remained unset, and the audio service reported zero active recording client.

## Architectural result

The main APK has no root or OnePlus runtime requirement. It probes the optional Companion at runtime. ASR selection is now real rather than cosmetic: bundled builds default to app-owned offline ASR; source-only builds default to the system recognizer; the user may change only that channel without changing Phone Agent or DSP state.

## M3 exit gate

The required independent Android virtual-device fresh-install, denied-permission, offline deterministic-action, and residue checks are now complete. CPU wakeword remains an explicit experimental option pending power measurements; that M7 condition does not widen the M3 generic-base claim.
