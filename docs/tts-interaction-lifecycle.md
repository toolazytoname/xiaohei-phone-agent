# TTS interaction lifecycle

`VOICE-003` separates system-TTS completion from interruption in a pure lifecycle: `READY → SPEAKING → WAITING_FOLLOWUP`, while a user or system interruption follows `SPEAKING → INTERRUPTED → WAITING_FOLLOWUP`. Neither path restarts audio or a microphone.

The Android adapter reports the state and stops the engine for interruption. The conversation page uses it only when its independent TTS setting is `system`, and provides a separate stop-speech button. This is runtime wiring and tested boundary evidence, not proof of audible speech, audio-focus handling, or human interruption behavior.
