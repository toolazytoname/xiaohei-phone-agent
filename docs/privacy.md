# Privacy notice — Xiaohei Assistant

For a code-mapped account of audio, text, screenshot, notification, location, file, trace, and credential handling, see [Data flow and retention](privacy-data-flow.md).

Xiaohei records microphone audio only during a visible, bounded command session and releases it on result, error, interruption, or stop. Offline builds process speech on-device. A separately configured Phone Agent relay is disabled by default; changing model settings does not start it.

Notification access is optional. When granted, Xiaohei reads only active system notifications to answer a user request. It does not persist notification content or expose it from a locked session. Reply drafts remain on screen and are never sent automatically; the current release only opens the target app after the user confirms the displayed app, target, and full content.

The optional OnePlus DSP profile receives a device-local keyword model. Only keyword ID, confidence, and capture availability cross into the main app; wake audio is not included. Users can stop voice and DSP together from the app or ongoing status notification, disable notification access in Android Settings, clear the Phone Agent token, and uninstall with the documented transactional rollback.
