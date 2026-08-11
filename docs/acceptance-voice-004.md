# VOICE-004 Android audio-duplex acceptance

Date: 2026-08-11

Device: OnePlus 8T (`KB2000`), Android 14

Xiaohei: private local-ASR debug build of `0.2.0-alpha.3` (`versionCode=4`), installed/base APK SHA-256 `304aa07c08d5e127116c498e4547477425b4ddcec93b037c4b0f05b937ba7a50`

## Implemented boundary

- A process-wide coordinator issues one identity-bound audio lease. Input and output cannot coexist; duplicate acquisition and stale/foreign release are denied.
- The real system-TTS adapter owns the output lease from accepted `speak()` through final completion, error, stop, interruption, or destroy.
- The app-owned local-ASR service and system-ASR command session own an input lease for their complete recording session. The optional CPU wakeword recorder uses the same input boundary.
- The OnePlus DSP profile remains outside Android recorder ownership because its verified mode uses `captureRequested=false` and the low-power hardware path owns no app `AudioRecord`.

## Device matrix

| State | Input evidence | Output evidence | Terminal evidence |
|---|---|---|---|
| Offline TTS speaking | Record monitor had no active client | Conversation showed `TTS: SPEAKING`; AudioFlinger had one active 24 kHz TTS track owned by the offline-engine process | Visible Stop Speech produced `TTS: INTERRUPTED`; TTS active-track count and active-record-client count both became 0 |
| Offline ASR listening | `RecordActivityMonitor` showed `active? true`, package `io.github.toolazytoname.xiaohei`, source `VOICE_RECOGNITION`, mono 16 kHz PCM16, not silenced; AudioFlinger showed one active input track | Offline-engine active TTS-track count was 0 | Global stop logged `session_stopped microphone_released=true`; the record monitor became empty and TTS active-track count remained 0 |

The ASR build input was the pinned upstream sherpa-onnx 1.13.4 arm64 Chinese 14M APK with SHA-256 `7d5680a287e73c6095105ef79d0e38c070a36c78b961a7f5c2b353fc166f922d`. It was used only as a private, verified build input and is not committed or approved as a public release asset.

## Failure-driven correction

Two invalid paths were not counted as passes:

1. The selected Android system recognizer rejected the session before recording, so it could not provide input evidence. The visible configuration was changed to Xiaohei's pinned offline ASR instead of retrying the same failure.
2. Before the fix, global stop could precede a delayed local-ASR worker while that worker still reached `capture_started` about 456 ms later. Cancellation and `AudioRecord.startRecording()` now share the same lock: cancellation before start produces `capture_start_cancelled before_audio_start=true` with an empty record monitor; cancellation after start calls `AudioRecord.stop()` and the worker releases the track and lease.

## Deterministic gates and limits

The pure lease matrix passes two acquisitions, four conflicts, two stale-release rejections, and zero overlap. The full Android unit suite, static adapter-wiring gate, signed private APK build, installed/build hash match, and the device matrix above pass.

This proves process-local half-duplex ownership for Xiaohei's wired Android audio paths and post-stop resource return. It does not prove cross-process exclusion against unrelated apps, human sound quality, spoken barge-in, an audible interruption latency of 300 ms or less, Bluetooth/headset routing, long-duration power, or public model redistribution rights. Those remain separate `VOICE-007/010/011/012`, `CHAT-005/012`, release, and physical gates.

No APK, model, raw log, screenshot, recording, credential, transcript, or private device content is committed.

[简体中文](acceptance-voice-004.zh-CN.md)
