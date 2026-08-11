# Audio duplex arbiter

`VOICE-004` provides a synchronized ownership rule: **input** (recognition/recorder) and **output** (TTS) cannot be held together. A conflicting acquisition is denied; terminal release returns ownership to `NONE`.

The process-wide layer issues one identity-bound lease at a time. A duplicate acquisition is denied, and a stale or foreign lease cannot release the current owner. It is wired into Xiaohei's system-TTS adapter, local ASR service, system-ASR command session, and optional CPU wakeword recorder. The DSP wake path does not acquire a microphone lease because the Android profile requests no capture and the DSP owns its low-power hardware path.

Deterministic tests cover input/output conflicts, duplicate acquisition, stale release and terminal return to `NONE`. Device acceptance must additionally prove that a real TTS track and active recorder never overlap and both return to zero after stop. Human sound quality and spoken interruption remain separate gates.
