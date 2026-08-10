# Root read-only diagnostics

[简体中文](root-read-only-diagnostics.zh-CN.md) · [broker](root-broker-core.md) · [status](../STATUS.md)

`ROOT-003` adds a pure projection boundary for the three existing fixed broker actions. `read_service_status` can return at most service, port, package, and profile availability; battery and audio actions each return one corresponding availability state. Every entry has only a fixed category, one of `available`/`unavailable`/`unknown`, and a fixed public label.

There are no command, path, PID, port number, package name, profile contents, battery value, audio content, user text, log text, token, or raw adapter-output fields. Missing source state becomes `unknown`; more than four entries cannot be represented. This class does not call the broker, `su`, shell, Android, network, filesystem, or device APIs. A future root adapter must first pass the exact broker action, map raw device observations privately to these states, redact its own audit record, and be accepted on an independent device.
