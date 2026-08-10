# Root capability boundary

[简体中文](root-capability-boundary.zh-CN.md) · [threat model](threat-model.md) · [status](../STATUS.md)

Status: `ROOT-001` defines governance only. The phone may be rooted, but Xiaohei has **no product root broker** and generic `su -c`, shell, arbitrary path, system-partition write, boot image, credential-store, payment/OTP/password, destructive Git, or network-exfiltration authority remains denied.

## Future allowlist

Only three future fixed action IDs are cataloged: read service status, read battery status, and read audio status. They are metadata, not commands or permissions. A later broker must bind each ID to exact input/output schemas, signing identity, fresh local confirmation, a bounded timeout, redacted audit, and a revocation path.

## Threats and recovery ownership

| Threat | Required boundary | Recovery owner |
|---|---|---|
| Prompt/tool injection | No free-form shell; typed fixed action ID only | Product policy + owner review |
| Wrong target/system damage | Exact target schema, dry run, reversible plan | Human device owner |
| Credential/privacy loss | Permanent denial; no root file browsing | Human device owner |
| Boot failure / loss of access | Offline backup and independent-device validation before changes | Human device owner |
| Compromised token/app | Signature binding, short scope, global revoke | Future broker + owner |

No document or catalog entry upgrades Android, OpenCode, model, relay, or accessibility authority to root. Root implementation begins only at `ROOT-002` after `POLICY-003` creates separate authorization tiers.
