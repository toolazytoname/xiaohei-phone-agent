# Deterministic offline Conversation FAQ

[简体中文](conversation-offline-faq.zh-CN.md) · [Execution backlog](execution-backlog.md) · [Status](../STATUS.md)

Status date: 2026-08-10. `CHAT-011` provides a deliberately small local fallback when a remote Conversation turn does not succeed. It is fixed application logic, not a generative model, and it cannot act on the phone.

## Exact behavior

1. Conversation first follows its normal bounded remote path.
2. Cancellation never falls back. After another remote/configuration failure, the app compares only the current user text with 25 exact Chinese/English help phrases.
3. Five topics are available: capability boundary, apparent offline state, Stop/Cancel, privacy/memory, and whether this is a local model.
4. A match returns a deterministic answer prefixed with `LOCAL FIXED FAQ | NOT A REMOTE MODEL`. The UI also says the remote turn did not succeed.
5. An unknown, action-seeking, combined, multiline, injected, or oversized question receives no local answer. The original remote/configuration error remains visible.

Terminal punctuation, case, and repeated spaces are normalized. Semantic similarity and fuzzy matching are intentionally absent: “How do I stop?” matches; “What does stop mean?” does not.

## Authority and privacy

Each local result has constant `modelCalls=0`, `actionCalls=0`, and `usesContext=false`. The fallback imports no Android, network, credential, model-client, command-router, tool-gateway, shell, or file APIs. It reads no prior turn and sends no extra request. If the remote path already attempted a request, that earlier attempt still counts; the fallback adds none.

A matched answer may be retained as a visibly labeled assistant turn inside the existing bounded, memory-only Conversation session. It clears under the same lock/background/end/turn/time/token rules and does not gain action authority.

## Why no bundled 0.6B model

The long-term architecture permits a small local model only for classification, fixed FAQ, privacy rewrite, and offline explanation. This task selects the fixed-FAQ option so the public APK gains a reproducible offline help path without committing weights, increasing APK size, adding a model license, or consuming phone memory. A future local-model adapter must remain separately selectable, visibly labeled, resource-bounded, and incapable of planning/actions.

Pure-Java tests cover 25 accepted aliases, 10 unknown/action/injection cases, oversized input, deterministic output, and zero authority. Static enforcement proves the failed-remote-only integration and that a disabled Conversation channel returns before any network thread is created. The APK builds with v2/v3 signatures.

On a fresh Android 14 AOSP emulator, the normal onboarding/main-screen path entered Conversation with the remote channel disabled. `what can you do` produced the visible remote-failure state and both local/non-model labels. An action-seeking unknown input produced only `Conversation channel disabled`; the foreground remained `ConversationActivity`, no other app opened, and logs contained no fatal/ANR. No mock server, network model, speech, screenshot, or physical OnePlus was used; the APK, data, and UI XML were removed afterward.
