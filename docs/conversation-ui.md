# Single-turn Conversation UI

Status date: 2026-08-10. This page records the `CHAT-004` implementation and its bounded AOSP-emulator acceptance. It is not evidence for speech recognition, TTS quality, a physical-device credential store, or model-driven actions.

## Product boundary

The screen accepts one text message and renders one model reply. It deliberately has no path to Android actions, notifications, files, root, OpenCode, the Phone Agent, or any other tool surface.

The user always sees:

- a bilingual zero-authority notice;
- an explicit idle, requesting, cancelling, completed, or failed state;
- separate Send and Cancel controls;
- selectable, display-only output.

The activity is not exported. Six stable accessibility descriptions support deterministic inspection without screenshots:

`conversation-authority-notice`, `conversation-state`, `conversation-input`, `conversation-send`, `conversation-cancel`, and `conversation-output`.

## Lifecycle and race safety

Only one request may own the screen. Starting a new request cancels the previous one. A generation-bound pending-call object handles cancellation before or after transport binding, synchronous completion, late callbacks, and activity destruction. A stale callback cannot replace a newer reply.

The input and Send control are disabled while a request is active. Cancel becomes disabled as soon as cancellation starts. Closing the activity cancels the active request.

## Reproducible checks

The repository gate performs two independent checks:

1. Pure-Java lifecycle tests cover normal completion, cancellation before and after binding, synchronous callback-before-bind, and stale-cancel cleanup.
2. A static boundary verifier confirms the six UI identifiers, `exported=false`, `action_authority=none`, and zero references from the Conversation UI/client to Android action dispatch, services, broadcasts, the Phone Agent, or the tool gateway.

Run:

```bash
bash apps/android/xiaohei-android/test.sh
python3 scripts/verify-conversation-ui-boundary.py
bash scripts/verify.sh
```

## AOSP-emulator acceptance record

On 2026-08-10, the current debug candidate was fresh-installed on the dedicated Android 14 ARM64 emulator. The normal onboarding and model-configuration screens were used to enable Conversation and point it at a host-loopback mock endpoint through an explicit ADB reverse mapping. The Phone Agent remained disabled.

From the public main screen, the tester opened **Xiaohei chat: single-turn text (no action authority)**, entered `hello`, and selected Send. The mock server received one streaming request and returned a bounded SSE reply. UI-tree inspection, without screenshots, observed:

- state: `Status: reply shown only`;
- output: `XIAOHEI_UI_MOCK_OK`;
- Send enabled, Cancel disabled after completion;
- no fatal exception or app ANR;
- no action-authority path in the shipped UI/client source.

The reverse mapping, test APK, app data, and mock process were then removed. The physical OnePlus device was not accessed by this acceptance run.

## What remains open

- `CHAT-002`: physical-device Keystore save/clear/restore acceptance;
- `CHAT-005`: human speech → text → model → intelligible, interruptible TTS;
- `CHAT-006`: bounded in-memory multi-turn session;
- `ROUTE-*`, `PLAN-*`, and `TOOL-*`: separately governed routing, planning, and action authority.

Model text must never become an action merely because it resembles JSON or a tool call. Action authority can only be added later through the versioned schema, policy, confirmation, and tool-gateway chain.
