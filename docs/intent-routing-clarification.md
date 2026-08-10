# Low-confidence clarification

[简体中文](intent-routing-clarification.zh-CN.md) · [Three-way routing](intent-routing-three-way.md) · [Execution backlog](execution-backlog.md) · [Status](../STATUS.md)

Status date: 2026-08-10. `ROUTE-003` adds a pure, local clarification policy between raw text and the inert three-way classifier. A low-confidence phrase becomes a question; it never becomes a guessed Android action, model call, or tool call.

## Decisions

| Kind | Example | Result |
|---|---|---|
| `ASK_TARGET` | “Open Gallery and Camera” | Ask the user to choose exactly one target. |
| `ASK_INTENT` | “Gallery” | Ask whether the user wants a phone action or a discussion. |
| `ASK_SCOPE` | “Help me” | Ask for the missing target and desired result. |
| `ROUTE` | “Open Gallery” or “What is Gallery?” | Return the existing deterministic-command/chat/complex-task classification unchanged. |

Every ask result is forced to `CHAT` plus `CommandRouter.Action.UNKNOWN`. Its prompt states that nothing was guessed or executed. Every result records constant `modelCalls=0` and `actionCalls=0`.

## Deliberately bounded rules

- Multiple reviewed action targets trigger target clarification.
- A small exact set of incomplete phrases triggers scope clarification.
- Bare, action-related topics and non-conceptual phrases that an older keyword router would treat as actions trigger intent clarification.
- Conceptual cues such as “what”, “why”, “how”, “什么意思”, or “为什么” keep questions as chat.
- Clear deterministic commands and clear complex requests are not blocked here. Classification does not authorize a high-risk request; later policy must still deny it.

This is a conservative command boundary, not general natural-language understanding. It intentionally avoids fuzzy matching and does not send unclear text to a model to decide whether an action is safe.

## Acceptance and integration boundary

The synthetic, non-private matrix contains 50 texts: ten target ambiguities, ten intent ambiguities, ten incomplete scopes, and twenty clear controls. All 30 ambiguous cases ask with an unknown command and zero side effects; all 20 clear cases remain routable. Static enforcement rejects Android APIs, action dispatch, model clients, tool gateways, shell, and network.

`RouteClarificationPolicy` is intentionally not referenced by `MainActivity` yet. `ROUTE-004` now provides a versioned, unconfirmed `ActionRequest`; a later integration must still wait for bounded planning, policy, and fresh confirmation before connecting routing to a user-visible action flow.
