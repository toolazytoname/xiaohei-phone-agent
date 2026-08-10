# Read-only task card

[简体中文](task-card.zh-CN.md) · [Task plan](rules-first-task-plan.md) · [OpenCode progress](opencode-progress-card.md)

UX-003 adds a public, read-only task-card projection. It shows only an approved target summary, reviewed step count/current step, time and step budget, fixed result category, and takeover state. It cannot accept task prose, task/request/plan identifiers, paths, tokens, model replies, terminal output, or reasoning.

The default card says no reviewed task exists and starts nothing. A future adapter may supply a card only after the existing task-plan/policy/confirmation boundaries approve it; stopping or takeover must not cause automatic retry. The current UI does not claim a real planner, OpenCode process, or tool execution exists.
