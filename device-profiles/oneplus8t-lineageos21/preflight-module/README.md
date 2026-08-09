# OnePlus 8T DSP preflight module

This Magisk module is intentionally limited to the DSP Companion **preflight**.

- Installs the local Companion under `system_ext/priv-app` with its own UID.
- Grants only `MANAGE_SOUND_TRIGGER` and `CAPTURE_AUDIO_HOTWORD` through a matching partition allowlist.
- Adds a single local hidden-API allowlist entry required by Android 14's SoundTrigger API.
- Contains no OEM APK, model, shared library, model provider, network access, action adapter, microphone recorder, or recognition lifecycle.

The module is removable with Magisk. A successful preflight is only permission evidence; it is not a claim that wake-word recognition is active. See `../dsp-companion-contract.md`.
