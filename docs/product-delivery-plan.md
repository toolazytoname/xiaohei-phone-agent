# Manufacturer-grade phone assistant delivery plan

[简体中文](product-delivery-plan.zh-CN.md) · [Roadmap](roadmap.md) · [Architecture](architecture.md) · [Security](../SECURITY.md)

This plan separates a one-off demo from a product that can be installed, used daily, diagnosed, upgraded, and removed safely. Xiaohei does not copy one vendor's private services. It aims for a comparable user experience using public Android capabilities, explicit user grants, and narrowly scoped device integrations.

> **Status reading:** the checkboxes below are the original capability backlog, not a live completion ledger. The authoritative current M0–M7 evidence is the bilingual [delivery evidence matrix](delivery-evidence-matrix.md): M0/M2/M3/M4/M5 are complete at their stated scope; M1/M6/M7 retain the explicitly listed physical power, acoustic, recovery-media, and/or publication gates. Do not infer completion from an unchecked backlog item, or completion of a physical gate from an implementation alone.

## 1. Delivery targets

### First device product

Deliver this closed loop on the current OnePlus 8T:

> Screen off → say the already validated stock phrase “Xiaobu Xiaobu” → hear an acknowledgement → say “open the gallery” → see the transcript and planned action → open the system image surface → return to the armed state.

The first device build keeps the proven stock phrase. A custom “Xiaohei” phrase is a later qualification item and must not disguise an always-on CPU recognizer as DSP support.

### Portable public product

An ordinary Android user can install the base APK without root, OnePlus libraries, or Qualcomm DSP and invoke it through push-to-talk, a Quick Settings tile, or the system assistant role where available. A DSP package is an optional, separately tested device enhancement.

### Out of scope

- No private protocol impersonation or claim of evading app abuse controls.
- No direct model authority to send, delete, purchase, install, or grant permissions.
- No OEM APK/model/library, signing key, provider credential, private endpoint, or real conversation in public artifacts.
- No completion claim based only on a living process, HTTP 200, a clickable button, or one successful run.
- No hidden microphone, accessibility, notification access, or background task.

## 2. Manufacturer-assistant capability checklist

Priority: `P0` is required for the first usable product, `P1` for public Alpha/Beta, and `P2` for closer manufacturer-level parity.

### Invocation and standby

- [ ] `P0` Push-to-talk base mode on compatible Android devices.
- [ ] `P0` Quick Settings and home entry with an honest availability state.
- [ ] `P0` OnePlus 8T DSP Companion exposing only status, arm, disarm, and wake event.
- [ ] `P0` Independent wake, voice-session, model-provider, and action states.
- [ ] `P0` Bounded re-arm state machine with duplicate-task protection.
- [ ] `P0` Defined screen-off, lock-screen, background, unlock, and reboot behavior.
- [ ] `P1` Runtime-detected system assistant and hardware-gesture entry.
- [ ] `P1` Explicit opt-in CPU keyword mode with a foreground notification and power warning.
- [ ] `P1` Sensitivity, cooldown, rejection, and acknowledgement settings.
- [ ] `P2` Qualified custom “Xiaohei” keyword.
- [ ] `P2` Headset, Bluetooth, car, and external microphone paths.

### Speech input and output

- [ ] `P0` Open a bounded command audio session only after invocation.
- [ ] `P0` VAD/endpointer, timeout, cancellation, and immediate microphone release.
- [ ] `P0` Streaming Chinese ASR with visible partial/final text and actionable errors.
- [ ] `P0` Local earcon or short TTS for invoked, acting, completed, and failed states.
- [ ] `P1` Independent local/proxy ASR routing and custom vocabulary.
- [ ] `P1` Barge-in, playback interruption, and one clarification turn.
- [ ] `P2` Multi-turn context with explicit expiry and offline short-command fallback.

### Intent and planning

- [ ] `P0` Deterministic commands before LLM planning.
- [ ] `P0` Versioned intent schema; raw model text never becomes coordinates directly.
- [ ] `P0` Reject unknown tools, missing arguments, and ambiguous targets.
- [ ] `P0` Per-task ID, step limit, timeout, cancellation, and schema validation.
- [ ] `P1` Local lightweight routing plus user-selected remote planning for complex tasks.
- [ ] `P1` Observe-plan-act-verify with one evidence-based recovery attempt.
- [ ] `P2` User-controlled memory and successful-trajectory-to-Skill compilation.

### Android actions

- [ ] `P0` Public intents for apps, gallery, browser, settings, dialer, and map targets.
- [ ] `P0` Reversible device controls with system-settings handoff when Android restricts direct control.
- [ ] `P0` Structured success, partial, needs-user, failed, and cancelled results.
- [ ] `P0` Target, lock state, permission, and duplicate checks before acting.
- [ ] `P1` User-granted notification summaries without crawling private app storage.
- [ ] `P1` Accessibility semantics first; coordinates and screenshots are bounded fallbacks.
- [ ] `P1` Versioned adapters and regression tasks for 10–15 common apps.
- [ ] `P1` Message drafts with recipient/content preview and fresh confirmation.
- [ ] `P2` Cross-app tasks, state recovery, and semantic/visual fusion.

### Product UX

- [ ] `P0` Onboarding that explains every sensitive capability before requesting it.
- [ ] `P0` Independent wake backend, ASR, Phone Agent model, and action-service status.
- [ ] `P0` Visible OFF, ARMED, LISTENING, THINKING, CONFIRMING, ACTING, and ERROR states.
- [ ] `P0` One task card showing transcript, plan, current step, and result.
- [ ] `P0` Global stop from the app, foreground notification, and task surface.
- [ ] `P0` Errors with cause, impact, and one valid recovery entry.
- [ ] `P1` Tile, widget, assistant session UI, optional bubble, redacted history, and permission center.
- [ ] `P1` Provider name, model, health, locality/cost hints, and no token disclosure.
- [ ] `P2` Lock-screen/headset/car feedback and complete accessibility/i18n support.

### Model routing

- [ ] `P0` A dedicated Phone Agent profile, independent from Claude Code, OpenCode, and Happy.
- [ ] `P0` Shared credential source is allowed, but activation and rollback remain channel-specific.
- [ ] `P0` Android Keystore/encrypted storage; secrets never appear in UI or logs.
- [ ] `P0` Changing a model never starts or stops llama.cpp, Happy, or OpenCode Web.
- [ ] `P0` Low-cost health checks instead of full agent requests.
- [ ] `P1` Local routing/offline fixed commands; remote complex vision and planning.
- [ ] `P1` Explicit timeout, rate-limit, balance, context, and format errors.
- [ ] `P1` At most one bounded provider fallback; no paid retry loop.

### Safety and privacy

- [ ] `P0` Low, medium, high, and forbidden-by-default risk classes.
- [ ] `P0` Fresh target/content-bound confirmation for sends, deletes, purchases, installs, grants, and security changes.
- [ ] `P0` Lock-screen allowlist and private-content suppression.
- [ ] `P0` No observation or automation by default on payment, banking, password, OTP, or DRM surfaces.
- [ ] `P0` Android-visible microphone, notification, and accessibility use.
- [ ] `P0` Redacted logs; no raw audio, full chat body, or credentials.
- [ ] `P0` Least-privilege components, emergency stop, timeout, idempotency, uninstall, and rollback.
- [ ] `P1` Per-app allow/deny policy and user-previewed diagnostic export.
- [ ] `P1` Conservative WeChat boundary: notification summary, official app launch, and confirmed draft; no zero-risk claim.
- [ ] `P2` Threat model, dependency scanning, SBOM, signing policy, and vulnerability response.

### Android lifecycle and reliability

- [ ] `P0` Correct foreground-service, audio-focus, call, alarm, media, and route behavior.
- [ ] `P0` Explainable recovery after process death, service crash, network change, or provider timeout.
- [ ] `P0` Only user-enabled modes resume after boot.
- [ ] `P0` Upgrade, downgrade, clear-data, and uninstall leave no model, recording session, or persistent wake lock.
- [ ] `P0` Unique-UID DSP Companion limited to SoundTrigger lifecycle and redacted events.
- [ ] `P1` Runtime assistant-role detection, Doze/background handling, and migration tests.
- [ ] `P2` Multi-user, work-profile, foldable, and additional-ROM support.

### Evidence and support

- [ ] `P0` Local structured state, duration, result, and redacted failure fingerprint.
- [ ] `P0` No repeated acoustic, screenshot, or model test for an unchanged failure fingerprint.
- [ ] `P0` Unit coverage for state machines, schemas, risk policy, and idempotency.
- [ ] `P0` Real-device install, permission, arm, invoke, act, cancel, reboot, and uninstall tests.
- [ ] `P0` Three clean fixed-task runs and three OnePlus cold-boot/arm/acoustic cycles.
- [ ] `P0` Physically unplugged DSP OFF/ARMED power comparison, AudioRecord and AP-wakelock checks.
- [ ] `P1` Noise, distance, accent, false accept/reject, Bluetooth, 8–24 hour idle, and 100-task stress runs.
- [ ] `P1` 20+ app/page-version regression matrix and evidence-backed release reports.

## 3. Delivery sequence

| Milestone | Outcome | Exit gate |
|---|---|---|
| M0 — Contracts | Freeze event/action schemas, state machine, errors, and evidence format | Development APK and public Release are clearly distinguished |
| M1 — OnePlus vertical slice | “Xiaobu Xiaobu” → Chinese ASR → open gallery → re-arm | Three cold boots, arm cycles, and screen-off acoustic runs; clean uninstall |
| M2 — Daily short commands | Ten deterministic commands, voice UI, stop, recovery, independent profiles | Thirty fixed commands without duplicate action; reboot/call/network recovery |
| M3 — Generic Android Alpha | Rootless push-to-talk/tile/assistant base; separate OnePlus enhancement | Fresh install on at least one non-OnePlus device and honest capability states |
| M4 — Notification assistant | Notification unread summary and confirmed message draft | Permission/privacy/lock-screen and isolated-account tests pass |
| M5 — Visible Phone Agent | Bounded semantic-first cross-app execution | 10–15 app task matrix, sensitive-surface denial, global stop, regression traces |
| M6 — Public Beta | Release hardening, power, migration, diagnostics, security artifacts | 8–24 hour idle, 100-task stress, signed reproducible release |
| M7 — Custom keyword/devices | Qualified “Xiaohei” and a second device profile | Accuracy, power, rollback, and independent-device gates pass |

## 4. Required release artifacts

- [ ] Generic Android APK and a separately packaged OnePlus DSP enhancement.
- [ ] Reproducible build, release signature, version, SHA-256, and SBOM.
- [ ] Install, upgrade, permission, command, troubleshooting, disable, uninstall, and rollback guides.
- [ ] Bilingual README, release notes, privacy notice, and compatibility matrix.
- [ ] Real-device report with environment, steps, expected/actual result, evidence, and residue check.
- [ ] Automated tests, CI, secret/proprietary-asset release gates, known limitations, and issue template.
- [x] An offline on-phone operation card for service state, model switching, and rollback ([operation card](phone-operation-card.md)).

## 5. Engineering scale

| Stage | Focused single-developer scale | Result |
|---|---:|---|
| M1 | 1–3 weeks | Real “Xiaobu Xiaobu → open gallery” loop on this OnePlus 8T |
| M2 | another 2–4 weeks | Ten commands and a usable internal Beta |
| M3–M4 | another 1–2 months | Generic Android Alpha, notification summary, confirmed drafts |
| M5–M6 | another 2–4 months | Visible Phone Agent, app regression, public Beta reliability |
| M7 / closer OEM parity | ongoing 6–12+ person-months | Custom low-power keyword, multiple devices, long-term compatibility |

The visible interaction can arrive quickly on the current device. The difficult tail is not attaching an LLM; it is screen-off invocation, Android lifecycle, power, permission safety, changing app UIs, and recovery. OEM assistants can modify the system image and ship privileged services. A public third-party product must compensate with capability tiers, explicit grants, and honest compatibility evidence.

## 6. Immediate single vertical slice

```text
OnePlus 8T screen off
  → stock “Xiaobu Xiaobu” DSP callback
  → bounded Xiaohei audio session
  → Chinese ASR: “open the gallery”
  → deterministic intent
  → low-risk policy
  → system image surface
  → visible result
  → microphone release and ARMED
```

Until this path passes three cold-boot and three acoustic cycles, work does not branch into automated WeChat replies, visual-agent tasks, a custom keyword, or additional devices. This keeps device operations, screenshots, tokens, and model calls bounded while producing a real reusable acceptance case.
