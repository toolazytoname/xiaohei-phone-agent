# FVC-010 Conversation private-profile isolation acceptance

[简体中文](acceptance-fvc-010.zh-CN.md) · [Free-voice delivery plan](free-voice-chat-delivery-plan.md) · [Status](../STATUS.md)

Acceptance date: 2026-08-11. The user-selected CC Switch profile was saved through Xiaohei's public Independent Model and Speech Channels screen. The URL, token, model ID, and UI XML are deliberately absent from this record and Git.

| Channel/field | Redacted result |
|---|---|
| Conversation | enabled; HTTPS endpoint and model are non-empty within bounded length; its dedicated Android Keystore slot is configured |
| Conversation TTS | `system`; the default offline Chinese engine is unchanged |
| Phone Agent | disabled; endpoint/model lengths are zero; token slot is unconfigured |
| TTS Relay | endpoint/voice lengths are zero; token slot is unconfigured |
| ASR | Existing Xiaohei offline-ASR selection is unchanged |
| Audio | Saving configuration starts neither recorder nor TTS; no active Xiaohei recorder was observed |

The client treats the endpoint as an OpenAI-compatible base and appends `/chat/completions` only at request time. This configuration made no model request, so it is not connectivity or answer-quality evidence.

## Result

`FVC-010` is complete. `FVC-020` runs only the predeclared minimal real text sequence: one model reply, one reference follow-up, a local End Chat, and cancellation behavior. An unchanged network failure is never retried for token spending.
