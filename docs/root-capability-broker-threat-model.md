# Root Capability Broker Threat Model

Status: design prerequisite for `ROOT-001`; no root executor is implemented by this document.

The model and OpenCode never receive `su -c`, an interactive root shell, credential paths, or a broad filesystem path. A future broker accepts only versioned action IDs, exact schemas, a one-use capability token, bounded timeout, audit record, and a defined rollback.

## Allowlist candidates

| Action family | Scope | Default |
|---|---|---|
| Read-only diagnostics | fixed service/package/port/battery/audio queries | preview then allow |
| Service lifecycle | named Xiaohei-owned service only | confirmation + verify PID/port |
| Device profile rollback | signed, versioned profile only | confirmation + reboot verification |

## Permanent denial

- Generic shell, arbitrary command strings, arbitrary paths, wildcards, pipes, redirection, downloads, package-manager install/uninstall, credential/key stores, payment/OTP/password surfaces, security bypasses, and exfiltration.
- Any request whose target differs after user confirmation or whose capability token is expired/replayed/cross-task.

## Recovery ownership

Every allowed mutation needs a preflight snapshot, a bounded rollback action, and a postcondition check. If rollback cannot be defined, the action is denied rather than delegated to a model.
