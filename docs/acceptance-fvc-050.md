# FVC-050: half-duplex multi-turn voice code-gate acceptance

[中文](acceptance-fvc-050.zh-CN.md) · [Delivery plan](free-voice-chat-delivery-plan.md) · [Executor runbook](free-voice-chat-executor-runbook.md)

Date: 2026-08-11. Scope is static/JVM half-duplex and bounded-session gates only; it excludes live speech, remote calls, and phone hearing assessment.

## Verified code contract

- Text and voice finals share one `ConversationSessionCoordinator`: six turns, 2,048 tokens, five minutes; no hidden split contexts.
- Only `WAITING_FOLLOWUP` shows Continue talking; an active request or recording disables it. TTS completion never calls `startVoiceTurn()`.
- Stop, Repeat, Clear, Continue, and End use the exact local control policy with explicit `modelCalls = 0`; unit coverage retains 23 phrases.
- A Conversation profile change clears old context before a fresh send. JVM cases cover half-duplex, timeout, turn limit, and referential context.

## Reproducible commands

```bash
python3 scripts/verify-conversation-followup.py
bash apps/android/xiaohei-android/test.sh
bash scripts/verify.sh
```

Passing status is exactly `FVC-050A = VERIFY`. `FVC-050B` still requires two preregistered referential turns, exactly two calls, and resource cleanup after Stop speech/End/model switch on the OnePlus before claiming an L2 multi-turn experience.
