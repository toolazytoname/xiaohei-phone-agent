# Delivery evidence matrix

Status date: 2026-08-09. This is an evidence index, not a release claim. A row is `partial` whenever any stated exit gate lacks current evidence.

| Milestone | Current status | Evidence that exists | Exit gates still missing |
|---|---|---|---|
| M0 — contracts and baseline | partial | Versioned intent, snapshot, trace, diagnostic, and configuration contracts; bilingual product/release/security documents. | Formal public release scope, production signing governance, and final release provenance. |
| M1 — OnePlus voice vertical slice | partial | Three cold boots, three arm/disarm cycles, three screen-off DSP → local ASR → gallery chains, clean transactional uninstall/reinstall. | Physically unplugged DSP OFF/ARMED power A/B; one audible-prompt listening check. |
| M2 — daily short commands | partial | 12 deterministic actions, 30-action regression, tile/status-stop/reboot, privacy-preserving independent model channels; voice session now releases on audio-focus loss. | A real incoming-call interruption, not just lifecycle code or Activity interruption. |
| M3 — generic Android Alpha | partial | Source-only base APK has no DSP/root requirement; profile-disabled portable path and local-ASR action chain are proven. | Fresh install, denied-permission, offline, action, and residue evidence on an independent non-OnePlus device or Android virtual device. |
| M4 — notifications and confirmed drafts | partial | Optional notification access, locked-session denial, volatile visible draft, cancellation, no auto-send; fixture removed afterward. | Isolated non-primary messaging account, revocation-while-running, privacy notification variants, and manual-send observation. |
| M5 — visible Phone Agent | partial | Preview-before-execute, local policy, package binding before/after actions, global stop, Settings two-step task, Calculator task, bounded redacted trace. | Consent-gated screenshot fallback, 10–15 common-app task matrix, real multi-step recovery. |
| M6 — public Beta hardening | partial | Debug/release separation, exact APK scan/SBOM/diagnostics, upgrade/downgrade/uninstall, 100-action stress, 25 surface launches, non-secret model-channel backup, and byte-identical debug builds. | 8–24 hour physical unplugged idle/power, independent malware-engine review, production key governance, model redistribution approval, a real user-config backup/restore drill, production-key reproducibility run and signed APK. |
| M7 — “Xiaohei Xiaohei” and more devices | partial | Opt-in CPU KWS acoustic end-to-end to gallery, independent DSP control, limited quiet-room synthetic baseline. | Multi-speaker/noise/distance and power evidence, second independent device/profile, and custom DSP keyword support or a narrowed public claim. |

## Evidence rules

- Source changes, unit tests, and static scans prove implementation only; they do not replace an environmental or user-facing acceptance run.
- A fixture, mock, or same-device profile-disabled run must be labeled as such and cannot satisfy its independent-account/device gate.
- A failure fingerprint is recorded once and addressed before a retry. Repeating screenshots, acoustic prompts, or paid-model calls without a changed condition is not evidence.
- Debug/test-signed APKs remain internal acceptance artifacts. They must not be presented as public releases.

## Authoritative records

- [M1 OnePlus record](acceptance-oneplus8t-m1.md)
- [M2 short commands](acceptance-m2.md)
- [M3 generic Android](acceptance-m3.md)
- [M4 notifications](acceptance-m4.md)
- [M5 Phone Agent](acceptance-m5.md)
- [M6 hardening](acceptance-m6.md)
- [M7 wake word](acceptance-m7.md)
- [Public release checklist](release-checklist.md)
