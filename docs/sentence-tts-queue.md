# Sentence TTS queue

`SentenceTtsQueue` splits a bounded text stream into sentences and makes the first sentence available immediately. Later sentences advance only after the current generation completes.

Replacing a stream or cancelling it clears queued sentences and increments the generation. Completion events from older generations are ignored, so cancelled text cannot resume or reach playback later. The queue has no Android TTS, network, audio-focus, microphone, or auto-resume capability.

The system-TTS adapter is wired to the queue for sentence advancement and cancellation. VOICE-010 remains `VERIFY` until audible first-sentence latency and cancellation are verified on device.
