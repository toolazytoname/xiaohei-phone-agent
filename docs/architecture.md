# Architecture

[简体中文](architecture.zh-CN.md)

## Trust boundaries

```text
Always-on, no command audio     Wakeword Broker
Short-lived sensitive audio     Voice Gateway
Text and model exchange          Model adapters
Risk and authorization           Policy Engine
Device mutation                  Android Actions
User visibility and control      Xiaohei Android App
```

No component may silently expand another component's authority. A wake event authorizes only a short command session; an interpreted command does not authorize a high-risk action until policy and confirmation complete.

## Components

### Wakeword Broker

Defines one capability-based interface for app button, Quick Settings, shortcut/hardware intent, selected Android assistant, CPU KWS, and vendor DSP backends. Only the DSP adapter owns SoundTrigger attach/load/start/stop/unload. CPU KWS and manual/assistant invocation are never mislabeled as DSP.

### Voice Gateway

Opens a bounded post-wake audio session, applies VAD, invokes a replaceable ASR adapter, and closes audio promptly. It does not keep the microphone open between commands.

### Policy Engine

Converts text into a structured intent, selects an action adapter, assigns a risk level, requests confirmation when required, and records a redacted decision. Provider/model choice is an adapter detail, not part of authorization.

### Android Actions

Uses, in order of preference: public Android intents, notification access, accessibility-driven visible interaction, and an explicitly authorized local shell adapter. Private protocol impersonation is outside the product boundary.

### Android App

Shows `OFF / ARMING / ARMED / LISTENING / THINKING / CONFIRMING / ACTING / ERROR`, permissions, selected model profile, action preview, and a local redacted history. Wakeword, model selection, and optional remote control remain independent controls.

## Versioned contracts

- `wakeword-event.v1.schema.json`: no raw audio; describes the source, keyword alias, confidence, and capture boundary.
- `action-request.v1.schema.json`: describes target, action, risk, confirmation, dry-run state, and redaction policy.
- `task-plan.v1.schema.json`: describes a request-bound, 1–8-step dry-run DAG with tool/risk, dependency, idempotency, and timeout limits; it grants no execution authority.
- `confirmation-grant.v1.schema.json`: describes a one-use memory-only local gesture bound to task/request/plan, target/content digests, and a 1–60 second monotonic window; it is not a capability token.
- `tool-catalog.v1.schema.json`: describes immutable reviewed tool metadata, concrete closed input/output schemas, rollback declaration, audience, and timeout. Catalog membership is not execution authority; see the [versioned catalog boundary](versioned-tool-catalog.md).
- `tool-call.v1.schema.json` and `capability-token.v1.schema.json`: bind one call, including its catalog-capped timeout, to task/request/plan/call/catalog scope and a 1–30 second in-memory capability. Numeric-loopback/same-UID authorization is documented in the [gateway boundary](loopback-tool-gateway.md).
- `tool-result.v1.schema.json`: represents one private, structured zero-or-one-adapter outcome with monotonic timing and bounded output. The pure coordinator, typed cancellation/errors, and explicit test-only adapter boundary are documented in the [execution lifecycle](tool-execution-lifecycle.md).

Runtime payloads may contain private user data in memory, but fixtures and public acceptance reports must be redacted before storage or publication.

## Integration rules

- `android-ai-stack` supplies provider profiles and AI runtime status through an adapter; Xiaohei never reads a private CC Switch database directly.
- `android-device-test` validates UI and device behavior, but product-specific selectors and expected actions remain here.
- `pocket-pentest` supplies authorized device capabilities; it does not become a product runtime dependency for ordinary users.
- `happy-relay-deploy` is optional. Local wake, policy, and Android actions must remain usable without a remote server.
- `oneplus-8t-mobile-lab` links tested combinations and learning material rather than vendoring this repository.

## Compatibility rule

The generic Android application must never load a vendor DSP library merely because the SoC family looks similar. It selects a backend only after matching an explicit device/ROM profile and completing read-only capability checks. Tier A invocation is always available even when all always-on backends are unavailable.
