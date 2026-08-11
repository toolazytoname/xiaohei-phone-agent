# FVC-040: Conversation one-turn voice code-gate acceptance

[中文](acceptance-fvc-040.zh-CN.md) · [Delivery plan](free-voice-chat-delivery-plan.md) · [Executor runbook](free-voice-chat-executor-runbook.md)

Date: 2026-08-11. Scope is code/JVM/static gates only: no phone and no real relay. It does not prove human Mandarin ASR, a model reply, audible TTS, or zero platform resources on a device.

## Verified code contract

- Talk interrupts output before creating a `CONVERSATION` ASR session and rejects a new input turn while a request exists.
- A partial only updates UI; a final may send only after the one-way state transition. The recognizer stops before delivering final text; local ASR releases recorder and input lease in `finally`.
- The state machine rejects skipped listening, listening while thinking, stale partials, and automatic restart after failure. TTS completion only reaches `WAITING_FOLLOWUP`; it never auto-opens the microphone.
- `onStop`, `onDestroy`, and global stop all stop the active voice session.

## Reproducible commands

```bash
python3 scripts/verify-conversation-voice-turn.py
bash apps/android/xiaohei-android/test.sh
bash scripts/verify.sh
```

Passing status is exactly `FVC-040A = VERIFY`. `FVC-040B` must still demonstrate one non-private spoken final → exactly one model reply → offline TTS → `WAITING_FOLLOWUP`, plus one cancelled turn with zero new calls, on the OnePlus before an L2 one-turn device-loop claim.
