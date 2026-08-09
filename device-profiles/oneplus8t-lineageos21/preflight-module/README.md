# OnePlus 8T DSP preflight module

This Magisk module is intentionally limited to the DSP Companion **preflight**.

- Installs the local Companion under `system_ext/priv-app` with its own UID.
- Grants only `MANAGE_SOUND_TRIGGER` and `CAPTURE_AUDIO_HOTWORD` through a matching partition allowlist.
- Adds a single local hidden-API allowlist entry required by Android 14's SoundTrigger API.
- Contains no OEM APK, model, shared library, model provider, network access, action adapter, microphone recorder, or recognition lifecycle.

The module is removable with Magisk. A successful preflight is only permission evidence; it is not a claim that wake-word recognition is active. See `../dsp-companion-contract.md`.

The public build contains no proprietary wake model. For a local lifecycle test, extract a model from an APK you own and inject it only into the ignored build output:

```bash
./extract-private-model.sh /path/to/OVoiceManagerServiceOnePlus.apk ../../../../local/sm4_xiaobuxiaobu.uim
XIAOHEI_WAKE_MODEL=../../../../local/sm4_xiaobuxiaobu.uim ./build-module.sh
```

This enables only the explicit `load → unload` buttons. It does not start recognition.
On a LineageOS build that omits the matching Qualcomm/OEM runtime closure, also set
`XIAOHEI_OEM_LIB_ROOT` to a private local directory with the exact `system_ext/`
and `vendor/` layout expected by `build-module.sh`. These binaries must come from
firmware matching the physical device and remain outside Git.
