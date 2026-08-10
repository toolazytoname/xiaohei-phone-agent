# Minimal planner request

`MinimalPlannerRequest` is the fixed envelope reserved for a future remote planning adapter. Its only instance fields are `action`, `dryRun`, `stepBudget`, `timeoutMs`, and `catalogVersion`.

It has no user text, UI data, local paths, images, request identity, or credentials. It also has no network, tool, or Android execution capability. Bounds are shared with `TaskPlanValidator`: 1–8 steps and 1,000–60,000 ms.

This is deliberately not a claim that a remote planner is wired today. PLAN-002 remains `VERIFY` until a real adapter is proven to transmit this envelope and only this envelope.
