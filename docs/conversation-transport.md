# Bounded Conversation Transport

[简体中文](conversation-transport.zh-CN.md) · [Master plan](sovereign-mobile-agent-master-plan.md) · [Status](../STATUS.md)

`CHAT-003` defines the network boundary for open conversation. It returns text only and carries no tools, action requests, phone context, root authority, or OpenCode authority.

## Request boundary

- One turn accepts at most 4,096 UTF-16 code units; the serialized request is capped at 64 KiB.
- The OpenAI-compatible path is fixed at `<base>/chat/completions`, with `stream=true` and at most 512 output tokens.
- Public endpoints require HTTPS. Cleartext HTTP is allowed only for `127.0.0.1`, `::1`, or `localhost`.
- Loopback explicitly uses `NO_PROXY`, preventing Clash or a VPN from sending an on-phone internal service outward. Public HTTPS continues through the system network path.
- URL user info, query, fragment, redirects, and newline-bearing tokens are rejected. The independent Conversation Keystore slot supplies the Authorization header only.

## Response boundary

- `text/event-stream` is preferred. Accumulated text is accepted only after `[DONE]`; premature EOF discards the entire partial answer.
- If a compatible provider ignores streaming, a standard `application/json` response is accepted.
- Raw response data is capped at 64 KiB. Default connect and read timeouts are 7 and 15 seconds.
- Rate limit, other HTTP errors, redirects, timeout, overflow, truncation, empty response, parse error, and network error map to fixed credential-free categories.
- This layer never retries automatically. Cancellation disconnects the active request, and a request completes its callback at most once.

## Automated acceptance

A pure-Java suite covers eleven cases: successful SSE, JSON fallback, truncated SSE, HTTP 429, unfollowed 302, read timeout, cancellation during a blocked read, response overflow, rejected external cleartext, rejected header-injection configuration, and IPv6-loopback policy. It calls no real model, spends no model tokens, and records no user text or secrets.

```bash
bash apps/android/xiaohei-android/test.sh
bash apps/android/xiaohei-android/build.sh
```

This evidence completes the `CHAT-003` mock network boundary. It does not replace the `CHAT-004` user-interface path, the `CHAT-005` human voice loop, or final `CHAT-012` conversation acceptance.
