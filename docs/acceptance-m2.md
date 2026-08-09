# M2 offline short-command acceptance

Date: 2026-08-09  
Device: OnePlus 8T / Android 14

Thirty-three fixed Chinese utterances pass the pure-Java router test, an unknown money-transfer request is denied, and navigation targets remain bounded and URL-encoded. Real-device actions passed for gallery, system settings, Wi-Fi, Bluetooth, camera, browser, dialer without calling, navigation search, torch on/off, and media volume up/down: 12 successful actions. Alarm launch is honestly unsupported because this ROM has no `SHOW_ALARMS` handler.

The daily UI now offers one-tap offline speech, a global voice-and-DSP stop, a scrollable status surface, an explicit command list, and a redacted diagnostic share sheet. A real Quick Settings tile was registered and tapped from SystemUI; it opened the visible app directly in `LISTENING`. When the activity was interrupted, the session returned to `ARMED` and reported that the microphone had been released. Offline ASR does not depend on network state.

An ambiguous two-target utterance was stopped without an action and asked the user to provide one target. The bounded device harness then completed 30 action-level runs with exactly 30 `ok=true` records and zero failures or duplicate records.

ASR and Phone Agent now have a separate configuration screen. The device rejected an enabled plain-HTTP remote endpoint, accepted HTTPS, encrypted a dummy token into ciphertext/IV backed by Android Keystore, and never stored the plaintext. The test token and test profile were then removed; changing this configuration did not start a service.

The Phone Agent channel now also exposes a user-invoked low-cost `GET /models` health check. It never sends a planning prompt, has bounded timeouts, and disables HTTP redirect following so a Token is not silently forwarded to another endpoint. A real configured-relay health check remains user-environment evidence.

The opt-in ongoing notification was posted on-device with one action. From `ARMED`, its visible “Stop all” action returned the assistant to `OFF` and the DSP profile to `DETACHED`. After a controlled reboot, `boot_completed=1`, the Assistant Role and Quick Settings tile persisted, opening the app showed `OFF / DSP DETACHED`, the ongoing notification returned, and there was no retained command session.

The short command session requests Android transient exclusive speech focus before listening. If the system revokes focus, it cancels recognition, releases the microphone, and exposes an explainable state. A debug-only, visible two-minute interruption-test button was added because the normal eight-second command bound is too short for a human-coordinated incoming call; release builds retain the eight-second limit and do not expose the button. On the physical OnePlus 8T, local ASR logged `session_started` at 15:03:17 and `speech_ready` at 15:03:18. The real incoming call produced `audio_focus_lost change=-2 active=true` at 15:03:38 and `session_stopped microphone_released=true` one millisecond later. The UI returned to `ARMED` with an explicit interruption message, and active recording clients were zero after the call. Logs contain neither transcript nor call identity. Accessibility, root shell, automatic dialing, and background submission were not used. M2 is complete.
