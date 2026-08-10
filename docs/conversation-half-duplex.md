# Bounded half-duplex Conversation

Status date: 2026-08-10. `CHAT-007` connects the memory-only context boundary to the visible model-request path. It is a text conversation feature with zero action authority, not the speech loop or Phone Agent.

## User behavior

The public main screen opens a bilingual **6-turn half-duplex** dialog. Its fixed budgets are:

- six completed user/assistant turns;
- 2048 conservatively estimated transcript tokens;
- five minutes total from the first accepted input;
- one in-flight model request.

While the model is replying, the input and Send controls are disabled and Cancel is enabled. A failed or cancelled request rolls back its pending user message; the next request does not inherit a phantom turn.

The screen shows the completed-turn and estimated-token counters. It stores visible text only in the Activity instance and never writes it to saved state, preferences, files, a database, logs, diagnostics, or a static collection.

## Context sent to the model

The client accepts only an odd, alternating sequence that starts and ends with a user message, has at most 16 messages, bounds each message, and stays within the contract token ceiling. It prepends the existing zero-tool system instruction and submits the immutable snapshot from `MemoryConversationSession`.

Model text is still display-only. JSON, a claimed tool call, or an instruction-shaped reply cannot reach Android actions, services, broadcasts, the Phone Agent, OpenCode, or root.

## Clearing rules

The request and all Activity/session-owned text are cleared when:

- the user presses **End chat**, or enters an exact bilingual end command;
- the six-turn, token, or five-minute budget ends;
- the Conversation endpoint/model/enabled profile changes;
- the device locks or the Activity leaves the foreground;
- the Activity is destroyed.

A profile change stops at a visible boundary and asks the user to send again, so the old context is never silently sent to the new model. Exact end-command matching avoids treating a sentence that merely contains “end” as a destructive request.

## Deterministic coverage

Eleven pure-Java coordinator cases prove:

- a referential follow-up receives the preceding user/assistant pair;
- exact Chinese and English end commands send no network request;
- deadline, model switch, lock, and background clear the session;
- the half-duplex busy state rejects a second input;
- failed-request rollback permits a changed retry;
- a 3-turn test window clears at its exact limit;
- an invalid assistant reply closes with a distinct reason;
- public status has no `String` field;
- configured follow-up windows outside 3–8 turns are rejected.

The repository UI boundary gate additionally confirms the seven stable accessibility descriptions, public main-screen label, 6-turn/5-minute defaults, lifecycle hooks, non-exported Activity, and zero action paths.

## AOSP-emulator user-path acceptance

On 2026-08-10, the debug candidate was fresh-installed on the dedicated Android 14 ARM64 emulator and configured through the normal onboarding/model-channel UI for a host-loopback streaming mock. The Phone Agent remained disabled.

The first input was `remember_xiaohei`; the UI displayed `FIRST_CONTEXT_OK` at turn `1/6`. The second input was `what_was_name`. The mock returned `REFERENCE_CONTEXT_OK` only after verifying the exact request roles `system, user, assistant, user` and the prior pair. The UI displayed both turns and `2/6`.

Pressing **End chat** then changed the visible state to “Chat ended and cleared”; UI-tree inspection found only the empty-context notice and none of the previous transcript. Both server-side checks reported `context_ok=true`; Android logs had no app fatal exception or ANR. No screenshot or real model call was used.

The reverse mapping, test APK/data, and mock process were removed. The physical OnePlus device was not accessed.

## Remaining scope

Prompt minimization and adversarial prompt/tool-forgery tests are covered by `CHAT-008`. [Zero-call local controls](conversation-local-controls.md) are covered by `CHAT-009`. Speech input/TTS remains `CHAT-005` and final human conversation acceptance remains `CHAT-012`.
