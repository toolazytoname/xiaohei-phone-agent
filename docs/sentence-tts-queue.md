# Sentence TTS queue

`SentenceTtsQueue` splits a bounded text stream into sentences and makes the first sentence available immediately. Later sentences advance only after the current generation completes.

Replacing a stream or cancelling it clears queued sentences and increments the generation. Completion events from older generations are ignored, so cancelled text cannot resume or reach playback later. The queue has no Android TTS, network, audio-focus, microphone, or auto-resume capability.

VOICE-010 remains `VERIFY` until it is wired to a real TTS adapter and verified for audible first-sentence latency and cancellation on device.
