# Voice conversation troubleshooting

Use this page for the private model-bearing Conversation preview. It does not diagnose or alter DSP, CPU KWS, Phone Agent, OpenCode, or Claude/Happy configuration.

| Symptom | Safe first action | Do not do |
|---|---|---|
| Talk says ASR unavailable | Verify the private model-bearing build and microphone permission; use text chat until repaired. | Do not install a source-only APK over the private model build. |
| Partial text looks wrong | Finish the turn once and inspect final text; use the editable text fallback if needed. | Do not repeat the same phrase until it happens to pass. |
| No model reply | Check the visible Conversation configuration and network, then use one changed-condition retry. | Do not expose token/endpoint or retry unchanged paid failures. |
| Speech overlaps another app | Tap Stop speech; Android focus loss should also stop output. | Do not enable CPU KWS as a workaround. |
| Bluetooth/headset changed | Start a fresh Talk turn after route settlement. | Do not expect listening or speech to continue across the route change. |
| Need complete stop | Use Stop or End chat, then confirm the microphone indicator disappears. | Do not use force-stop as the normal control path. |

If a recorder remains active after a visible stop, record only time, public state label, and build version; revoke microphone permission and report the issue without transcripts, screenshots of private apps, tokens, or raw logs.
