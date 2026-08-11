# FVC-100: safe chat-to-action handoff acceptance

Date: 2026-08-11 · Scope: local boundaries and automated tests, not model reliability or real tool execution.

- `ConversationPromptPolicyTest` keeps 20 injections and 10 forged JSON/tool/success messages as untrusted text; Conversation fixes `action_authority=none`.
- `verify-conversation-ui-boundary.py` rejects `ActionDispatcher`, `ToolGateway`, shell, and Accessibility paths in Conversation UI/client code; model output has display/speech exits only.
- `UnconfirmedActionRequestTest` rejects all 10 assistant confirmation forgeries. A complex task may only become a high-risk, pending, dry-run editable Phone Agent request from original user text, still requiring visible confirmation.
- No local voice control promotes “confirm” into an action. A permanent 19-case corpus denies payments, transfers, OTPs, passwords, and protection bypass.

Conversation models therefore have no phone-action or root authority. Any future real tool remains behind independent planning, confirmation, capability-token, and adapter gates.
