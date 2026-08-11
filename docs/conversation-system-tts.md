# Conversation system-TTS integration

[简体中文](conversation-system-tts.zh-CN.md)

When the independent Conversation TTS channel is explicitly set to `system`, accepted assistant replies and the local **Repeat** action are sent to `SystemTtsAdapter`. The text chat remains half-duplex and has no action authority.

The conversation page exposes **Stop speech (keep chat)**. It interrupts the active system-TTS stream without clearing the displayed conversation. Chat stop, clear, leaving the page, lock/background cleanup, and Activity destruction also interrupt and cancel queued sentences. None of these paths auto-resume audio.

`off` and `relay` do not instantiate the system adapter. A missing or failed Android TTS engine is surfaced as a local status and does not affect the model request or phone actions.

## Evidence boundary

This is an Android runtime wiring change, but it is not evidence that a particular device has an installed engine or that its audible stop latency is below 300 ms. `VOICE-011` records the OnePlus engine-visible stop boundary separately, while audible latency remains a `HUMAN` gate. Voice barge-in stays out of scope until echo-loop qualification.
