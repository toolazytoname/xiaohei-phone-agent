# Release-signing governance

Status: internal pre-public-release policy. This document identifies a release certificate but never stores a private key, password, Keychain item, or filesystem location.

## Current release identity

| Field | Value |
|---|---|
| Subject | `CN=Xiaohei Release, O=toolazytoname, C=CN` |
| Key | RSA 4096 / SHA-256 with RSA |
| Certificate SHA-256 | `1c0cf5bf518c3b63037dae70388974551bb1f0f851084328a48af13ebcc12c07` |
| APK signature schemes | v2 and v3; v1 is intentionally disabled because `minSdk=26` |
| First internal release candidate | `0.2.0-alpha.2 (3)` SHA-256 `6ad593561125af22fb10161f286c2f15a906af8734db9c065d3b80b2ffd9a26a` |

The PKCS#12 keystore is outside the public repository. Its password is stored in the local macOS Keychain, not in shell history, Git, a release note, or an APK.

An `age`/X25519 recovery bundle can be prepared with `scripts/prepare-signing-recovery.sh`. The script exports only the public certificate beside the encrypted archive, records hashes without paths or aliases, and can perform a decrypt-and-hash drill when an identity file is supplied. A bundle on the signing Mac remains **staging**, not an offline copy; the gate closes only after the owner moves a verified copy and the recovery identity to separately controlled offline storage.

## Release procedure

1. Retrieve the release password only into the current process environment.
2. Build with `XIAOHEI_BUILD_VARIANT=release` and the external keystore/alias/password variables required by `build.sh`.
3. Run `scripts/verify-reproducible-build.sh` with identical model inputs and signing identity; it requires two byte-identical builds and invokes the exact APK static scan.
4. Publish the resulting SHA-256, SBOM, certificate fingerprint, source revision, model-input hashes, and known limitations together.
5. Do not install a differently signed release APK over a debug installation. Use a clean acceptance device or uninstall only after the configuration/rollback plan is recorded.

## Required before first public release

- Make an encrypted, offline recovery copy of the release keystore with an owner-controlled recovery process; record its existence privately, not in Git.
- Confirm model redistribution rights and attach the review to the release evidence.
- Complete the release checklist, including physical idle/power and independent malware-engine evidence.
- Keep release CI credentials in a secret store with least privilege; never print them in build logs.

## Compromise and rotation

If the private key, Keychain secret, or signing host is suspected compromised, stop publication, revoke CI access, preserve relevant audit evidence privately, and create a replacement identity. After a public APK exists, use an Android signing lineage/rotation plan so supported devices can validate the transition; document the old and new certificate fingerprints in the security advisory.

Related: [release checklist](release-checklist.md) · [threat model](threat-model.md) · [M6 acceptance](acceptance-m6.md)
