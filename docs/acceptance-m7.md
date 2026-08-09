# M7 custom wake word acceptance — partial

Date: 2026-08-09  
Device: OnePlus 8T (`KB2000`), Android 14 / LineageOS 21  
Candidate: `0.2.0-alpha.1` (`versionCode=2`), debug signed

## Proven on the physical device

- The official sherpa-onnx 1.13.4 Chinese KWS bundle was pinned by SHA-256 `1ee827227c1369b55e0aa5e35de93981ddcaa153238bfa21063260413278f07f`. No upstream binary is committed.
- One shared sherpa runtime provides offline ASR and KWS. The build removes the unused duplicate KWS checkpoint and produces an approximately 64 MB APK.
- The only keyword is `x iǎo h ēi x iǎo h ēi @小黑小黑`. Two independent Mandarin acoustic trials both produced `keyword=小黑小黑` on the first utterance.
- KWS released recording before the visible Assistant session acquired the microphone. After local ASR ended, CPU KWS returned to `LISTENING`.
- The second trial reached the command router. ASR did not yield the intended allowlisted command, so the router returned `未匹配命令` and performed no action. This is a safe denial, not a successful speech-to-action claim.
- The independent stop control changed state to `OFF`, destroyed the foreground service, and released active recording. DSP remained `DETACHED`; CPU KWS never changed DSP state.
- Listening snapshot: total PSS about 149 MB, total RSS about 284 MB, thermal status 0. This is not a battery-life result.

## Product boundary and open gates

CPU KWS is opt-in, foreground, microphone-visible, higher-power, and disabled by default. It is a portable fallback, not low-power DSP. M7 still needs an accurate wake-to-action acoustic chain, corpus false-accept/false-reject and unplugged power tests, an independently verified second device/profile, and either custom DSP keyword support or a narrowed public promise.
