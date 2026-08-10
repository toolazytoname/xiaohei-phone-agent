# Root audit and revocation

[简体中文](root-audit-revocation.zh-CN.md) · [broker](root-broker-core.md) · [status](../STATUS.md)

`ROOT-008` records each in-memory broker decision as a redacted event with only a sequence number, fixed action identifier (or `unknown`), and fixed decision. It intentionally excludes request IDs, signer, parameters, paths, commands, timestamps, tokens, device output, and user content. The public schema contains only those safe fields.

`revokeAll()` clears the one-use request set and permanently closes that broker instance: every later request is recorded as `deny_revoked`. There is no re-enable path, token persistence, transport, root call, shell, or device operation. A future global-stop integration must call this method only after it is wired to the real broker, and needs independent-device acceptance.
