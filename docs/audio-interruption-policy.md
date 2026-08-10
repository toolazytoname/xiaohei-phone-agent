# Audio interruption policy

`AudioInterruptionPolicy` maps four non-content interruption sources—call, alarm, media and Activity lifecycle—to one fail-closed decision: stop input, stop output, and release audio ownership. It never resumes either path automatically.

The home Activity now routes its pause path through the policy before stopping its active ASR session. Existing ASR audio-focus loss already stops and releases input. System TTS exposes a separate interruption method, but no shared Android adapter yet connects every source to both ASR and TTS.

No phone-state permission, call metadata, alarm text, media metadata, or audio content is collected. VOICE-005 remains `VERIFY` until real interruption sources and audio resources are exercised on device.
