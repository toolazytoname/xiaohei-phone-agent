# FVC-000 redacted baseline acceptance

[简体中文](acceptance-fvc-000.zh-CN.md) · [Free-voice delivery plan](free-voice-chat-delivery-plan.md) · [Status](../STATUS.md)

Acceptance date: 2026-08-11. This is a read-only pre-implementation baseline. It wrote no Conversation configuration, made no model request, and installed no APK.

| Item | Observation |
|---|---|
| Worktree | `main` at `7715d3f`; only this plan/status documentation plus pre-existing untracked `docs/articles/`, which was not touched |
| Device | OnePlus 8T connected by USB; serial deliberately omitted |
| Xiaohei package | `0.2.0-alpha.3` / versionCode 4 / APK Signature Scheme v4; installed base APK SHA-256 `37f2c1f6f5f3c9d87637dda369a7e732e01ffbb4f8fda4359be8d03ca57fbf8d` |
| Model package provenance | This hash matches the private local-ASR debug acceptance in `acceptance-voice-010/011`; upstream ASR/KWS build inputs remain outside Git and no reusable copy was found in ordinary local download locations |
| ASR/TTS | UI reports bundled Xiaohei offline Chinese ASR; Android defaults to offline `com.benjaminwan.chinesettstflite`; Conversation TTS profile is `system` |
| Conversation | `conversation_enabled=false`; endpoint/model are empty; the Conversation Keystore token was not read |
| CPU KWS | Private state is `OFF` |
| DSP | `io.github.toolazytoname.xiaohei.dsp/.DspControlService` runs as a foreground service with the `ARM` intent. Shell lacks signature permission for its exact provider state, so this is not overstated as acoustic-callback or power evidence. |
| Audio | No active Xiaohei recorder was observed during baseline; no voice session was started. |

## Result

`FVC-000` is complete. Next is `FVC-010`: safely write the user-selected relay only through independent Conversation settings and verify channel isolation. Endpoints, tokens, model text, raw UI XML, and private build inputs remain forbidden from Git and logs.
