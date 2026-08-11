# VOICE-010 sentence-TTS queue acceptance

Date: 2026-08-11

Device: OnePlus 8T (`KB2000`), Android 14

Xiaohei: private local-ASR debug build of `0.2.0-alpha.3` (`versionCode=4`), installed/base APK SHA-256 `37f2c1f6f5f3c9d87637dda369a7e732e01ffbb4f8fda4359be8d03ca57fbf8d`

## Implemented boundary

- A reply is split into a generation-bound, ordered sentence queue. The first sentence is submitted immediately; the next sentence is submitted only after the current Android TTS utterance completes.
- Replacement, stop, interruption, timeout, and destroy invalidate the queue generation. Late engine callbacks are ignored rather than advancing stale text.
- Queue diagnostics are metadata-only: generation, sequence, pending count, and callback latency. They never log the spoken text.
- The queue remains inside the selected Android System-TTS adapter. It does not start a server, a network request, a microphone, or a persistent CPU wake-word service.

## Deterministic checks

`SentenceTtsQueueTest` covers first-sentence availability, ordered sequence numbers, replacement invalidation, cancellation, stale completion rejection, and no overlap. `TtsLifecycleTest` covers interruption and an explicit user Repeat recovery without auto-resume. The complete Android unit suite, static queue gate, and signed private APK build pass.

## OnePlus device evidence

The test used a fixed local FAQ, labelled in the UI as not a remote model. The Conversation profile was temporarily enabled only with `http://127.0.0.1` and a placeholder model so its deliberately failed loopback request could reach the existing fixed-FAQ fallback; no credential or external request was used. The original state was restored after the run: Conversation disabled, endpoint/model empty, local ASR selected, System-TTS selected, CPU wake-word OFF, and DSP `ACTIVE(handle=4)`.

| Run | Queue evidence | Android output evidence | Result |
|---|---|---|---|
| Natural completion, generation 1 | `11:53:35.675` created with 4 sentences; sequence 1 started at `11:53:35.687`; sequence 2 at `11:53:40.688`; sequences 3/4 followed only after their predecessors; finished with pending 0 at `11:53:49.032` | Offline ChineseTTS process PID 4089 created 24 kHz tracks: 400 at `11:53:36.012`, 401 at `11:53:41.153`, then 402/403 for later sentences | Ordered cross-sentence progression was observed on the real engine. |
| Mid-queue stop, generation 2 | Sequence 1 started at `11:54:28.184`; sequence 2 started at `11:54:33.221`, pending 3; user Stop Speech at `11:54:33.799` logged `queue_cancelled reason=interrupt … dropped=3` | Second-sentence track 405 was added at `11:54:33.728` and removed at `11:54:33.955`; no sequence 3/4 submission appeared after cancellation | The visible control reached `TTS: INTERRUPTED`; queued remaining text did not resume. |

These times also show why the acceptance wording is deliberately narrow: actual AudioFlinger output begins after the TTS engine callback, and neither this run nor automation can establish a human-perceived 300 ms target.

## Boundaries that remain open

This proves ordered real-device queue progression and cancellation of queued content. It does not prove human intelligibility, naturalness, audible interruption of 300 ms or less, speech barge-in/echo safety, Bluetooth/headset route behavior, long-duration power, or an online/relay TTS service. Those remain `VOICE-011`, `VOICE-012`, `CHAT-005/012`, and physical/human gates.

No APK, model, raw log, recording, screenshot, transcript, credential, or private device content is committed.

[简体中文](acceptance-voice-010.zh-CN.md)
