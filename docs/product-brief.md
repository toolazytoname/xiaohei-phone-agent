# Product brief

[简体中文](product-brief.zh-CN.md)

## Positioning

Xiaohei is the trustworthy action layer between an Android user and their local or remote AI tools. It is not another chat window: it wakes on demand, understands one bounded command, explains the intended action, obtains confirmation when necessary, and operates the phone through observable Android mechanisms.

## Primary audience

- Android power users who want a personal assistant they can inspect and control.
- Developers building device-local voice and agent experiences.
- Researchers validating low-power wake word, on-device AI, and user-like UI automation on real hardware.

## Distribution strategy

The public product starts with a generic Android core, not the OnePlus DSP integration. Every downloadable build must offer at least a manual, Quick Settings, shortcut, or supported assistant invocation. Device-specific low-power wake word support is installed as a capability-checked backend.

This separates two claims:

- Xiaohei's command, policy, and Android action experience can be broadly portable.
- Always-on low-power DSP wake is a hardware/firmware/system integration and needs an explicit device profile.

## First product slice

Tap or invoke Xiaohei, then say: “Open the gallery.”

This slice is intentionally narrow. It must demonstrate short command capture, ASR, structured intent, low-risk policy decision, Android action, and visible result on ordinary Android. Screen-off DSP wake is an additional acceptance path on supported device profiles, not a prerequisite for the generic alpha.

## Product-owned responsibilities

- Wakeword Broker lifecycle and user-facing state.
- Short voice-command session and ASR adapter contract.
- Intent, policy, confirmation, and audit decisions.
- Android action registry and result presentation.
- Product UI, onboarding, status, permissions, and local history.

## Explicit non-goals

- Reimplementing OpenCode, Claude, Happy, llama.cpp, or provider configuration.
- Shipping proprietary wake-word models, OEM applications, platform keys, or model weights.
- Impersonating private app protocols or bypassing platform/app security controls.
- Sending messages, deleting data, purchasing, or changing security settings without explicit user confirmation.
- Claiming broad device compatibility from a single OnePlus 8T validation.

## Success criteria for the first public alpha

1. Fresh-install instructions work on at least one unrooted generic Android profile and the declared OnePlus research profile.
2. Base invocation works even when no DSP backend is available.
3. Each wake backend reports `available`, `unsupported`, `permission_required`, or `ready` before activation.
4. “Open gallery” succeeds and produces a structured, redacted acceptance report.
5. The OnePlus DSP backend separately passes physical-unplug power and screen-off regression.
6. Stop, uninstall, reboot, permission denial, and dependency failure all have tested recovery paths.
