# VOICE-011 TTS interruption boundary acceptance

Date: 2026-08-11

Device: OnePlus 8T (`KB2000`), Android 14

Xiaohei: private local-ASR debug build of `0.2.0-alpha.3` (`versionCode=4`), installed/base APK SHA-256 `37f2c1f6f5f3c9d87637dda369a7e732e01ffbb4f8fda4359be8d03ca57fbf8d`

## Automated/device boundary proved

- Conversation exposes a distinct `conversation-stop-speech` control. It interrupts the selected System-TTS adapter while retaining the in-memory chat, and Repeat is an explicit local user action rather than an auto-resume.
- Stop, clear, leave, global stop, timeout, and destroy invalidate the current utterance/queue before calling the Android TTS engine, so late completion cannot restart queued content.
- In the OnePlus generation-2 queue run, sentence 2 began at `11:54:33.221`. The user pressed Stop Speech at `11:54:33.799`; the adapter recorded `queue_cancelled reason=interrupt … dropped=3`, the second-sentence 24 kHz AudioFlinger track 405 was removed at `11:54:33.955`, and no sequences 3 or 4 were submitted. The engine-visible track release was therefore 156 ms from the queue callback and 227 ms from the user-control event.
- `VOICE-004` separately proves the app-owned local ASR and TTS cannot hold their process audio input/output leases concurrently. This is a software half-duplex boundary, not a claim that arbitrary acoustic echo is impossible.

## Honest remaining human gate

AudioFlinger timing does not establish when a person ceases to hear sound. A human must confirm Mandarin intelligibility/naturalness, an audible interruption target of 300 ms or less, and that a real wake/listen interaction does not cause an objectionable speaker-to-microphone echo loop. Until that controlled human/device run exists, voice barge-in remains out of scope and `VOICE-011` is `HUMAN`, not a product-complete claim.

No recording, screenshot, raw log, transcript, credential, APK, or model is committed.

[简体中文](acceptance-voice-011.zh-CN.md)
