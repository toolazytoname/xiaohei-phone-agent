# Transport and capability security boundary

[简体中文](transport-security-boundary.zh-CN.md) · [Conversation transport](conversation-transport.md) · [Loopback gateway](loopback-tool-gateway.md)

`SEC-003` records the current boundaries rather than asserting a completed network penetration test.

| Surface | Enforced local rule | Automated evidence | Remaining gate |
|---|---|---|---|
| Public model transport | Only HTTPS; Android network config disables cleartext by default. The client never installs a permissive trust manager or hostname verifier. | External HTTP, URL user info/query/fragment, header-newline injection, overflow and timeout cases reject. | Independent-device TLS interception with an untrusted CA, trusted user CA, expired cert, hostname mismatch and redirect attempt. |
| On-phone relay | HTTP is accepted only for `127.0.0.1`, `::1`, or `localhost`; loopback uses `NO_PROXY`, so Clash/VPN does not forward an internal request. | IPv6 loopback and external-cleartext transport cases pass. | Confirm the actual bound address/port and proxy behavior on the target phone. |
| Redirects | `HttpURLConnection` redirect following is disabled; any 3xx result is discarded without retry. | Exact 302 case maps to `REDIRECT_REJECTED`. | Real HTTPS-to-HTTP and cross-host redirect exercise. |
| Tool authorization | Gateway accepts numeric loopback peers only, equal non-negative UIDs, and a private memory receipt/token bound to one exact call. | Cross-UID/non-loopback, scope drift, expiry, clock rollback, replay, revocation and foreign-gateway matrices pass. | A real same-UID listener/adapter must derive peer evidence from its connection, not request JSON. |

No current transport performs certificate pinning. Standard Android TLS validation is intentionally retained; pinning would require certificate rotation and independent-device recovery evidence before it could be enabled. No automatic retry is allowed after any transport failure.
