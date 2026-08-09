# Xiaohei 0.2.0-alpha.2 — internal acceptance build

This is an internal, locally debug-signed candidate, not a public Release.

Highlights since alpha.1:

- Opt-in foreground CPU keyword spotting for “Xiaohei Xiaohei”, independent from the OnePlus DSP controls and disabled by default.
- One shared sherpa-onnx runtime for KWS and offline Chinese ASR; upstream APK/model inputs remain outside Git.
- Visible Phone Agent two-step execution with re-observation after every step.
- Bounded redacted JSONL trace export; no snapshot tree, screenshot, prompt, notification body, or token persistence.
- Bilingual threat model, exact-APK static security gate, and CycloneDX SBOM coverage for both ASR and KWS inputs.
- Verified code 2→3 upgrade, Android downgrade refusal, controlled maintenance downgrade, re-upgrade, transactional uninstall, and fresh install on OnePlus 8T.
- First launch now explains base mode and the optional microphone, notification, Accessibility, and OnePlus DSP capabilities before any permission request.

Open release gates: independent second Android device/profile, real incoming-call and isolated messaging-account acceptance, 10–15 Phone Agent apps plus screenshot fallback, 8–24 hour unplugged power, production signing governance, external dependency/malware review, and model redistribution approval.
