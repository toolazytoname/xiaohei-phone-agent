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
| VOICE-001 | VERIFY | BASE-005 | Read-only Chinese TTS probe implemented and built; initial OnePlus/AOSP package probes found no engine, pending candidate-package independent-device UI path |
| VOICE-002 | VERIFY | VOICE-001 | System TTS adapter with explicit lifecycle; 10 transitions/build pass, real speech/stop awaits a device with a registered engine |
| VOICE-003 | BACKLOG | VOICE-002 | Add `SPEAKING`, `WAITING_FOLLOWUP`, and `INTERRUPTED` legal transitions |
| VOICE-004 | BACKLOG | VOICE-003 | Half-duplex lifecycle; audio evidence proves recorder and TTS do not overlap |
| VOICE-005 | BACKLOG | VOICE-004 | Calls, alarms, media, and Activity interruption release audio cleanly |
| VOICE-006 | DONE | BASE-005 | Protocol for 30–50 human open-ended Mandarin utterances; no raw audio in Git; bilingual sampling and redaction template published |
| VOICE-007 | BACKLOG | VOICE-006 | A/B command ASR against an open-conversation ASR on accuracy/latency/memory |
| VOICE-008 | BACKLOG | VOICE-007 | Independent command/conversation ASR profiles; command correction cannot rewrite chat |
| VOICE-009 | BACKLOG | VOICE-008 | Ordered partial/final streaming transcript UI with cancel/timeout tests |
| VOICE-010 | BACKLOG | VOICE-004 | Sentence-buffered streaming TTS whose queue cancels immediately |
| VOICE-011 | BACKLOG | VOICE-010 | Button interruption first; voice barge-in only after echo-loop qualification |
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
| PLAN-001 | READY | BASE-006 | Rules-first planner and versioned plan schema |
| PLAN-002 | BACKLOG | PLAN-001 | Remote planner receives minimum snapshot only |
| PLAN-003 | BACKLOG | PLAN-001 | Re-observe after every step; app-switch race causes zero out-of-scope actions |
| PLAN-004 | BACKLOG | PLAN-003 | One evidence-changing recovery, no unchanged retry loop |
| POLICY-001 | DONE | — | L0–L4 risk, sensitive-surface denial, and package allowlist foundation |
| POLICY-002 | BACKLOG | ROUTE-004 | Fresh confirmation bound to task, target, content, and expiry |
| POLICY-003 | BACKLOG | POLICY-002 | Separate Android/OpenCode/root authorization tiers |
| POLICY-004 | BACKLOG | POLICY-003 | Permanent payment/OTP/password/evasion denial corpus |

## Tool gateway and Android capabilities

| ID | Status | Depends on | Deliverable and acceptance |
|---|---|---|---|
| TOOL-001 | BACKLOG | BASE-006 | Versioned tool catalog with risk, schema, and rollback |
| TOOL-002 | BACKLOG | TOOL-001,POLICY-002 | Loopback gateway and short-lived capability tokens; reject remote/replay/cross-task |
| TOOL-003 | BACKLOG | TOOL-002 | Timeout, cancel, idempotency, and structured error per tool |
| TOOL-004 | DONE | — | Public intent/settings/gallery/camera/browser/map/dialer actions |
| TOOL-005 | DONE | — | Current-notification summary and confirmed volatile drafts |
| TOOL-006 | DONE | — | Package-bound semantic Accessibility and one memory-only visual recovery |
| TOOL-007 | BACKLOG | TOOL-001 | MediaStore query/copy/move/rollback in a test collection |
| TOOL-008 | BACKLOG | TOOL-001 | Calendar/reminder preview, confirm, create, and rollback in a test account |
| TOOL-009 | BACKLOG | TOOL-001 | Media play/pause/volume/routing matrix |
| TOOL-010 | BACKLOG | TOOL-003 | Semantic input/scroll/back/select; never click generic approval controls |
| TOOL-011 | BACKLOG | TOOL-010 | Versioned adapters and failure explanations for 15+ common apps |
| TOOL-012 | BACKLOG | TOOL-003 | Before/after snapshots and rollback catalog; exit code alone is not success |

## OpenCode mobile executor

| ID | Status | Depends on | Deliverable and acceptance |
|---|---|---|---|
| OC-001 | DONE | — | Phone TUI/Web, independent profile, and real relay acceptance |
| OC-002 | BACKLOG | TOOL-002 | Xiaohei/OpenCode task protocol with no generic shell authority |
| OC-003 | BACKLOG | OC-002 | Per-task workspace and allowed directories; reject traversal/symlink escape |
| OC-004 | BACKLOG | OC-003 | Bounded `oc run`: model/agent/time/token/step/output limits |
| OC-005 | BACKLOG | OC-004 | Redacted streaming progress mapped to the visible task card |
| OC-006 | BACKLOG | OC-004 | Stop kills subprocess, tokens, listeners, tmux, and temp workspace |
| OC-007 | BACKLOG | OC-004 | Restricted tools; deny root, key directories, and destructive Git |
| OC-008 | BACKLOG | OC-007 | Three rounds each: project summary, test diagnosis, controlled file organization |
| OC-009 | BACKLOG | OC-008 | Optional Web takeover with explicit ownership handoff |
| OC-010 | BACKLOG | OC-008 | Local-small-model recommendation remains visible and non-automatic |

## Root Capability Broker

| ID | Status | Depends on | Deliverable and acceptance |
|---|---|---|---|
| ROOT-001 | BACKLOG | BASE-006 | Bilingual root threat model, allow/deny catalog, recovery responsibility |
| ROOT-002 | BACKLOG | ROOT-001,POLICY-003 | Signature-bound broker with fixed action ids and exact schemas |
| ROOT-003 | BACKLOG | ROOT-002 | Bounded redacted read-only service/port/battery/audio/package/profile diagnostics |
| ROOT-004 | BACKLOG | ROOT-002 | Service lifecycle with package/PID/port verification and no wrong-process kill |
| ROOT-005 | BACKLOG | ROOT-002 | Fixed encrypted backup/restore; no plaintext residue |
| ROOT-006 | BACKLOG | ROOT-002 | Transactional device-profile install/uninstall with reboot checks |
| ROOT-007 | BACKLOG | ROOT-002 | Dry-run, diff preview, and fresh confirmation for system changes |
| ROOT-008 | BACKLOG | ROOT-003 | Redacted root audit and token revocation on global stop |
| ROOT-009 | BACKLOG | ROOT-004 | Denial corpus for destructive commands, broad paths, and credential/payment data |
| ROOT-010 | BACKLOG | ROOT-009 | Install/upgrade/rollback/uninstall on an independent test device first |

## UX and human control

| ID | Status | Depends on | Deliverable and acceptance |
|---|---|---|---|
| UX-001 | BACKLOG | CHAT-004 | Independent wake/ASR/Conversation/Phone Agent/OpenCode/root status |
| UX-002 | BACKLOG | CHAT-005 | Bilingual accessible dialog screen with transcript, stop, clear, privacy state |
| UX-003 | BACKLOG | PLAN-001 | Task card: target, plan, current step, budget, result, takeover; no chain-of-thought |
| UX-004 | BACKLOG | POLICY-002 | Confirmation shows app, target, content, permission, and rollback |
| UX-005 | BACKLOG | TOOL-003 | App/notification/voice/widget global stop; all resources reach zero |
| UX-006 | BACKLOG | TOOL-012 | Redacted history delete-one/clear-all/disable-storage |
| UX-007 | BACKLOG | ROOT-003 | Permission center with purpose, last use, revoke, unsupported reason |
| UX-008 | BACKLOG | UX-003 | Every failure shows cause, impact, and one valid recovery path |

## Reliability, security, and release

| ID | Status | Depends on | Deliverable and acceptance |
|---|---|---|---|
| REL-001 | VERIFY | — | Three comparable physically unplugged DSP OFF/ARMED A/B runs plus complete 8–24h TSV |
| REL-002 | HUMAN | VOICE-002 | Human prompt/TTS clarity and volume acceptance |
| REL-003 | HUMAN | VOICE-006,VOICE-007 | Pre-registered real multi-speaker/noise/distance qualification |
| REL-004 | BACKLOG | CHAT-012,TOOL-011 | 100 mixed chat/command/task stress; no crash, duplicate action, or recorder residue |
| REL-005 | BACKLOG | OC-008,ROOT-010 | Weak network/offline/process-kill/reboot/model-timeout recovery |
| REL-006 | BACKLOG | UX-005 | 8–24h service-combination idle regression |
| SEC-001 | BACKLOG | ROOT-001 | Threat model for chat injection, tool poisoning, and root |
| SEC-002 | BACKLOG | SEC-001 | Audio/text/image/notification/location/file data-flow and retention table |
| SEC-003 | BACKLOG | TOOL-002 | Capability-token/TLS/redirect/loopback/replay tests |
| SEC-004 | BACKLOG | OC-007,ROOT-009 | Adversarial injection/traversal/escalation/exfiltration suite |
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
