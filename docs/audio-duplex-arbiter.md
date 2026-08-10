# Audio duplex arbiter

`VOICE-004` adds a synchronized ownership rule: **input** (recognition/recorder) and **output** (TTS) cannot be held together. A conflicting acquisition is denied; terminal release and global interruption return ownership to `NONE`.

The regression proves the arbiter never projects simultaneous recorder/TTS ownership. It is deliberately not wired into Android recognizer or TTS adapters yet, so real audio, focus, microphone release and human interruption remain device evidence before completion.
