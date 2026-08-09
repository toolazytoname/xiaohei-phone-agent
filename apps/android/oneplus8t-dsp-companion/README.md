# OnePlus 8T DSP Companion (local lifecycle probe)

This local-only, device-gated companion performs a SoundTrigger preflight and an explicit `attach → load → unload → detach` lifecycle probe. The public source and APK contain no OEM model or library. Private inputs can be injected only into ignored, local Magisk build output; the probe never records audio or starts recognition.

Validated locally on the OnePlus 8T / LineageOS 21 profile on 2026-08-09: after installation as a removable `system_ext` Magisk module, the Companion is a unique-UID `SYSTEM` package, has only its two allowlisted SoundTrigger permissions, leaves global `hidden_api_policy` at its default value, and lists Qualcomm module 0. With user-revocable `RECORD_AUDIO` granted and private matching H25 inputs supplied locally, a bounded model lifecycle passed: HAL load success, middleware handle 1, HAL unload status 0, then middleware `DETACH`. Recognition was never started. This is lifecycle evidence only; it is not a new acoustic wake-word claim.

It is a separate package with a unique UID. `MANAGE_SOUND_TRIGGER` and `CAPTURE_AUDIO_HOTWORD` must come only from a partition-matched local `privapp-permissions` allowlist. Android 14 additionally checks user-revocable runtime `RECORD_AUDIO` before SoundTrigger attach; this does not authorize the Companion to retain command audio or create an always-on AudioRecord path. Do not treat the public build as a generic Android APK.

Build locally with `./build.sh`. See `device-profiles/oneplus8t-lineageos21/dsp-companion-contract.md`.
