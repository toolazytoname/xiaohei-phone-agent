# Loopback Tool Gateway Authorization Core

[简体中文](loopback-tool-gateway.zh-CN.md) · [Tool Catalog](versioned-tool-catalog.md) · [Fresh Confirmation](fresh-confirmation.md) · [Status](../STATUS.md)

Status date: 2026-08-10. `TOOL-002` adds a pure in-process authorization core between one consumed local confirmation and one exact tool call. It does not open a socket, parse model text, invoke an Android adapter, or report an action as successful. A later transport must derive peer evidence from its accepted local connection; peer fields must never be copied from request JSON.

## Two one-use exchanges

1. `FreshConfirmationGate` accepts an eligible foreground/unlocked local user gesture and, after exact task/request/plan/target/content comparison, returns `ALLOW_ONCE` with a private capability receipt.
2. Only the gateway can consume that receipt. It checks peer and call metadata first, then exchanges the receipt once for an opaque in-memory token. Reusing the same result returns `CONFIRMATION_REPLAY`.
3. The token authorizes one call whose full canonical scope matches its salted SHA-256 call digest. Success, expiry, clock rollback, local scope/catalog failure, idempotency replay, or explicit global revocation removes the active token.

The receipt carries only confirmation/task/request/plan IDs. It contains no target, content, UI text, screenshot, accessibility tree, or digest. A confirmation whose task/request/plan differs from the call is consumed and rejected.

## Peer and token boundary

Both local and remote endpoint evidence must be numeric IPv4 loopback in `127.0.0.0/8` or IPv6 `::1`; wildcard addresses, LAN/public addresses, `localhost`, missing values, and other IPv6 addresses are rejected. The transport-reported peer UID must equal the gateway owner UID and both must be non-negative.

The default issuer creates a 128-bit `SecureRandom` token ID. A token is:

- memory-only, single-use, non-public-log-safe, and valid for 1–30 seconds on a caller-supplied monotonic clock;
- bound to confirmation, task, ActionRequest, plan, call, tool, version, risk, audience, arguments, and idempotency key;
- stored only in the issuing gateway registry, with at most 16 active tokens and 256 fail-closed replay records;
- unusable in another gateway instance, after exact expiry, after clock rollback, after any local scope change, or after `revokeAll()`.

The JSON capability schema is inspectable metadata, not a self-authenticating bearer credential. Reconstructing or editing JSON cannot create the private runtime `Token` or registry entry. The call digest detects scope drift; it is not a signature and does not replace the memory registry or peer checks.

## Contract and acceptance

`tool-call.v1` now carries task/request/plan/call identity, reviewed catalog scope, bounded string arguments, a 16–128 character idempotency key, monotonic request time, and `public_log_safe=false`. `capability-token.v1` adds the matching identities, confirmation ID, exact call digest, 1–30 second monotonic window, `single_use=true`, and `persistence=memory_only`. Tool results are also non-public-log-safe by default.

The pure-Java matrix covers 50 groups: ten exact one-use authorizations, ten non-loopback/cross-UID peers, five missing/forged/replayed/mismatched confirmations, seven call-scope changes, three catalog changes, five malformed/future/stale/private-metadata call cases, five expiry/clock cases, and five replay/revocation/foreign-gateway cases. Every result records zero model, action, and execution calls. Seven public fixture files independently cover one valid call/token pair, four invalid token cases, and six peer cases.

```sh
bash apps/android/xiaohei-android/test.sh
python3 scripts/verify-tool-gateway-contract.py
python3 scripts/verify-loopback-tool-gateway-boundary.py
bash scripts/verify.sh
```

## Remaining execution work

The current Android UI and activities do not reference `ToolGateway`; there is no socket listener or adapter invocation. `TOOL-003` must add structured timeout/cancel/idempotency execution results, while later integration must supply trusted transport-derived peer evidence and an accessible confirmation UI. OpenCode and root retain independent audiences and cannot use an Android capability.
