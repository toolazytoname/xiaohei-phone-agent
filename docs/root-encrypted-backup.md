# Root encrypted backup core

[简体中文](root-encrypted-backup.zh-CN.md) · [root boundary](root-capability-boundary.md) · [status](../STATUS.md)

`ROOT-005` adds an in-memory, fixed-scope AES-256-GCM envelope for at most 16 KiB of explicitly supplied backup metadata. It takes an injected 32-byte key, creates a fresh 12-byte IV, defensively copies the encrypted envelope, and rejects wrong keys, tampering, invalid envelopes, and invalid key sizes. The core has no file, directory, root, Android, token, network, or model API.

It does not create a real backup, persist key material, or prove plaintext residue deletion on a device. A future adapter must use a device-keystore/non-exportable key, fixed approved data selection, encrypted atomic storage, cleanup verification, restore transaction, and independent offline-media recovery acceptance. This task does not close `RELEASE-004`.
