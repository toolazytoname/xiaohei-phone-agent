# Root profile transaction

[简体中文](root-profile-transaction.zh-CN.md) · [backup](root-encrypted-backup.md) · [status](../STATUS.md)

`ROOT-006` adds a fixed-profile transaction ledger: precheck exact profile identity and digest, snapshot a valid digest, mark an externally performed apply, reject rollback digest drift, then record a post-reboot verification state. Invalid ordering, identity, or digest is denied. The class does not install/uninstall a profile, access files, reboot, or call root.

The state machine is preparation for a future bounded adapter only. Real profile installation needs encrypted snapshot persistence, verified package/image provenance, explicit user confirmation, device reboot observation, rollback execution, and independent-device acceptance.
