# OpenCode Task Proposal Protocol

[简体中文](opencode-task-protocol.zh-CN.md) · [Master plan](sovereign-mobile-agent-master-plan.md) · [Tool gateway](loopback-tool-gateway.md) · [Status](../STATUS.md)

Status date: 2026-08-10. `OC-002` defines a private, pending, dry-run proposal boundary between Xiaohei's typed complex-task request and a future OpenCode executor. It does not launch OpenCode, create a workspace, send a prompt, open a network connection, invoke Android/root, or execute a command.

## Narrow protocol

Only three reviewed task kinds can cross this boundary:

| Kind | Meaning now | Not granted |
|---|---|---|
| `project_summary` | Propose a project-structure summary | File access or a model call |
| `test_diagnosis` | Propose diagnosis of selected test evidence | Test execution or terminal access |
| `controlled_file_organization` | Propose future organization of explicitly scoped project files | Workspace creation, file mutation, or path access |

The proposal is created only from an existing `UnconfirmedActionRequest` that was derived from the current typed user turn and remains `high`, `pending`, `dry_run=true`, `requires_confirmation=true`, and non-public-log-safe. The protocol rebinds it to new `task_id` and `plan_id`, sets audience to `opencode_gateway`, and fixes `execution_state=not_started`.

```text
typed user complex request
        │ existing route + pending ActionRequest checks
        ▼
OpenCode task proposal (private, dry-run, pending)
        │ later: confirmation + workspace + bounded runner
        ▼
OpenCode execution — not implemented by OC-002
```

`opencode-task.v1` is deliberately closed. It has no `command`, `argv`, environment, current-directory, workspace, URL, token, root, process, or execution-result field. JSON is inspectable proposal metadata, never an execution capability. The private instruction is redacted from public-safe metadata; only its code-point count is safe to display.

## Acceptance evidence

The pure Java matrix covers three reviewed kinds, ten instruction-shaped attacks, six invalid source/kind/identity cases, immutable private metadata, and zero model/action/execution calls. Five public fixtures cover two valid proposals plus unknown-field command injection, forged source, and live-state rejection.

```sh
bash apps/android/xiaohei-android/test.sh
python3 scripts/verify-opencode-task-contract.py
python3 scripts/verify-opencode-task-boundary.py
bash scripts/verify.sh
```

## What remains

`OC-003` must allocate one constrained workspace and reject traversal, symlink escape, and cross-task reads. `OC-004` then adds a bounded runner; `OC-005` through `OC-007` add redacted progress, stop/cleanup, and restricted tools. This protocol cannot be treated as proof that OpenCode has run, that a file was read or changed, or that a user confirmation UI exists.
