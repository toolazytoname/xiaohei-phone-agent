# Delivery evidence matrix

Status date: 2026-08-10. This is an evidence index, not a release claim. A row is `partial` whenever any stated exit gate lacks current evidence.

| Milestone | Current status | Evidence that exists | Exit gates still missing |
|---|---|---|---|
| M0 — contracts and baseline | complete | Versioned intent, snapshot, trace, diagnostic, and configuration contracts; frozen bilingual generic/model/device release scope; external RSA-4096 signing governance; exact source/certificate/APK/SBOM provenance bundle. | None for the M0 contract. Public upload and endurance gates remain M6 work. |
| M1 — OnePlus voice vertical slice | partial | Three cold boots, three arm/disarm cycles, three screen-off DSP → local ASR → gallery chains, clean transactional uninstall/reinstall. | Physically unplugged DSP OFF/ARMED power A/B; one audible-prompt listening check. |
| M2 — daily short commands | complete | 12 deterministic actions, 30-action regression, tile/status-stop/reboot, privacy-preserving independent model channels; a real incoming call interrupted an active local-ASR session, produced transient focus loss, released the microphone, returned to `ARMED`, and left zero recording clients. | None for the M2 contract. |
| M3 — generic Android Alpha | complete | Source-only base APK has no DSP/root requirement; a clean Android 14 AOSP ARM64 virtual device completed fresh install, no-permission onboarding, microphone denial, offline gallery routing, and uninstall residue checks. | None for the M3 generic-base contract. CPU wakeword power remains an M7 condition. |
| M4 — notifications and confirmed drafts | complete | Optional access, lock/screen-off denial, PUBLIC/PRIVATE/SECRET handling, exact-notification freshness, dynamic revocation clearing, volatile visible draft, and an independent AOSP Messaging synthetic identity. Confirmation opened only the app; sent rows remained zero and no message data persisted in Xiaohei. | None for the M4 contract; unattended primary-WeChat automation remains explicitly out of scope. |
| M5 — visible Phone Agent | complete | Preview-before-execute, local policy, package binding before/after actions, global stop, Settings two-step and Calculator tasks, a 10/10 distinct-app AOSP matrix, bounded redacted trace, and a real notification-invoked local visual recovery followed by manual exact-label retry. Screenshots are never uploaded or persisted. | None for the M5 contract. |
| M6 — public Beta hardening | partial | Debug/release separation, external release identity, exact APK scan/SBOM/provenance, exact release fresh-install rollback, upgrade/downgrade/uninstall, 100-action stress, 25 surfaces, tested non-secret backup/restore, byte-identical builds, independent ClamAV scan, and verified encrypted signing-recovery staging. | 8–24 hour physical unplugged idle/power, moving the verified signing recovery to separately controlled offline media, and final public-upload approval. Model rights block only the private model-bearing package, not the no-model generic candidate. |
| M7 — “Xiaohei Xiaohei” and more devices | partial | Opt-in CPU KWS acoustic end-to-end to gallery, independent DSP control, a clean Android 14 ARM64 generic profile with start/stop/uninstall rollback, a narrowed no-custom-DSP claim, and a reproducible 80-case synthetic diagnostic. | Real multi-speaker/noise/distance and unplugged-power evidence; only two standard Mandarin TTS voices have intelligible positive controls, so the synthetic diagnostic cannot qualify human accuracy. |

## Long-term incremental capabilities

| Task | Current state | Evidence now | Remaining gates |
|---|---|---|---|
| `CHAT-003` — bounded Conversation transport | Complete in task scope | SSE-first with JSON fallback, loopback `NO_PROXY`, no redirects/retries, and a 64 KiB cap; eleven deterministic transport cases, APK build, and repository gates pass with zero model calls. | The `CHAT-005` human voice/TTS loop and final `CHAT-012` conversation acceptance remain open. |
| `CHAT-004` — single-turn Conversation UI | Complete in task scope | Zero-authority bilingual UI, race-safe cancel/destroy handling, six stable accessibility identifiers, static action-boundary gate, and one fresh AOSP-emulator user-path streaming mock reply without crash or ANR. | Physical-device credential acceptance remains `CHAT-002`; human speech/TTS and final conversation acceptance remain `CHAT-005`/`CHAT-012`. |
| `CHAT-006` — bounded in-memory session | Complete in task scope | Contract-aligned 1–8 turn, 64–8192 estimated-token, and 1 s–15 min bounds; twelve deterministic lifecycle cases and a static no-persistence/logging gate pass. Terminal paths release session-owned transcript references; new instances restore no text. | Integrated by `CHAT-007`; this remains a reference-release guarantee, not a JVM memory-zeroization claim. |
| `CHAT-007` — half-duplex follow-up | Complete in task scope | Six-turn/2048-token/five-minute UI integration; eleven deterministic reference/end/timeout/profile/lock/background/half-duplex/invalid-reply cases; fresh AOSP user path sent two streaming turns, server verified exact prior context, UI cleared on End, and no fatal/ANR occurred. | Idempotent zero-call conversation controls remain `CHAT-009`; speech/TTS and human acceptance remain `CHAT-005`/`CHAT-012`. |
| `CHAT-008` — minimal prompt boundary | Complete in task scope | One versioned static system envelope; 20 prompt injections, 10 assistant tool forgeries, five sensitive shapes, malformed bounds, immutability, and static no-dynamic-context/no-action-path gates pass with zero model calls. | This proves role/context enforcement and zero action authority, not universal model obedience or answer quality. |

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
