# Zero-call local Conversation controls

Status date: 2026-08-10. `CHAT-009` adds explicit Stop, Repeat, Clear, Continue, and End controls to the half-duplex dialog. These controls are local state transitions and never construct a prompt envelope or model request.

## Controls

| Control | Local effect | Context effect | Model calls |
|---|---|---|---:|
| Stop | Cancels one in-flight request, if any, and pauses input | Keeps completed turns | 0 |
| Repeat | Re-displays the last assistant reply | Does not add a turn | 0 |
| Clear | Cancels, clears visible/session-owned text, disables input | Removes context | 0 |
| Continue | Leaves pause/cleared state and re-enables input | Keeps paused context or starts empty | 0 |
| End | Cancels and clears like a terminal Clear | Removes context | 0 |

After Clear or End, the user presses Continue before starting a new session. This makes the empty/active boundary visible instead of silently submitting the next text.

Repeat is display-only in the current text product. A future TTS adapter may speak the same locally held reply, but it must not ask the Conversation model again.

## Buttons and recognized text

Four independent buttons expose Stop, Repeat, Clear, and Continue; End remains a separate terminal button. The same exact-match parser accepts 23 Chinese/English control phrases, so a future conversation-ASR final transcript can enter the identical local path.

Matching is intentionally exact after case/terminal-punctuation normalization. “停止” is a control; “停止是什么意思” is ordinary conversation. This avoids destroying context because a user mentioned a control word inside a question.

This task proves parser compatibility with ASR text, not acoustic voice recognition. Real microphone/ASR/TTS behavior remains under the `VOICE-*`, `CHAT-005`, and `CHAT-012` gates.

## Idempotency

The pure-Java state machine records `ACTIVE`, `PAUSED`, or `CLEARED`, plus request and last-reply availability. Every control outcome has a constant `modelCalls=0`.

- A second Stop does not cancel twice.
- A second Clear or End changes no state and restores no text.
- A second Continue changes no state.
- Repeat may be requested again, but never mutates the conversation or adds a model turn.
- Paused/Cleared state rejects Send until Continue.

Nine deterministic groups cover 23 exact phrases, eight non-control substrings, cancel-once behavior, repeat availability, clear/end forgetting, continue, paused send denial, and all five zero-call outcomes.

## AOSP-emulator user-path acceptance

On 2026-08-10, a fresh Android 14 ARM64 emulator install used the normal onboarding/config/main-screen path and one host-loopback SSE mock request. The UI displayed `CONTROL_BASE_OK`; the server count was exactly one.

The tester then selected Repeat, Stop, Continue, and Clear. UI-tree inspection observed the local repeated reply, paused input, restored input, and finally an empty-context state with Repeat disabled. The mock server count remained exactly one after all four controls, and Android logs contained no app fatal exception or ANR.

The reverse mapping, test APK/data, and mock process were removed. No real model, screenshot, speech, or physical OnePlus access was used.
