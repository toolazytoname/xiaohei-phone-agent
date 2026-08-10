# Unconfirmed ActionRequest boundary

[简体中文](unconfirmed-action-request.zh-CN.md) · [Clarification](intent-routing-clarification.md) · [Master plan](sovereign-mobile-agent-master-plan.md) · [Status](../STATUS.md)

Status date: 2026-08-10. `ROUTE-004` creates a versioned proposal for a clear complex task without granting execution authority. It is the bridge from a typed user conversation turn to later planning and policy, not a shortcut from model output to Android actions.

## Creation flow

```text
typed conversation message
  ├─ ASSISTANT → reject before interpreting text
  └─ USER
       ├─ ambiguous/incomplete → ask through ROUTE-003
       ├─ chat or deterministic short command → do not upgrade
       └─ explicit complex task → ActionRequest v1
                                  risk=high
                                  confirmation_state=pending
                                  requires_confirmation=true
                                  dry_run=true
                                  → stop
```

The factory receives a `MemoryConversationSession.Message`, not an untyped string. Assistant replies that resemble JSON, tool calls, policy results, or successful actions are rejected before routing. User content can include `"confirmation_state":"confirmed"`; it remains sensitive parameter text and cannot change locally assigned fields.

## Fixed authority boundary

New proposals use `target=local_service` and `action=plan_complex_task`. Because policy has not reviewed a target or tool yet, risk fails conservatively to `high`. Confirmation is always required, state is always `pending`, and execution mode is always dry-run. The immutable request has no confirmation transition and no execution method.

Raw user text is bounded to 2048 characters, marked `public_log_safe=false`, and listed as `parameters.user_text` in redaction metadata. Public-safe metadata exposes only schema/action/risk/state/dry-run and text length; it excludes text and request identity.

The public `action-request.v1` Schema now enforces that pending requests require later confirmation and remain dry-run. A `not_required` state must agree with `requires_confirmation=false`. Four public synthetic fixtures cover one valid pending request and rejection of live-pending, no-confirmation-pending, and unknown-field payloads.

## Acceptance and remaining work

The deterministic matrix contains 39 cases: ten complex user requests created as pending dry-run, ten assistant confirmation forgeries rejected, ten chats/short commands not upgraded, five ambiguous inputs returned to clarification, and four invalid metadata cases rejected. All paths make zero model and action calls.

This foundation is intentionally not wired to `MainActivity` or `ConversationActivity`. `PLAN-001` now defines the bounded dry-run plan output; `POLICY-002` must still define a fresh confirmation transition bound to task, target, content, expiry, and device state, while `PLAN-002` supplies the later minimal remote adapter. Until those gates exist, the request stops at `pending`.
