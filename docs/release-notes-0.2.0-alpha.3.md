# Xiaohei 0.2.0-alpha.3 — release candidate

[简体中文](release-notes-0.2.0-alpha.3.zh-CN.md) · [Release scope](release-scope-0.2.0-alpha.3.md)

This candidate turns the internal vertical slice into a clearly bounded generic package and keeps model/device enhancements separate.

## Highlights

- Completed the M2 short-command contract, including a real incoming-call interruption that released the microphone and returned to `ARMED`.
- Completed the M4 notification/confirmed-draft contract on independent AOSP Messaging. Confirmation only opens the target app; Xiaohei never fills or sends the message.
- Completed the M5 visible Phone Agent contract with a 10/10 distinct-app semantic matrix and a user-invoked, memory-only screenshot recovery that never uploads or persists pixels.
- Added an independently exercised Android 14 ARM64 generic profile with honest no-DSP state, CPU-KWS visible start/stop, clean uninstall, and zero recording residue.
- Added reproducible release provenance bundling and KWS corpus tools. A 72-case synthetic probe exposed positive speaker-robustness misses; the risk remains visible instead of being promoted as accuracy evidence.
- Bumped the default package to `versionCode=4`, `versionName=0.2.0-alpha.3`.

## Distribution boundary

The intended public generic candidate contains no embedded ASR/KWS model weights. The private combined-model package and OnePlus DSP enhancement are not public release assets. “Xiaohei Xiaohei” remains an experimental opt-in foreground CPU fallback; no custom DSP-keyword claim is made.

## Remaining gates

No public APK has been uploaded. [Exact-candidate ClamAV scanning](malware-scan-0.2.0-alpha.3.md), release provenance, and an encrypted recovery decrypt drill now pass. Physical idle/power, moving the recovery bundle to separately controlled offline media, and final public-upload approval remain tracked. Embedded-model distribution also remains blocked on explicit rights review.
