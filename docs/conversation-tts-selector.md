# Independent Conversation TTS selector

[简体中文](conversation-tts-selector.zh-CN.md) · [Execution backlog](execution-backlog.md) · [Status](../STATUS.md)

Status date: 2026-08-10. `CHAT-010` delivers a separate output-channel selector and safe configuration boundary for Conversation speech. It does not claim that relay audio playback or human speech quality has passed.

## Three explicit states

| Selection | Stored meaning | Selection-time effect |
|---|---|---|
| Off | Do not select a speech adapter | No engine, network, model, or action service starts |
| System TTS | Select Android's registered system TTS for future Conversation playback | Selection only; it does not initialize or speak |
| Relay TTS | Select a separately configured HTTPS/loopback relay, voice ID, and Keystore token | Selection only; it does not call the relay |

The TTS provider, relay endpoint, voice ID, and relay-token slot are independent of both the Conversation model profile and the Phone Agent profile. Changing TTS cannot change either profile's enabled state, endpoint, model ID, or token slot.

## Safety and portability

- Relay endpoints accept HTTPS, plus HTTP only on `localhost`, `127.0.0.1`, or `::1`.
- The TTS relay token has its own Android Keystore alias. Conversation and Phone Agent credentials are not reused.
- Non-secret backup format v3 carries the provider, endpoint, and voice ID, never a token. Restore remains compatible with v2 and forces Conversation, TTS, and Phone Agent off while clearing all three token slots.
- Choosing or saving a TTS state does not initialize Android TTS, make a relay request, call a model, or start an action service.

## Verified scope

Pure-Java regression tests cover all three providers, endpoint bounds, and unchanged Conversation/Phone Agent fingerprints. Static enforcement checks key ownership, the dedicated Keystore slot, token-free backup, stable UI identifiers, and zero selection-time side effects. The APK builds with v2/v3 signatures.

On a fresh Android 14 AOSP emulator, the normal onboarding and main-screen entry configured non-empty Conversation and Phone Agent profiles plus Relay TTS. After saving Relay and then changing only TTS to System, all six model-channel fields remained byte-for-byte equivalent while only `tts_provider` changed. Service and log inspection found zero TTS initialization, zero Xiaohei speech/service start, and zero fatal/ANR. No screenshot, real model, speech, network relay, or physical OnePlus was used; the test APK, data, and UI XML were removed afterward.

Actual system-engine speech depends on a registered device engine. Relay transport/playback is not implemented by this selector task. Human intelligibility, interruption behavior, microphone integration, and a complete speech → model → TTS turn remain under `VOICE-001`, `CHAT-005`, and `CHAT-012`.
