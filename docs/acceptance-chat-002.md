# CHAT-002 Android Keystore and token-free backup acceptance

Date: 2026-08-11

Device: OnePlus 8T (`KB2000`), Android 14

## Device flow

1. The initially disabled, empty Conversation profile was temporarily set to the permitted loopback address `http://127.0.0.1`, a placeholder model, and a random test token generated only for this run. No health check, model request, or external network call was made.
2. The visible save result reported `Conversation Token 已安全配置`. App-private inspection found only `token_iv` and `token_ciphertext` in the Conversation preference file; no plaintext field was present.
3. The normal Android share preview for `xiaohei-model-channels.v3` exposed the complete non-secret backup text. It contained ASR mode, enable flags, endpoints/models, and TTS metadata, but no token value, IV, ciphertext, alias, or `token_` field. No target application was selected.
4. The normal restore page accepted a valid, non-secret v3 backup entered through its multiline UI. It visibly reported that all three Token slots had been cleared and Conversation, TTS, and Phone Agent remained disabled without starting services. Afterwards no `secure_channel*.xml` token preference file existed.
5. The pre-test state was restored: local ASR (`asr_mode=0`), Conversation disabled with empty endpoint/model, Phone Agent disabled, System TTS selected with the device's offline `ChineseTtsTflite` default, CPU wake-word OFF, and DSP `ACTIVE(handle=4)`.

## What this proves

This is an independent physical-device save → token-free export → restore/clear cycle for the Conversation Keystore slot. It proves the UI flow neither starts a model service nor exposes the test token in the visible backup.

It does not validate a real remote model credential, external sharing recipient, uninstall/reinstall Keystore behavior, another OEM, or any model request. Those are separate environment or release gates.

No test token, screenshot, raw UI XML, backup payload, credential, APK, model, or private device content is committed.

[简体中文](acceptance-chat-002.zh-CN.md)
