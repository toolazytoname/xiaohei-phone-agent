# Contracts / 数据契约

These JSON Schemas are versioned boundaries between Xiaohei components. They contain no provider credentials or OEM data and can be implemented by Android, Termux, or desktop test tools.

这些 JSON Schema 是小黑组件间的版本化边界，不包含 Provider 凭据或 OEM 数据，可由 Android、Termux 或桌面测试工具共同实现。

- `wakeword-event.v1.schema.json`: minimal event emitted after a wake hit; no raw audio.
- `action-request.v1.schema.json`: policy-reviewed action with risk, confirmation, dry-run, and redaction metadata.
- `conversation-session.v1.schema.json`: bounded memory-only chat state with explicit turn, token, and timeout budgets. It carries neither transcript content nor action authority.
- `tool-call.v1.schema.json`, `tool-result.v1.schema.json`, and `capability-token.v1.schema.json`: the model-independent boundary for a proposed tool call, observed result, and one-use short-lived authorization. These schemas contain no bearer secret.

`fixtures/conversation-session.v1/` contains public, synthetic boundary fixtures only. Run `python3 scripts/verify-conversation-session-contract.py` to validate their structural and cross-field limits without downloading a JSON Schema package.
