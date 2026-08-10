# Fresh scope-bound confirmation

[简体中文](fresh-confirmation.zh-CN.md) · [Unconfirmed request](unconfirmed-action-request.md) · [Task Plan](rules-first-task-plan.md) · [Status](../STATUS.md)

Status date: 2026-08-10. `POLICY-002` adds a local, in-memory, one-use confirmation gate. Confirmation is a short-lived grant created only by an explicit local user gesture while Xiaohei is foreground, interactive, and unlocked. It is not a model response, a boolean in JSON, or a reusable capability token.

## Binding and lifecycle

A grant binds the exact:

- task ID, ActionRequest ID, and Plan ID;
- target and full displayed content;
- issue time and expiry on a caller-supplied monotonic clock;
- local-user-gesture source and eligible device state.

Target and content are stored only as SHA-256 digests salted by the unique confirmation ID. The grant stays in one process object and is never persisted. Status exposes only active/remaining-time/result; it carries no identifiers, target, content, or digest.

The TTL must be 1–60 seconds. Authorization succeeds once, consumes the grant, and performs no action itself. Exact expiry, clock rollback, target/content/identity change, invalid scope, lock, screen non-interactive state, backgrounding, cancellation, or a failed comparison destroys the grant. Returning to the old target cannot revive it.

## Model and authority boundary

The issue API takes a typed source. `ASSISTANT_TEXT` is rejected before grant construction, so JSON such as `confirmed=true`, a claimed tool result, or natural-language approval cannot confirm anything. The production class has no model client, text parser, Android call, tool gateway, shell, network, storage, or logging path.

`confirmation-grant.v1` is a public structural contract for the memory-only record. It contains salted digests but no raw target/content, is marked non-public-log-safe, and requires `source=local_user_gesture`, `single_use=true`, and `persistence=memory_only`. It is not the capability token defined for `TOOL-002`.

## Acceptance and remaining integration

The pure-Java matrix contains 50 groups/cases: ten exact allow-once flows, five target changes, five content changes, five task/request/plan changes, five expiry/clock failures, five device-state failures, ten assistant forgeries, and five missing/cancel/replay paths. Every outcome records zero model and action calls. Four public fixtures independently accept one fresh grant and reject assistant source, invalid window, and a raw-content field.

The gate is intentionally not wired to the current UI or executor. `UX-004` must present an accessible app/target/content/permission/rollback preview and call the fixed local gesture path; `TOOL-002` may later exchange one successful gate result for one scoped capability token. Until then, `ALLOW_ONCE` has no execution consumer.
