# VOICE-001 offline Chinese TTS acceptance

Date: 2026-08-11

Device: OnePlus 8T (`KB2000`), Android 14

Xiaohei: `0.2.0-alpha.3` (`versionCode=4`), APK SHA-256 `e8a447b1ce459d6ec68d733507c74123b23dea25313c84fb690afe1cc6435167`

## Qualified state

- The device owner explicitly authorized installation and default-engine selection.
- `ChineseTtsTflite 0.5.0` (`versionCode=5`) was downloaded from an F-Droid mirror listed in F-Droid's signed repository index. Its installed base APK is 70,075,044 bytes and has SHA-256 `bdc8a50c028b4f0eacd2ab2f22cbbefe8ee00262b25e388cf545c54cbacbc76e`, exactly matching that signed index.
- Android registered `com.benjaminwan.chinesettstflite/.service.TtsService`, and `tts_default_synth` was `com.benjaminwan.chinesettstflite`.
- From Xiaohei's visible main UI, the owner-visible “Check system Chinese TTS (read-only)” path returned `READY`: engine `com.benjaminwan.chinesettstflite`, 4 Chinese voices, all 4 offline, Simplified Chinese available (`1`), Traditional Chinese unavailable (`0`). The probe did not speak, download, or change settings.
- The engine's built-in fixed Mandarin sample was played with FastSpeech2. Android logged `AudioTrack ... 81600 frames delivered`; the app did not appear in `dumpsys power` wake-lock holders. After returning Home, the TTS service was Android-bound rather than foreground-started.

## Power and claim boundary

The selected engine is offline and performs CPU inference only when speech is requested. It is not the always-on wake path: the OnePlus DSP remains responsible for low-power wake detection. This run is not a battery-life qualification, human intelligibility score, Xiaohei Conversation adapter test, or audible stop-latency result. Those remain under `VOICE-002/004/010/011`, `CHAT-005/012`, and the physical power gates.

No APK, model, raw log, screenshot, voice recording, credential, or private device content is committed.

## Rollback

Select another Android TTS engine or clear the default, then uninstall `com.benjaminwan.chinesettstflite`. Xiaohei's probe remains read-only and will report the resulting state.

[简体中文](acceptance-voice-001.zh-CN.md)
