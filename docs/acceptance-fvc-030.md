# FVC-030: Open-conversation ASR profile acceptance

Date: 2026-08-11 · Scope: code and build gates, not a human accuracy claim.

- `COMMAND` and `CONVERSATION` are explicit; only `COMMAND` loads command hotwords and uses hotword decoding.
- `CONVERSATION` uses hotword-free `greedy_search`; unknown or case-mismatched profiles fail closed rather than falling back to command mode.
- The restricted `VoiceCommandSession` extra reaches the app-owned recognition service and local engine. The conversation route does not invoke command normalization or `CommandRouter`.
- Stable provider IDs are reserved: `local_command_14m`, `local_conversation_candidate`, and `android_system`; they do not download or switch automatically.
- `bash apps/android/xiaohei-android/test.sh`, `bash apps/android/xiaohei-android/build.sh`, and `git diff --check` passed.

This is a model-free development APK and must not replace the private model-bearing OnePlus build. No human open-conversation transcription accuracy claim has been made; that is an FVC-040/FVC-050 device gate.
