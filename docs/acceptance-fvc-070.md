# FVC-070: interruption and audio-resource code-gate acceptance

[中文](acceptance-fvc-070.zh-CN.md) · [Delivery plan](free-voice-chat-delivery-plan.md) · [Executor runbook](free-voice-chat-executor-runbook.md)

Date: 2026-08-11. Scope is source/JVM/static gates only and does not replace real calls, alarms, media, or Bluetooth-route tests.

The private OnePlus candidate was upgraded on 2026-08-11 to `0.2.0-alpha.5-private (6)` through a successful Android incremental install. This proves only that the matching package can upgrade; no model or voice call was started during this install and it does not prove interruption behavior.

## Verified code contract

- On user stop, failure, destroy, and system audio-focus loss, TTS cancels its queue, releases output lease, and abandons audio focus; it never resumes speech or recording automatically.
- On exclusive-focus loss, `VoiceCommandSession` stops the recognizer, abandons focus, releases input lease, and reports only a safe error.
- On screen-off, Conversation immediately stops active ASR before clearing in-memory session and pending request; `onStop`, `onDestroy`, and global stop use the same stop path.
- A new or removed audio route explicitly Stops the current turn; old recording/speech cannot continue onto a new device.

## Reproducible commands

```bash
python3 scripts/verify-tts-interaction-lifecycle.py
python3 scripts/verify-conversation-voice-turn.py
bash apps/android/xiaohei-android/test.sh
bash scripts/verify.sh
```

Passing status is exactly `FVC-070A = VERIFY`. Real calls must still be tested in LISTENING, THINKING, and SPEAKING; alarm/media focus and each supported route remain HUMAN gates and cannot be claimed from automated green checks.
