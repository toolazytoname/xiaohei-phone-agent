# Xiaohei Long-Term Execution Backlog

[简体中文](execution-backlog.zh-CN.md) · [Master plan](sovereign-mobile-agent-master-plan.md) · [Human status](../STATUS.md)

This is an ordered ledger for implementation agents. An agent claims one `READY` task at a time and may mark it `DONE` only after its dependencies, acceptance, evidence, and rollback requirements are satisfied. The Chinese mirror contains the same task IDs with additional operational detail.

## Status semantics

| Status | Meaning |
|---|---|
| `DONE` | Real path plus required denial, rollback, automated, and device evidence |
| `VERIFY` | Implementation exists; independent, human, power, or release gate remains |
| `IN_PROGRESS` | The one task currently being changed in this workstream |
| `READY` | Dependencies are satisfied |
| `BACKLOG` | Dependencies remain |
| `BLOCKED` | External blocker and resume condition are recorded; do not retry unchanged |
| `HUMAN` | Requires a person, physical device action, or independent medium |

## Mandatory protocol for a less-capable agent

1. Read `AGENTS.md`, `STATUS.md`, the master plan, this ledger, and the target task references in full.
2. Run `git status -sb`. Never include user-owned `docs/articles/`, models, APKs, keys, tokens, or private raw evidence.
3. Move exactly one `READY` task to `IN_PROGRESS`; do not refactor adjacent systems.
4. Add or identify acceptance first, then implement the smallest scoped change.
5. Budget model calls, screenshots, and ADB. Never access a phone during a power sample.
6. Code/build success is not device acceptance. Without a real path, stop at `VERIFY`.
7. Stop before root, send/delete/install/grant/payment, private data, or external writes unless the task explicitly authorizes the exact action.
8. Update this ledger, `STATUS.md`, the relevant acceptance record, and bilingual public docs.
9. Run `bash scripts/verify.sh`, relevant unit/build/device checks, and `git diff --check`.
10. Keep one task per PR. Record root cause, impact, checks, rollback, and remaining gates; merge only after CI and review.

Use this handoff shape:

```text
TASK / STATUS / SCOPE / DEPENDENCIES
CHANGED / VALIDATION / EVIDENCE
ROLLBACK / REMAINS / NEXT
```

## Foundation and contracts

| ID | Status | Depends on | Deliverable and acceptance |
|---|---|---|---|
| BASE-001 | DONE | — | M0–M7 evidence matrix and evidence-over-demo rule |
| BASE-002 | DONE | — | Versioned wake, action, Agent-result, and diagnostic contracts |
| BASE-003 | DONE | — | Generic Android, OnePlus DSP, and CPU KWS capability tiers |
| BASE-004 | DONE | BASE-001 | Add master plan, ledger, `STATUS.md`, a bilingual product overview, and generic base-mode UI evidence to the README navigation |
| BASE-005 | DONE | BASE-004 | `conversation-session.v1` schema; two valid boundaries, three rejections, and cross-field checks pass |
| BASE-006 | VERIFY | BASE-005 | `tool-call.v1`, `tool-result.v1`, and capability-token contracts; fixtures reject unknown/replay/cross-task/expiry, pending gateway interoperability |
| BASE-007 | VERIFY | BASE-006 | Unified failure fingerprint and one-recovery rule; unit regression passes, pending real gateway integration |

## Voice and conversation audio

| ID | Status | Depends on | Deliverable and acceptance |
|---|---|---|---|
| VOICE-001 | DONE | BASE-005 | On the OnePlus 8T, the owner-authorized offline engine was installed from an APK whose SHA-256 matches the signed F-Droid index, selected as the Android default, and independently reported by Xiaohei's read-only UI probe as `READY`: 4 Chinese voices, all 4 offline, Simplified Chinese available. The engine's built-in FastSpeech2 sample delivered 81,600 audio frames with no engine wake-lock reference; exact hashes and limits are recorded in `acceptance-voice-001.md` |
| VOICE-002 | DONE | VOICE-001 | OnePlus 8T Xiaohei Conversation initialized the qualified offline Android engine, entered `SPEAKING`, and the visible stop-speech control produced `INTERRUPTED` plus an `AudioTrack` stop after 6,976 frames. Atomic lifecycle/utterance invalidation rejects stale callbacks; 24 transitions, 6 stale-callback rejections, the full unit suite, signed APK build, zero active record clients, and zero Xiaohei/engine wake-lock references passed. Human sound quality and audible latency remain separate gates; see `acceptance-voice-002.md` |
| VOICE-003 | DONE | VOICE-002 | Pure TTS lifecycle models `SPEAKING`, `WAITING_FOLLOWUP`, and `INTERRUPTED` transitions; adapter labels completion/interruption without auto-resume; real audio remains gated |
| VOICE-004 | DONE | VOICE-003 | Identity-bound process leases now wire system TTS, local ASR, system-ASR sessions, and optional CPU KWS. On OnePlus 8T, real offline TTS showed an active output track with no recorder; real offline ASR showed one active 16 kHz input track with no TTS; both returned to zero on visible/global stop. A discovered cancel-before-start race now logs `capture_start_cancelled` without opening the recorder. Full tests, static wiring, signed private build, and installed/build hash match pass; see `acceptance-voice-004.md` |
| VOICE-005 | VERIFY | VOICE-004 | Signal-only policy maps call, alarm, media and Activity interruption to stop input/output and release ownership with no auto-resume; home Activity pause uses it for ASR, while shared Android source/TTS wiring and device audio evidence remain required |
| VOICE-006 | DONE | BASE-005 | Protocol for 30–50 human open-ended Mandarin utterances; no raw audio in Git; bilingual sampling and redaction template published |
| VOICE-007 | HUMAN | VOICE-006 | A/B command ASR against an open-conversation ASR on accuracy/latency/memory; requires the pre-registered real Mandarin samples and device measurements, not synthetic retries |
| VOICE-008 | BACKLOG | VOICE-007 | Independent command/conversation ASR profiles; command correction cannot rewrite chat |
| VOICE-009 | BACKLOG | VOICE-008 | Ordered partial/final streaming transcript UI with cancel/timeout tests |
| VOICE-010 | VERIFY | VOICE-004 | Generation-bound sentence queue is wired to the system-TTS adapter: it exposes a first sentence immediately, advances only after current completion, and invalidates queued/late completions on replacement, stop, interruption or destroy; device audible latency/cancellation evidence remains required |
| VOICE-011 | VERIFY | VOICE-010 | Conversation UI has a separate visible stop-speech control; accepted/repeated replies use the selected system TTS only, and stop/clear/lifecycle paths interrupt it without auto-resume. Audible ≤300ms interruption and echo-loop qualification remain device gates |
| VOICE-012 | BACKLOG | VOICE-011 | Bluetooth/headset/speaker focus and route matrix |

## Conversation engine

| ID | Status | Depends on | Deliverable and acceptance |
|---|---|---|---|
| CHAT-001 | DONE | BASE-005 | Independent Conversation profile; configuration-isolation regression and AOSP user-path visibility pass; credentials/calls remain CHAT-002/003 |
| CHAT-002 | VERIFY | CHAT-001 | Keystore credentials and token-free backup/restore; build/config/backup regression passes, pending independent-device Keystore save-clear-restore path |
| CHAT-003 | DONE | CHAT-002 | Eleven deterministic transport cases plus unit, APK build, and repository gates pass with zero model calls |
| CHAT-004 | DONE | CHAT-003 | Single-turn text chat with zero action authority; lifecycle/static gates and a fresh AOSP-emulator user-path SSE reply pass |
| CHAT-005 | BACKLOG | CHAT-004,VOICE-002 | Human single-turn speech → text → model → TTS; global stop works |
| CHAT-006 | DONE | CHAT-004 | Pure-Java in-memory session enforces contract-aligned turn/time/token budgets, terminal clearing, failed-turn rollback, and zero restore path |
| CHAT-007 | DONE | CHAT-006 | Six-turn half-duplex UI carries bounded context; reference/end/timeout/profile/lock/background paths and fresh AOSP streaming mock pass |
| CHAT-008 | DONE | CHAT-006 | Versioned static prompt envelope; 20 injections, 10 tool forgeries, five sensitive shapes, malformed bounds, and static zero-action/privacy gates pass |
| CHAT-009 | DONE | CHAT-007 | Stop/repeat/clear/continue/end share an exact local parser and idempotent state; unit/static gates and fresh AOSP button path keep mock count at one baseline call |
| CHAT-010 | DONE | CHAT-007 | Independent system/relay TTS selector |
| CHAT-011 | DONE | CHAT-007 | Deterministic five-topic/25-phrase offline FAQ after failed remote turns; visible non-model label, ten unknown/action/injection rejections, zero added model/action/context use, and clean AOSP known/unknown paths |
| CHAT-012 | VERIFY | CHAT-009 | Automated exact-candidate 20-question/5-interruption/5-timeout/5-local-privacy-denial matrix, static zero-recorder path, signed APK, and clean AOSP privacy UI with zero fatal/ANR/active recorder pass; human Mandarin TTS/intelligibility/interruption remains |

## Routing, planning, and authorization

| ID | Status | Depends on | Deliverable and acceptance |
|---|---|---|---|
| ROUTE-001 | DONE | — | Local deterministic `CommandRouter` with ambiguity tests |
| ROUTE-002 | DONE | CHAT-004 | Inert three-way classifier; exact 100-case matrix passes 40 deterministic commands, 35 chats, and 25 complex tasks with zero model/action calls; conceptual keyword and ambiguous-action regressions remain non-actions |
| ROUTE-003 | DONE | ROUTE-002 | Pure local clarification passes an exact 50-case matrix: 10 target, 10 intent, 10 scope asks and 20 clear controls; every ask carries chat/unknown with zero guessed actions, model calls, or action calls and remains unwired |
| ROUTE-004 | DONE | BASE-006,ROUTE-002 | Typed user complex tasks become immutable schema-v1 high-risk pending dry-run requests; 39-case matrix rejects assistant confirmation forgeries, chats/commands, ambiguities, and invalid metadata with zero model/action calls; remains unwired |
| PLAN-001 | DONE | BASE-006 | Versioned dry-run DAG plus rules-first validator; 34 Java cases and five public fixtures cover valid forward/linear plans, exact eight-step/60-second bounds, unknown tools, risk/version/idempotency/dependency/argument failures, and five real cycles with zero model/action calls; remains unwired |
| PLAN-002 | VERIFY | PLAN-001 | Fixed five-field planner envelope permits only action, dry-run, bounded budgets and catalog version; it carries no user text/UI data/path/image/request identity/credential and has no transport or execution capability; real adapter evidence remains required |
| PLAN-003 | VERIFY | PLAN-001 | Metadata-only fail-closed guard requires an exact fresh foreground-package postcondition after every successful step; app switches, stale/invalid observations and failed actions halt all following steps with no retry; real Android observer/adapter and device race evidence remain required |
| PLAN-004 | VERIFY | PLAN-003 | In-memory fail-closed recovery gate records bounded failure evidence, denies unchanged evidence, grants one recovery only after evidence changes, then denies every later recovery; real planner/executor wiring and user-visible recovery evidence remain required |
| POLICY-001 | DONE | — | L0–L4 risk, sensitive-surface denial, and package allowlist foundation |
| POLICY-002 | DONE | ROUTE-004 | Memory-only one-use local-user confirmation bound to task/request/plan, salted target/content digests, eligible foreground/unlocked device state, and a 1–60-second monotonic window; exact 50-case matrix rejects changes, expiry, lock/background, assistant forgeries, cancellation, and replay with zero model/action calls; remains unwired |
| POLICY-003 | DONE | POLICY-002 | In-memory audience-tier boundary allows only same-tier Android/OpenCode metadata and rejects all cross-tier requests; root remains denied until a broker exists |
| POLICY-004 | DONE | POLICY-003 | Permanent local payment/OTP/password/evasion denial corpus over package, visible-text, and requested-label surfaces; 19-case regression with zero model/action calls |

## Tool gateway and Android capabilities

| ID | Status | Depends on | Deliverable and acceptance |
|---|---|---|---|
| TOOL-001 | DONE | BASE-006 | Immutable seven-entry v1 catalog with exact risk, ten closed input/output schemas, rollback declaration, audience, and timeout; Java/fixture/static gates reject duplicates, unknown versions, missing schemas, and unresolved rollback without execution wiring |
| TOOL-002 | DONE | TOOL-001,POLICY-002 | Pure loopback/same-UID authorization core exchanges one internal confirmation receipt for one 1–30-second call-bound capability; 50 Java groups and seven fixture files reject remote, cross-UID, confirmation reuse/scope, malformed/stale call drift, expiry, clock rollback, replay, and foreign gateways with zero adapter execution |
| TOOL-003 | VERIFY | TOOL-002 | Coordinator binds catalog-capped timeout into one-use authority, limits execution to one adapter, interrupts timeout/cancel workers, rejects scope/idempotency replay, and returns bounded private structured errors. The authorized Android bridge now consumes only the existing gateway result and resolves only reviewed MediaStore-test/calendar-test adapters; real permission, user-facing gateway caller, teardown and device evidence remain unverified |
| TOOL-004 | DONE | — | Public intent/settings/gallery/camera/browser/map/dialer actions |
| TOOL-005 | DONE | — | Current-notification summary and confirmed volatile drafts |
| TOOL-006 | DONE | — | Package-bound semantic Accessibility and one memory-only visual recovery |
| TOOL-007 | VERIFY | TOOL-001 | Closed `Pictures/XiaoheiTest/` MediaStore adapter implements query, one-item copy, rename-style move, and exact in-memory rollback IDs; catalog/schema/policy tests reject arbitrary URI/path, batch and unknown operations. Real permission, gateway wiring, system confirmation, and device rollback evidence remain required |
| TOOL-008 | VERIFY | TOOL-001 | Closed `xiaohei-test` calendar adapter previews bounded metadata, creates a no-alarm event only after reviewed input, and rolls back only event IDs created by its current in-memory instance. Arbitrary account/calendar content and batch operations are rejected; user runtime permission, gateway wiring and device rollback remain required |
| TOOL-009 | VERIFY | TOOL-001 | Android media boundary provides only relative media-volume ±1 and output-route observation (Bluetooth/wired/speaker-or-system); it cannot play/pause a session or force a route. Active-session, focus, Bluetooth/headset/speaker matrix and device evidence remain required |
| TOOL-010 | VERIFY | TOOL-003 | User-enabled Accessibility now exposes exact-label select plus one bounded back/scroll operation in a reviewed package; generic approval labels and automatic text entry are rejected. Visible per-request text confirmation, gateway wiring, app/version matrix and device evidence remain required |
| TOOL-011 | VERIFY | TOOL-010 | Closed revision-1 registry covers 15 reviewed package targets with exact-select/one-scroll/one-back capabilities and typed fail-closed explanations; it makes no installed-UI version claim. Gateway caller, per-app/version device matrix, changed-page handling, and text-confirmation path remain required |
| TOOL-012 | VERIFY | TOOL-003 | Metadata-only outcome gate requires a fresh expected foreground-package observation after adapter success; stale/mismatched evidence, failed adapter and repeat verification fail closed; real adapter/observer wiring and reversible catalog evidence remain required |

## OpenCode mobile executor

| ID | Status | Depends on | Deliverable and acceptance |
|---|---|---|---|
| OC-001 | DONE | — | Phone TUI/Web, independent profile, and real relay acceptance |
| OC-002 | DONE | TOOL-002 | Closed private pending dry-run protocol accepts only three reviewed task kinds from an existing typed-user complex request; Java/fixture/static gates reject forged source, live state, unknown authority-shaped fields, and generic shell/workspace/network/root/credential surfaces with zero execution |
| OC-003 | DONE | OC-002 | Task-private input/output lease below a trusted app-private root; real temporary-filesystem matrix rejects absolute/traversal, symbolic-link, duplicate-task, and cross-task paths with zero content/process execution |
| OC-004 | DONE | OC-003 | Injected-adapter bounded runner enforces reviewed profile/agent plus time/token/step/redacted-output limits; matrix covers success, budget exceed, denial, timeout and cancellation with zero real process/network/content paths |
| OC-005 | DONE | OC-004 | Typed lifecycle projection is wired to a read-only visible card; it exposes only kind, stage, and bounded completed-step count while rejecting prompt/token/path/terminal-output surfaces |
| OC-006 | DONE | OC-004 | Registered-task stop cancels the worker, revokes active local gateway tokens, stops injected process/listener/tmux handles, and recursively releases only the private lease without following symlinks; real OS-handle/device acceptance remains open |
| OC-007 | DONE | OC-004 | Fail-closed typed intent policy permits only project summary, test diagnosis, and controlled organization; denies root, sensitive paths, destructive Git/delete, network, shell escape, and unknown text with zero execution |
| OC-008 | DONE | OC-007 | Nine synthetic temporary-workspace rounds: three each for project summary, test diagnosis, and controlled organization; every round passes protocol/policy/bounded adapter/result/cleanup and each kind denies Git/network intent; real OpenCode remains untested |
| OC-009 | VERIFY | OC-008 | In-memory local/Web ownership state transfers control only to a verified opaque web-session handle; duplicate/wrong/terminal transfers deny and no transfer can start, resume or duplicate a task; real Web handoff evidence remains required |
| OC-010 | DONE | OC-008 | Visible bilingual local-small-model guidance limits future use to non-authoritative suggestions; no automatic enable/model switch/planning/tool/root path and no bundled weights |

## Root Capability Broker

| ID | Status | Depends on | Deliverable and acceptance |
|---|---|---|---|
| ROOT-001 | DONE | BASE-006 | Bilingual root threat model, closed allow/deny catalog, and explicit human-device-owner recovery responsibility; catalog grants no capability and generic `su -c` remains denied |
| ROOT-002 | DONE | ROOT-001,POLICY-003 | In-memory fixed-action root broker core binds exact signer, three read-only action IDs, empty parameter schema and one-use request IDs; no root adapter or device execution exists |
| ROOT-003 | DONE | ROOT-002 | Bounded redacted read-only service/port/battery/audio/package/profile diagnostics; fixed category/state/label projection only, with no adapter or device execution |
| ROOT-004 | DONE | ROOT-002 | Service-stop dry-run preflight with exact package/process/PID/port verification, fresh confirmation, and no process signal or device execution |
| ROOT-005 | DONE | ROOT-002 | Fixed-scope in-memory AES-256-GCM backup/restore envelope with fresh IV, wrong-key/tamper rejection, and no disk paths; real persistence/cleanup/offline recovery remains gated |
| ROOT-006 | DONE | ROOT-002 | Fixed-profile in-memory transaction ledger with precheck, snapshot digest, rollback-drift rejection and post-reboot verification state; no installer/device execution exists |
| ROOT-007 | DONE | ROOT-002 | Fixed profile dry-run/diff preview requires distinct exact digests, fresh confirmation and no expiry; result is fixed and no system apply exists |
| ROOT-008 | DONE | ROOT-003 | In-memory redacted root decision audit and permanent broker-instance revocation; no tokens persist and post-revocation requests remain denied; global-stop wiring remains pending |
| ROOT-009 | DONE | ROOT-004 | Fail-closed destructive-command, broad/system-path, and credential/payment/evasion denial corpus; all unmatched root-shaped input stays denied and no command executes |
| ROOT-010 | BACKLOG | ROOT-009 | Install/upgrade/rollback/uninstall on an independent test device first |

## UX and human control

| ID | Status | Depends on | Deliverable and acceptance |
|---|---|---|---|
| UX-001 | DONE | CHAT-004 | Main page shows independent metadata-only wake/ASR/Conversation/Phone Agent/OpenCode/root states; it starts no runtime, reveals no credentials and keeps unavailable paths explicit |
| UX-002 | BACKLOG | CHAT-005 | Bilingual accessible dialog screen with transcript, stop, clear, privacy state |
| UX-003 | DONE | PLAN-001 | Read-only task card projects approved target summary, reviewed steps/current step, budget, fixed result and takeover; no task prose/paths/tokens/model output/reasoning or execution wiring |
| UX-004 | DONE | POLICY-002 | Fixed bilingual confirmation preview shows app, target, content, permission, and stop/rollback; cancel remains default and it grants/executes nothing |
| UX-005 | VERIFY | TOOL-003 | Closed application-level stop hub fans out once to explicitly registered owners, exposes failed release, and provides unregister handles to avoid Activity retention. Home/status stop covers voice/DSP/CPU, Conversation registers pending chat/TTS, and authorized tool execution registers its cancellation signal; Phone Agent, OpenCode, root and device proof of zero resources remain required |
| UX-006 | VERIFY | TOOL-012 | Agent UI keeps redacted trace storage off by default and provides explicit enable, delete-latest-one, clear-all, and disable-and-clear controls. This is not a tool-outcome history: real outcome evidence wiring and independent device behavior remain required |
| UX-007 | DONE | ROOT-003 | Read-only bilingual permission center shows purpose, bounded state/recent-use availability, user-owned Android revoke path, and root's unsupported/unwired reason; it grants nothing |
| UX-008 | VERIFY | UX-003 | Typed public failure projection supplies cause, impact and one recovery path without raw error text; task-card failure is wired, remaining surfaces need adoption |

## Reliability, security, and release

| ID | Status | Depends on | Deliverable and acceptance |
|---|---|---|---|
| REL-001 | VERIFY | — | Three comparable physically unplugged DSP OFF/ARMED A/B runs plus complete 8–24h TSV |
| REL-002 | HUMAN | VOICE-002 | Human prompt/TTS clarity and volume acceptance |
| REL-003 | HUMAN | VOICE-006,VOICE-007 | Pre-registered real multi-speaker/noise/distance qualification |
| REL-004 | BACKLOG | CHAT-012,TOOL-011 | 100 mixed chat/command/task stress; no crash, duplicate action, or recorder residue. The supporting system/app launch matrix reads `mFocusedApp` only when Android 14 omits `topResumedActivity`; this does not replace the mixed workload or device gates |
| REL-005 | BACKLOG | OC-008,ROOT-010 | Weak network/offline/process-kill/reboot/model-timeout recovery |
| REL-006 | BACKLOG | UX-005 | 8–24h service-combination idle regression |
| SEC-001 | DONE | ROOT-001 | Bilingual threat model covers chat/notification injection, tool/schema poisoning, OpenCode traversal, root escalation and destructive/exfiltration paths; device-level acceptance remains explicit |
| SEC-002 | DONE | SEC-001 | Bilingual code-mapped data-flow/retention table covers audio, text, screenshots, notifications, location, files, trace and credentials; static verifier checks the six categories and current local boundaries, while third-party/device evidence remains explicit |
| SEC-003 | VERIFY | TOOL-002 | Bilingual transport/capability boundary maps TLS, redirect, loopback, UID and replay protections to existing automated evidence; independent-device MITM/real-listener exercise remains required |
| SEC-004 | DONE | OC-007,ROOT-009 | Bilingual aggregate adversarial suite preserves injection, traversal, escalation and privacy-exfiltration corpora; automated local boundaries pass while real adapters/devices remain explicit |
| RELEASE-001 | DONE | — | Release separation, external signing, SBOM/provenance, ClamAV baseline |
| RELEASE-002 | BACKLOG | CHAT-012,REL-004 | Versioned generic no-model release, two byte-identical builds, strict scan |
| RELEASE-003 | BACKLOG | RELEASE-002 | Exact fresh install, upgrade/downgrade, rollback, uninstall |
| RELEASE-004 | HUMAN | RELEASE-002 | Move encrypted signing recovery to independent offline media and verify restore |
| RELEASE-005 | BACKLOG | RELEASE-003,REL-001,RELEASE-004 | Bilingual notes/compatibility/limitations, verified bundle, approved public upload |

## Progress operations

| ID | Status | Depends on | Deliverable and acceptance |
|---|---|---|---|
| PROGRESS-001 | DONE | BASE-004 | Bilingual PR template and CI fixtures reject missing status, missing ledger mirror, multiple tasks, or unknown IDs |
| PROGRESS-002 | DONE | PROGRESS-001 | Required ten-field Issue Form, disabled public blanks, two chooser links, and structure/YAML checks pass |
| PROGRESS-003 | BLOCKED | PROGRESS-002 | Ten labels created/read back and manifest/drift checks pass; Project authorization surface unavailable; resume condition is documented |
| PROGRESS-004 | DONE | PROGRESS-001 | Text/JSON report closes all 102 counts and shows current/next, PRs, blockers, human gates, and public evidence |

## Recommended serial path

```text
BASE-004 → BASE-005
→ VOICE-001 → VOICE-002 → VOICE-003 → VOICE-004
→ CHAT-001 → CHAT-002 → CHAT-003 → CHAT-004 → CHAT-005
→ ROUTE-002 → ROUTE-003 → BASE-006 → ROUTE-004
→ PLAN-001 → POLICY-002 → TOOL-001 → TOOL-002
→ CHAT-006 → CHAT-007 → CHAT-009 → CHAT-012
→ OC-002 ... OC-008
→ ROOT-001 ... ROOT-010
→ REL / SEC / RELEASE final gates
```

`REL-001`, `REL-002`, `REL-003`, and `RELEASE-004` are parallel physical/human gates. An implementation agent must never mark them `DONE`.
