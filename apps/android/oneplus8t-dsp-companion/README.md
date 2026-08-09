# OnePlus 8T DSP Companion (local preflight)

This local-only, device-gated companion performs a **read-only SoundTrigger module preflight**. It does not include, load, or distribute OEM models/libraries; it never records audio or starts recognition.

Validated locally on the OnePlus 8T / LineageOS 21 profile on 2026-08-09: after installation as a removable `system_ext` Magisk module, the Companion is a unique-UID `SYSTEM` package, has only its two allowlisted SoundTrigger permissions, leaves global `hidden_api_policy` at its default value, and lists Qualcomm module 0. This is permission/lifecycle evidence only; it is not a wake-word claim.

It is a separate package with a unique UID. `MANAGE_SOUND_TRIGGER` and `CAPTURE_AUDIO_HOTWORD` must come only from a partition-matched local `privapp-permissions` allowlist. Do not treat the public build as a generic Android APK.

Build locally with `./build.sh`. See `device-profiles/oneplus8t-lineageos21/dsp-companion-contract.md`.
