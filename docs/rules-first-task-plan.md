# Rules-first Task Plan v1

[简体中文](rules-first-task-plan.zh-CN.md) · [Unconfirmed request](unconfirmed-action-request.md) · [Master plan](sovereign-mobile-agent-master-plan.md) · [Status](../STATUS.md)

Status date: 2026-08-10. `PLAN-001` replaces the old order-only step check with a versioned, bounded DAG contract and a pure local validator. A plan is still a proposal: validation does not call a model, execute a tool, touch Android, or grant confirmation.

## Contract

`task-plan.v1` binds a `plan_id` to the originating `request_id` and requires:

- `dry_run=true` and `public_log_safe=false`;
- a 1–8 step budget and no more than eight actual steps;
- a 1–60 second total timeout;
- tool version 1, exact catalog risk, bounded string arguments, and a unique 16–128 character idempotency key for every step;
- unique step IDs and a dependency list that names only steps in the same plan;
- an acyclic dependency graph.

Forward dependencies are structurally valid: list order is display order, not authorization or execution order. A real visiting/visited graph traversal detects self-cycles and multi-node cycles. The plan and each step defensively copy their lists/maps and expose immutable views.

## Rules-first boundary

The validator checks the current [five-entry reviewed versioned catalog](versioned-tool-catalog.md): Open Settings, Open Gallery, Open Dialer, Adjust Volume, and Observe. An unknown name such as `root.shell`, `android.tap`, or `opencode.run` is denied, as is a known tool with a mismatched risk. This does not pre-approve the five tools for execution; it only proves that a proposed plan uses known names and declared risks.

Tool execution, Android services, model clients, and network code are absent. `TaskPlanValidator` is not referenced by the main, Conversation, or existing Phone Agent activity. `POLICY-002` now supplies a separate fresh-confirmation foundation; `PLAN-002` still defines the minimal remote adapter, while capability tokens and the tool gateway remain independent later gates.

## Acceptance

The pure-Java matrix contains 34 cases: ten valid DAGs (including a forward-reference branch and the eight-step boundary), five unknown tools, two zero/over-budget plans, five genuine cycles, and twelve malformed/risk/version/dependency/argument cases. Every result records zero model and action calls.

Five public synthetic JSON fixtures independently exercise two valid DAGs plus unknown-tool, nine-step, and cycle rejection. Schema and semantic fixture validation require no downloaded JSON Schema library.
