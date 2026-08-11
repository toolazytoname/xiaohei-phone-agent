# Xiaohei Free Voice Chat: Executor Runbook

[中文](free-voice-chat-executor-runbook.zh-CN.md) · [Delivery plan](free-voice-chat-delivery-plan.md) · [Current status](../STATUS.md) · [Execution backlog](execution-backlog.md)

Status date: 2026-08-11. This is the single ordered, hand-off-ready work list for free voice chat. It is not a completion claim.

## 0. Delivery definition and fixed boundaries

The minimum delivery is **L2 push-to-talk voice chat**: the user taps Talk; only that utterance is recorded; open Mandarin ASR transcribes it; the selected Conversation channel receives it; offline system TTS speaks the reply; another utterance requires an explicit **Continue talking** tap. Text and voice share one in-memory session, capped at six turns, 2,048 tokens, and five minutes.

The optional next level is **L3 DSP per-turn chat**: qualified OEM DSP wake → exact “start chatting” intent → one open question → response/speech → Android recording released and DSP re-armed. This is not a claim of a custom “Xiaohei Xiaohei” DSP word.

- A chat reply is text/speech only: it never reaches `ActionDispatcher`, root, OpenCode, or a tool executor.
- Complex device work produces only an editable Phone Agent draft with a fresh visible confirmation. Payments, transfers, OTPs, passwords, and protection bypass remain permanently denied.
- Never commit or output credentials, private URLs, models/APKs, raw audio/transcripts, device serials, or UI dumps; never modify untracked `docs/articles/`.
- Never overwrite the OnePlus model-bearing private candidate with a generic model-free APK.
- Never retry an unchanged failure fingerprint. A new minimal verification is allowed only after code, endpoint/model, network, or physical state has actually changed.

## 1. Common procedure

For one `READY` task only: read this page, `STATUS.md`, plan, backlog, sources and evidence; run `git status -sb`; test before the smallest implementation; run task checks plus `bash scripts/verify.sh` and `git diff --check`; record automated/AOSP/OnePlus/HUMAN evidence separately; make one small PR with rollback and remaining gates; merge only after CI/review; then update status/backlog. At HUMAN gates, record the one needed action and stop instead of polling ADB or taking repeated screenshots.

## 2. Single execution queue

| # | Task | State | Scope | Acceptance / completion | Stop condition |
|---:|---|---|---|---|---|
| 1 | `FVC-040A` voice-turn code gate | READY | Conversation UI/state machine/`VoiceCommandSession`/tests | Talk stops TTS before input lease; partial never sends; one final sends at most once; empty/cancel/timeout release recorder; Thinking/Speaking never record; stale callbacks invalidate. Static/JVM pass → `VERIFY`. | Any action path or lease overlap. |
| 2 | `FVC-050A` half-duplex multi-turn code gate | READY after 1 | coordinator/control policy/tests | Only `WAITING_FOLLOWUP` offers Continue; one ASR turn per tap; shared six-turn/2048/five-minute context; controls are local/zero-call; switch cancels/clears. | Auto-mic after TTS or hidden separate contexts. |
| 3 | `FVC-060A` DSP-to-Conversation resource boundary | READY after 1 | DSP short intent/launch/resource gates | Exact start-chat intent only; no custom-DSP claim; recorder releases per turn; network/TTS do not start CPU KWS; deterministic DSP/TTS collision. | DSP asset/root change or persistent CPU recording. |
| 4 | `FVC-070A` interruption/route code gate | READY after 1 | TTS focus/Activity/call/route signals/tests | Pause/lock/global stop/route cancel input, HTTP and output without auto-resume; focus and stale-route coverage. | Private call audio or repeated dialing required. |
| 5 | `FVC-110A` model-free AOSP regression | READY after 1–4 | public source-only build/emulator | Fresh install; honest ASR state; two mocked turns; zero recorder/Fatal/ANR after stop; clean uninstall. Not OnePlus evidence. | Unstable baseline: record first fingerprint. |
| 6 | `FVC-110B` OnePlus private candidate preflight | READY after 5 | private input/signing/assets/build script | Trusted source, signing, and assets confirmed without exposing secrets; install only matching signature; redacted hash/version. | Missing input, mismatched signing, overwritten assets risk. |
| 7 | `FVC-040B` OnePlus one-turn loop | HUMAN after 6 | Conversation UI/minimal diagnostics | One preregistered non-private question: final → exactly one reply → offline TTS → `WAITING_FOLLOWUP` → zero recorder; one cancel causes no new call. Record only result/category/duration. | Same unchanged failure: stop spending tokens. |
| 8 | `FVC-050B` OnePlus two-turn/bounds | HUMAN after 7 | Conversation UI | Two reference turns, exactly two calls and `2/6`; Stop/End/model switch clear/release. | Reply reaches actions or ASR/TTS overlap. |
| 9 | `FVC-060B` OnePlus DSP L3 sample | HUMAN after 7 | OEM DSP/status/minimal diagnostics | Screen-off OEM wake → start-chat → question → one reply/TTS → DSP re-arm; CPU KWS OFF; zero recorder. | Call/charge/screen-on/incomplete sample: invalid, not pass. |
| 10 | `FVC-070B` physical call/focus/route matrix | HUMAN after 7 | calls/media/headsets | Call in Listening/Thinking/Speaking; focus loss and supported route changes; all stop/release/no auto-resume. | Repeat same failed call. |
| 11 | `FVC-080` Mandarin ASR selection | HUMAN after 7 | protocol/candidates/redacted summary | 30–50 preregistered samples, ≥3 speakers/distances/environments, each candidate once; select Conversation ASR from semantic success, latency, RSS/CPU, package delta. | Non-preregistered or repeated-until-pass speech. |
| 12 | `FVC-120` human product acceptance | HUMAN after 8–11 | evaluation form/phone | Quiet/room/distance/noise, references, five stops, recovery, call; natural Mandarin/state/no echo/no residual resources. | Critical failure: preserve it, no daily-use claim. |
| 13 | `FVC-130` release/rollback/docs | READY after 5; public release after 12 | docs/evidence/release artifacts | Bilingual L2/L3/experimental/action distinction; source-only SBOM/provenance/scan; AOSP upgrade/rollback/uninstall; review/merge. | Private material could enter Git or signing lacks independent verification. |

## 3. PR and human-assistance rules

Tasks 1–4 and 5 each get one PR. Task 6 is evidence-only if no code changes. A physical validation gets one evidence PR only after a changed condition. Task 13 is a final dedicated PR. Do not mix voice work with unrelated OpenCode/root/widget work.

Human help is needed only for tasks 7–12: ~3 minutes for one-turn/cancel, ~5 for two-turn/bounds, ~3 for unplugged screen-off DSP, ~10 for calls/routes, and 30–45 minutes for the preregistered ASR/hearing assessment. Outside those active tasks, do not request calls, repeated reading, persistent cabling, or screenshots.

## 4. Exact start prompt for the next executor

> Read `AGENTS.md`, `docs/free-voice-chat-executor-runbook.zh-CN.md`, `docs/free-voice-chat-delivery-plan.zh-CN.md`, `docs/execution-backlog.zh-CN.md`, and `STATUS.md`. Execute the first dependency-satisfied `READY` task in the queue, one task only. Preserve untracked `docs/articles/`. Test first, then implement; run `bash scripts/verify.sh`, relevant tests/builds, and `git diff --check`. Automated evidence never replaces OnePlus/HUMAN gates. Never commit or output secrets, private URLs, models, APKs, raw speech/transcripts, device data, or UI dumps; never repeat unchanged model calls, ADB experiments, screenshots, or calls. Update acceptance, `STATUS.md`, and backlog after each work package, then open a minimal PR; merge only after required CI and review. At HUMAN/physical gates, accurately record the needed action and stop that task.
