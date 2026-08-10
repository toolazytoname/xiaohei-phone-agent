# Root broker core

[简体中文](root-broker-core.zh-CN.md) · [root boundary](root-capability-boundary.md) · [status](../STATUS.md)

`ROOT-002` adds an in-memory fixed-action authorization core only. It recognizes three read-only action IDs, requires the exact broker signer and empty parameter object, and consumes each request ID once. Missing fields, unknown signer, malformed request, non-exact parameters, and replay are denied.

It does not invoke root. There is no `su`, shell, Android API, transport, token persistence, root process, or device change. A future adapter must additionally bind a real signing implementation, fresh confirmation, schemas, and independent-device acceptance.
