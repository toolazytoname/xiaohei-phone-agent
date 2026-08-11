# FVC-020 minimal real text-model and offline-TTS loop

[简体中文](acceptance-fvc-020.zh-CN.md) · [Free-voice delivery plan](free-voice-chat-delivery-plan.md) · [Status](../STATUS.md)

Acceptance date: 2026-08-11. Every real request used a fixed non-private short prompt. This record deliberately omits endpoint, token, model ID, and complete UI XML.

## One failure, one changed condition, two passing turns

1. The initial profile treated an HTTPS root as the Conversation base, so the client requested root `/chat/completions` and received `PARSE_ERROR`. That fingerprint occurred once and was not retried.
2. Only the endpoint changed to the same host's `/v1` base. Token, model, TTS, and other channels were unchanged.
3. A first fixed prompt requested one Chinese sentence and received a non-empty Chinese reply. Offline system TTS moved from `SPEAKING` to `WAITING_FOLLOWUP`.
4. A second fixed reference question received “Chinese”, proving `system,user,assistant,user` history and two-turn follow-up readiness.
5. The visible End Chat control cleared memory locally with no additional model call.

## Cancellation, offline, and resource boundaries

- `bash apps/android/xiaohei-android/test.sh` passed. `BoundedConversationTransportTest` covers cancellation, timeout, truncation, 429, redirect, and bounds; `PendingConversationCallTest` covers synchronous/stale callbacks; `ConversationControlPolicyTest` covers 23 exact local controls. No redundant paid relay cancellation was made.
- Existing `CHAT-011` AOSP evidence covers disabled-channel fixed FAQ and fail-closed unknown input; this working private profile was not toggled merely to repeat that proof.
- After the two turns, system TTS reported `WAITING_FOLLOWUP`; after End Chat there was no active Xiaohei recorder. CPU KWS remained `OFF`.
- One homepage coordinate error invoked global stop and one Gallery test entry; no user data was deleted, sent, or modified. DSP was restored to `ACTIVE(handle=5)` with `captureRequested=false`.

## Result

`FVC-020` is complete and L1 text-model plus offline playback is usable. A later UI improvement will make the `/v1` base expectation explicit/normalized without repeating real requests. Next is `FVC-030`, separate conversation-ASR profiles.
