# CHAT-012 Conversation acceptance

[简体中文](conversation-acceptance-chat-012.zh-CN.md) · [Execution backlog](execution-backlog.md) · [Status](../STATUS.md)

Status date: 2026-08-10. This page separates the automated exact-candidate evidence from the remaining human speech/TTS gate. Automated success cannot mark `CHAT-012` complete by itself.

## Automated matrix

The pure-Java matrix runs these exact groups without a real model or user data:

| Group | Count | Acceptance |
|---|---:|---|
| Ordinary questions | 20 | Four fresh five-turn sessions accept each question, build the versioned prompt envelope, and complete within turn/token/time limits |
| Interruptions | 5 | Lock, background, profile switch, cancel, and Stop terminate or cancel the intended state without a model call from the control |
| Timeouts | 5 | Before-next-turn, late reply, scheduled expiry, monotonic-clock rollback, and direct session deadline all fail closed and clear session-owned context |
| Privacy denials | 5 | Notifications, contacts, live location, private media/files, and credentials are denied locally before session/model creation |

The privacy policy accepts only 15 exact Chinese/English phrases across those five categories. Every denial is labeled `LOCAL PRIVACY DENIAL | ZERO MODEL CALLS`, reports constant zero model/action calls, and directs the user toward a future scoped/visible capability when appropriate. Conceptual privacy questions and combined text are not over-broadly denied; they continue through normal Conversation handling, which still has zero action authority.

Static enforcement verifies the privacy check precedes `coordinator.begin` and `ConversationClient.ask`, the Conversation path imports no recorder API, the acceptance counts cannot silently shrink, and the human gate remains in `STATUS.md`.

## Device evidence and remaining gate

The exact debug APK passed a fresh Android 14 AOSP path through normal onboarding, main screen, and Conversation. `read my contacts` was denied locally before any remote configuration or request; the page showed both local/zero-call labels, the foreground remained `ConversationActivity`, logs contained zero fatal/ANR, and `dumpsys media.audio_flinger` reported zero active record clients. The APK, data, and UI XML were removed afterward. No screenshot, real model, speech, network mock, or physical OnePlus was used. This proves that the text Conversation candidate did not leave a recorder running; it does not prove microphone/ASR/TTS quality.

`CHAT-012` is therefore `VERIFY`, not `DONE`. It remains there until a human, on the exact candidate that actually implements the speech loop, confirms Mandarin intelligibility, natural interruption, and zero recorder residue. Relay playback is currently not implemented, so that human gate cannot honestly run yet. The automated matrix is reusable when that candidate exists.
