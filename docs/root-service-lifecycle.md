# Root service lifecycle preflight

[简体中文](root-service-lifecycle.zh-CN.md) · [diagnostics](root-read-only-diagnostics.md) · [status](../STATUS.md)

`ROOT-004` accepts only a freshly confirmed `stop` dry-run, and only when expected and observed package name, process name, PID, and port all match exactly. Missing data, `start`, stale/missing confirmation, or any mismatch is denied. The preflight never signals a PID, starts/stops a service, opens a port, invokes root, or reads a device.

The contract is deliberately not public-log-safe because a real adapter would carry target metadata. Future execution needs a separately approved signed request, private audit, bounded timeout, post-stop absence check, rollback plan, and independent-device acceptance. This policy alone grants no lifecycle authority.
