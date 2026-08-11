# Xiaohei on-phone operation card

Applies to internal Alpha `0.2.0-alpha.2`. Save this page on the phone (or in a local clone of this repository) so it remains available offline.

## Four facts first

| Item | Default | Relationship |
|---|---:|---|
| Base short commands | Off; invoke from the home screen or Assistant | Does not require DSP, CPU wake word, or Phone Agent |
| CPU “Xiaohei Xiaohei” wake word | Off | Holds the microphone with a foreground notification and uses more power; **does not change DSP** |
| OnePlus DSP wake word | Available only with a compatible enhancement | Separately controlled; **does not change CPU wake word** |
| Speech output | Android offline Chinese TTS | Speaks only after a response; it is not an always-on server or wake path |
| Phone Agent model channel | Off | Stores complex-task configuration only; **saving or switching never starts/stops a service** |

CPU wake word is a portable experimental fallback, not a low-power DSP mode. Use base mode when no compatible DSP profile exists.

Use offline system TTS as the normal speech-output choice. An online TTS relay (including a Microsoft-compatible service) is optional and must be selected deliberately; it can require network access and is not needed for DSP wake or short offline commands.

## Daily use

1. Open Xiaohei and check the independent voice-session, DSP, CPU-wake, and Phone-Agent states.
2. Use “push to talk” or the system Assistant entry for one short command, such as “open gallery.”
3. The microphone should be released after completion, failure, or cancellation.
4. Verify the target and content for send, delete, install, grant, call, or security actions. You still perform the final send in a chat app.

## Start, stop, and state

### CPU “Xiaohei Xiaohei”

- Start: tap the CPU experimental wake button and grant microphone permission when asked. `LISTENING` plus an ongoing notification means it is active.
- Use: say the wake phrase, wait for the short command session, then speak the request. It should return to `LISTENING` afterward.
- Stop: tap the CPU stop button or “stop and release microphone” in its notification. Expected state: `OFF`.
- Error: stop once, then check microphone permission. Do not repeatedly restart; use base push-to-talk if it persists.

### OnePlus DSP (validated profile only on OnePlus 8T)

- Start: tap “start DSP low-power wake word.” `ARMED` is the only ready state.
- Stop: tap “stop and release DSP.” Expected state: `DETACHED`.
- Error: “profile not installed” or a status-read failure does not affect base or CPU modes. Do not repeatedly tap start.
- Roll back: stop and confirm `DETACHED`, then follow the device-specific install/rollback instructions. Never publish private OEM APKs, models, or logs.

### Emergency stop

- Tap “stop all: voice + DSP + CPU wake” in the app, or “stop all” from the ongoing status notification.
- For a running Phone Agent task, use its “global stop” button or “stop Agent” notification action.

### When an Agent cannot find a safe target

If a visible, low-risk Phone Agent task says that it cannot find its semantic target, first read the page yourself. With Phone Agent status notifications enabled, its notification offers **one local visual preview** during the single recovery window. Tapping it captures the current allowed page once, stops the task, and shows a temporary preview when you return to Phone Agent. The preview stays in memory only: it is not uploaded, saved, or used for automatic clicks. After checking it, enter a new exact low-risk target yourself. Do not request a preview on a page containing private information; sensitive pages are rejected before capture.

After stopping there should be no pending command. If the microphone indicator remains, force-stop Xiaohei and revoke microphone permission in Android Settings; record the time and displayed state for diagnosis.

## Models and channels: configuration only

Open “Model channels” from the home screen.

1. Select bundled offline Chinese ASR or Android system recognition. This changes transcription only.
2. Enable Phone Agent only for complex tasks; enter an HTTPS relay URL, model id, and a new token, then save. Tokens are not displayed back.
3. Disable the Agent toggle and save to disable its configuration. This never stops DSP, CPU wake, llama.cpp, Happy, OpenCode Web, or Claude Code.
4. HTTP is permitted only for `localhost` / `127.0.0.1`; all other endpoints must use HTTPS. When networking is unavailable, prefer offline ASR and deterministic short commands.

After an edit, confirm the page says configuration was saved and no service was started. Correct URL/token failures once rather than repeatedly issuing paid model requests.

## Permissions and privacy

- Microphone is used only for an invoked short command or CPU wake word explicitly started by you.
- Notification access is optional; it reads current system notifications only when queried and does not retain chat bodies.
- Accessibility is optional and only serves visible user-started Phone Agent tasks. Payment, banking, password, OTP, and DRM screens are denied by default.
- Phone Agent status notifications are optional but required for notification stop and the one-time local visual-preview recovery. Android 13+ asks for this permission explicitly; decline it if you do not want those controls.
- To fully disable: first use global stop, then revoke permissions in Android Settings; uninstall Xiaohei for full removal. Confirm DSP is `DETACHED` before removing a OnePlus enhancement.

## One useful failure record

Do not repeat an unchanged failure. Record the time, current state, one entry point pressed, visible message, network/Clash status, and whether global stop was used. Never include tokens, full URLs, chat bodies, raw audio, device serials, or private OEM files.

Related: [privacy](privacy.md) · [compatibility](compatibility.md) · [OnePlus M1 acceptance](acceptance-oneplus8t-m1.md) · [power test](power-test.md)
