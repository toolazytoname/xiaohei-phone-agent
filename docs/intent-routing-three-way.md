# Three-way intent classification

[简体中文](intent-routing-three-way.zh-CN.md) · [Execution backlog](execution-backlog.md) · [Status](../STATUS.md)

Status date: 2026-08-10. `ROUTE-002` adds a pure, inert classifier for three destinations: ordinary chat, an existing deterministic short command, or a complex task that will eventually require planning. Classification never starts a model or Android action.

## Rules-first order

1. Exact notification queries/message-draft forms already recognized by `CommandRouter` remain deterministic commands, but conceptual text such as “What does reply message mean?” is not trusted merely because the older router found keywords.
2. Explicit multi-step markers such as “then”, “after that”, “and also”, or their Chinese equivalents classify as complex tasks. No task is planned or executed here.
3. Other deterministic commands require both a reviewed `CommandRouter` result and an imperative cue such as Open, Close, Navigate, Show, or a bounded equivalent.
4. Ambiguous actions such as “open Gallery and Camera” are intentionally inert and temporarily return the chat/non-action route. `ROUTE-003` will turn low confidence into an explicit clarification instead of guessing.
5. Explicit organization/search/compare/sync/report/batch cues classify as complex. High-risk requests may enter the complex category, but later policy must deny them; category does not imply authorization.
6. Everything else defaults to chat.

Every result records constant `modelCalls=0` and `actionCalls=0`. Non-command results carry `CommandRouter.Action.UNKNOWN`, so downstream code cannot accidentally reuse a keyword-derived Android action.

## Acceptance

The synthetic, non-private matrix contains exactly 100 texts: 40 deterministic commands, 35 chats, and 25 complex tasks. It includes conceptual phrases containing command words, one ambiguous two-target command, multi-step requests, English/Chinese variants, and a high-risk transfer request. All classifications match expected routes; all results have zero side effects.

Static enforcement rejects any classifier reference to Android APIs, action dispatch, model clients, tool gateways, shell, or network. It also confirms `MainActivity` does not use the classifier yet. `ROUTE-003` and `ROUTE-004` now provide clarification and unconfirmed-request foundations; user-path integration still waits for planning, policy, and fresh-confirmation gates.
