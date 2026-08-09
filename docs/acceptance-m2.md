# M2 offline short-command acceptance

Date: 2026-08-09  
Device: OnePlus 8T / Android 14

Thirty-three fixed Chinese utterances pass the pure-Java router test, an unknown money-transfer request is denied, and navigation targets remain bounded and URL-encoded. Real-device actions passed for gallery, system settings, Wi-Fi, Bluetooth, camera, browser, dialer without calling, navigation search, torch on/off, and media volume up/down: 12 successful actions. Alarm launch is honestly unsupported because this ROM has no `SHOW_ALARMS` handler.

The daily UI now offers one-tap offline speech, a global voice-and-DSP stop, a scrollable status surface, an explicit command list, and a redacted diagnostic share sheet. A real Quick Settings tile was registered and tapped from SystemUI; it opened the visible app directly in `LISTENING`. When the activity was interrupted, the session returned to `ARMED` and reported that the microphone had been released. Offline ASR does not depend on network state.

An ambiguous two-target utterance was stopped without an action and asked the user to provide one target. The bounded device harness then completed 30 action-level runs with exactly 30 `ok=true` records and zero failures or duplicate records.

ASR and Phone Agent now have a separate configuration screen. The device rejected an enabled plain-HTTP remote endpoint, accepted HTTPS, encrypted a dummy token into ciphertext/IV backed by Android Keystore, and never stored the plaintext. The test token and test profile were then removed; changing this configuration did not start a service.

Open M2 gates: the planned foreground notification, a real reboot recovery run on this M2 build, and a real incoming-call interruption run (the equivalent activity interruption path is already verified). Accessibility, root shell, automatic dialing, and background submission are not used.
