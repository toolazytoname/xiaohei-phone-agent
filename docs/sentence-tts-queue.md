# Sentence TTS queue

`SentenceTtsQueue` splits a bounded text stream into sentences and makes the first sentence available immediately. Later sentences advance only after the current generation completes. Queue mutation is synchronized across the UI and Android TTS callback threads, and each item carries a monotonic sequence within its generation.

Replacing a stream or cancelling it clears queued sentences and increments the generation. Completion events from older generations are ignored, so cancelled text cannot resume or reach playback later. The queue has no Android TTS, network, audio-focus, microphone, or auto-resume capability.

The system-TTS adapter uses one critical section for completion, next-sentence submission, cancellation, and destroy. A stop cannot slip between dequeue and engine submission. Metadata-only logs expose generation, sequence, pending count, engine start-callback latency, cancellation reason and dropped count; they never contain spoken text.

Device acceptance must correlate the first engine callback and real AudioFlinger output, observe at least one ordered cross-sentence advance, then stop a queue with pending content and prove that no later sequence or active track appears. Audible perception and the ≤300 ms human interruption target remain separate `VOICE-011`/human gates.
