# VOICE-002 offline system-TTS adapter acceptance

Date: 2026-08-11

Device: OnePlus 8T (`KB2000`), Android 14

Xiaohei: `0.2.0-alpha.3` (`versionCode=4`)

## Device result

- Android's selected default remained the qualified offline engine `com.benjaminwan.chinesettstflite`; Microsoft online TTS was not used.
- Xiaohei's visible Conversation path initialized the real Android `TextToSpeech` adapter and reported `TTS: READY`.
- A deterministic local FAQ reply, with Conversation networking disabled, entered `TTS: SPEAKING`. This used no model or action call.
- The visible “Stop speech (keep chat)” control changed the state to `TTS: INTERRUPTED` and disabled itself. Android logged the corresponding `AudioTrack` stop after 6,976 delivered frames.
- After interruption, AudioFlinger showed no active record client, and `dumpsys power` contained zero wake-lock references for Xiaohei or the offline TTS package.
- The device's Fcitx5 input method was restored after test input.

## Race fix and deterministic gates

The first device observation exposed a stop/completion race: `TextToSpeech.stop()` could deliver a completion callback before the lifecycle entered its terminal state. The adapter now transitions atomically, invalidates the utterance ID, and only then asks Android to stop or shut down the engine. A stale completion or error callback cannot turn an interrupted, stopped, or destroyed utterance into completion/failure. The lifecycle test now covers 24 valid transitions and rejects six stale callback attempts; the full Android unit suite and signed APK build pass.

## Power and claim boundary

Offline TTS is an on-demand output path. It briefly uses the CPU only after a wake/session requests speech and releases output ownership when completed or interrupted. It neither listens for the wake phrase nor holds the CPU awake while idle. Low-power always-on wake remains the OnePlus DSP path; the experimental CPU wake path remains off.

This acceptance proves the Xiaohei adapter's real offline initialization, speech request, explicit interruption, callback rejection, and post-stop resource state. It does not claim audible stop latency of 300 ms or less, human Mandarin intelligibility/naturalness, a complete speech-to-model-to-speech turn, long-duration battery life, or a currently armed DSP session. Those remain separate `VOICE-004/010/011`, `CHAT-005/012`, and physical power gates.

No APK, model, raw log, screenshot, voice recording, credential, or private device content is committed.

[简体中文](acceptance-voice-002.zh-CN.md)
