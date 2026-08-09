# M6 public-Beta hardening acceptance — partial

Date: 2026-08-09  
Candidate version: `0.2.0-alpha.1` (`versionCode=2`)

## Proven gates

- Debug and release variants are distinct. `aapt2 dump badging` showed `application-debuggable` only on the debug APK. A release build using `androiddebugkey` was refused. A non-debuggable locally test-signed APK was produced only with the explicit `XIAOHEI_ALLOW_TEST_SIGNING=1` escape hatch and is not a public Release.
- Release builds require external keystore path, alias, store password, and key password. No key or password is stored in Git.
- The CycloneDX 1.5 SBOM generator emits the app component, APK hash when supplied, and the optional sherpa-onnx/model bundle hash and unresolved model-license notice.
- Configuration schema migration v1 maps legacy endpoint/model names, removes legacy keys, copies no token plaintext, and is idempotent. The installed app created `config_schema=1`.
- On the exact latest offline-ASR debug candidate, the real-device harness completed 100 deterministic actions: 100 success, zero dispatcher failure, zero fatal/ANR signature, and zero active recording client.
- Repository verification passed with 21 required artifacts, three valid JSON schemas, valid local links, no forbidden binary, no detected credential, and no private path/device identifier.

## Open M6 gates

The candidate is not a public Beta yet. Required evidence still missing: 8–24 hour unplugged idle/power run, 20+ app/page matrix, exact release-candidate malware/dependency scan, production signing-key governance, model redistribution approval, upgrade/downgrade/backup/restore matrix, full diagnostic bundle, and a genuinely signed reproducible public APK.
