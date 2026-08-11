# VOICE-005 audio-interruption evidence

Date: 2026-08-11

Device: OnePlus 8T (`KB2000`), Android 14

## Implemented policy

`AudioInterruptionPolicy` maps call, alarm, media, and Activity interruption signals to the same fail-safe result: stop input and output, release process-local audio ownership, and never auto-resume. Its deterministic matrix covers both input-active and output-active cases for all four sources.

## OnePlus Activity interruption run

The first attempt was deliberately rejected as evidence: it went to the background before the asynchronous local-ASR worker opened the recorder and produced `capture_start_cancelled before_audio_start=true`.

One changed-condition retry used the debug two-minute interruption entry and waited for a real recording start before leaving the activity:

1. `12:12:04.059` — `session_started audio_focus=exclusive local_asr=true`.
2. `12:12:05.162` — local ASR logged `capture_started source=6 maximum_ms=8000` and the UI reached speech-ready.
3. The device then went to Home; `12:12:06.114` logged `session_stopped microphone_released=true`.
4. Post-stop `dumpsys media.audio_flinger` reported `No active record clients`; no new session start was logged. The independent DSP profile remained `ACTIVE(handle=4)`.

This proves the implemented Activity/background branch stops a real app-owned local-ASR capture and does not auto-resume it. It does not substitute for a genuine call, alarm, or media-focus-loss signal, unrelated-app audio ownership, human audibility, route behavior, or long-duration power. Those conditions keep `VOICE-005` at `VERIFY`.

No recording, raw log, APK, model, credential, screenshot, or private device content is committed.

[简体中文](acceptance-voice-005.zh-CN.md)
