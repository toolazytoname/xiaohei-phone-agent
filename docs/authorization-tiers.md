# Authorization tiers

[简体中文](authorization-tiers.zh-CN.md) · [root boundary](root-capability-boundary.md) · [status](../STATUS.md)

`POLICY-003` separates Android, OpenCode, and root audiences. An Android credential can authorize only Android gateway metadata; an OpenCode credential can authorize only OpenCode gateway metadata; all root requests fail while the root broker is unimplemented. No lower-tier credential can upgrade across tiers.

This is a pure in-memory policy, not a token issuer, transport, UI, root broker, or execution path. `ROOT-002` must add signature-bound fixed actions only after this separation is retained.
