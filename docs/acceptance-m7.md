# M7 custom wake word acceptance — partial

Date: 2026-08-09  
Device: OnePlus 8T (`KB2000`), Android 14 / LineageOS 21  
Candidate: `0.2.0-alpha.2` (`versionCode=3`), debug signed

## Proven on the physical device

- The official sherpa-onnx 1.13.4 Chinese KWS bundle was pinned by SHA-256 `1ee827227c1369b55e0aa5e35de93981ddcaa153238bfa21063260413278f07f`. No upstream binary is committed.
- One shared sherpa runtime provides offline ASR and KWS. The build removes the unused duplicate KWS checkpoint and produces an approximately 64 MB APK.
- The only keyword is `x iǎo h ēi x iǎo h ēi @小黑小黑`. Two independent Mandarin acoustic trials both produced `keyword=小黑小黑` on the first utterance.
- KWS released recording before the visible Assistant session acquired the microphone. After local ASR ended, CPU KWS returned to `LISTENING`.
- The second trial reached the command router. ASR did not yield the intended allowlisted command, so the router returned `未匹配命令` and performed no action. This is a safe denial, not a successful speech-to-action claim.
- An event-synchronized alpha.2 trial completed the full acoustic chain: `keyword=小黑小黑` at 12:12:36.973, local ASR `speech_ready` at 12:12:38.481, spoken “Open Gallery”, `OPEN_GALLERY ok=true` at 12:12:42.311, and the system Photo Picker foreground. CPU KWS then returned to `LISTENING`.
- The independent stop control changed state to `OFF`, destroyed the foreground service, and released active recording. DSP remained `DETACHED`; CPU KWS never changed DSP state.
- Listening snapshot: total PSS about 149 MB, total RSS about 284 MB, thermal status 0. This is not a battery-life result.
- A small deterministic acoustic baseline played 10 near-negative Mandarin phrases (`Xiaobai`, `Xiaoai`, OEM `Xiaobu`, `Xiaomi`, reordered/partial Xiaohei, and ordinary commands): 0/10 false accepts. Five positive “Xiaohei Xiaohei” samples at speech rates 120/135/150/165/180 produced 5/5 detections; every session returned to `LISTENING`. This used one synthetic Mac voice in a quiet environment, not a production accuracy corpus.

## Independent generic profile and synthetic probe

- A clean Android 14 AOSP ARM64 virtual device independently installed the combined candidate with SHA-256 `70e23a097c2c82ba06d7c989a274b20be3f1da5f2874724e6c6f1647d99d1008`. It reported no DSP profile, kept CPU KWS off by default, started it only through a visible action, showed `LISTENING` and a private foreground notification, then stopped with no active service or recording client.
- Uninstall left no package, app process, active service, notification-listener grant, accessibility grant, or recording client. The exact profile and claim boundary are recorded in [`device-profiles/generic-android-api34-arm64`](../device-profiles/generic-android-api34-arm64/README.md).
- A pinned offline evaluator ran 80 disposable macOS-synthesis cases at the product threshold: 40 positive cases across ten system voices with clean, quiet, 10 dB-noise, and synthetic-reverb variants; and 40 near/ordinary negatives. All 40 negatives were rejected. All eight variants from the standard Mandarin voices `Tingting` (mainland) and `Meijia` (Taiwan) were detected; the eight macOS novelty voices were not detected.
- A separate check with the pinned general Mandarin ASR model transcribed both standard voices as “Xiaohei Xiaohei”, while all eight novelty voices produced empty recognition. Those novelty samples therefore do not establish intelligible positive speech and cannot be counted as KWS false rejects. The corpus is a deterministic pipeline diagnostic, not a speaker-robustness or accuracy qualification; no audio is committed and no real speaker or physical distance is represented.
- The public promise is deliberately narrow: “Xiaohei Xiaohei” is an experimental, opt-in foreground CPU fallback. The OnePlus DSP profile still uses the validated OEM phrase. No custom DSP-keyword claim is made.

## Product boundary and open gates

CPU KWS is opt-in, foreground, microphone-visible, higher-power, and disabled by default. It is a portable fallback, not low-power DSP. The second independent generic profile and narrowed public promise now have evidence. M7 still needs real multi-speaker/noise/distance accuracy and unplugged power tests; this synthetic probe is intentionally insufficient for an accuracy promotion.
