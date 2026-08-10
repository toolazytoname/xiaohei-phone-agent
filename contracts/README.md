# Contracts / 数据契约

These JSON Schemas are versioned boundaries between Xiaohei components. They contain no provider credentials or OEM data and can be implemented by Android, Termux, or desktop test tools.

这些 JSON Schema 是小黑组件间的版本化边界，不包含 Provider 凭据或 OEM 数据，可由 Android、Termux 或桌面测试工具共同实现。

- `wakeword-event.v1.schema.json`: minimal event emitted after a wake hit; no raw audio.
- `action-request.v1.schema.json`: action lifecycle proposal with risk, confirmation, dry-run, and redaction metadata; new complex requests remain pending until separate policy/confirmation gates.
- `task-plan.v1.schema.json`: bounded dry-run DAG proposal with request binding, step/time budgets, tool version/risk, dependencies, and idempotency metadata.
- `confirmation-grant.v1.schema.json`: memory-only, one-use, local-user grant bound to task/request/plan and salted target/content digests; distinct from an execution capability token.
- `tool-catalog.v1.schema.json`: immutable reviewed metadata for version, risk, concrete input/output schemas, rollback declaration, audience, and timeout. Presence in the catalog grants no execution authority.
- `tool-input-*.v1.schema.json` and `tool-output-*.v1.schema.json`: closed per-tool payload boundaries referenced by the catalog; observation excludes text, trees, screenshots, and raw media, and outputs are not public-log safe.
- `conversation-session.v1.schema.json`: bounded memory-only chat state with explicit turn, token, and timeout budgets. It carries neither transcript content nor action authority.
- `tool-call.v1.schema.json`, `tool-result.v1.schema.json`, and `capability-token.v1.schema.json`: the model-independent boundary for a proposed tool call, observed result, and one-use short-lived authorization. These schemas contain no bearer secret.

`fixtures/conversation-session.v1/` contains public, synthetic boundary fixtures only. Run `python3 scripts/verify-conversation-session-contract.py` to validate their structural and cross-field limits without downloading a JSON Schema package.

`fixtures/action-request.v1/` likewise contains public synthetic pending-request fixtures. `python3 scripts/verify-action-request-contract.py` rejects contradictory pending/live, pending/no-confirmation, and unknown-field payloads without a third-party Schema package.

`fixtures/task-plan.v1/` contains public synthetic DAG fixtures. `python3 scripts/verify-task-plan-contract.py` performs rules-first catalog, budget, dependency, and cycle checks without granting execution authority.

`fixtures/confirmation-grant.v1/` contains synthetic non-private grant records. `python3 scripts/verify-confirmation-grant-contract.py` rejects assistant source, invalid windows, and raw content fields.

`fixtures/tool-catalog.v1/` contains one exact public built-in catalog and duplicate/version/missing-schema/rollback rejection cases. `python3 scripts/verify-tool-catalog-contract.py` also rejects dangling input/output schema references without downloading a JSON Schema package.
