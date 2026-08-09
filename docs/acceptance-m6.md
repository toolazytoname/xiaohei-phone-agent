# M6 public-Beta hardening acceptance — partial

Date: 2026-08-09  
Stress baseline: `0.2.0-alpha.1` (`versionCode=2`); current incremental candidate: `0.2.0-alpha.2` (`versionCode=3`)

## Proven gates

- Debug and release variants are distinct. `aapt2 dump badging` showed `application-debuggable` only on the debug APK. A release build using `androiddebugkey` was refused. A non-debuggable locally test-signed APK was produced only with the explicit `XIAOHEI_ALLOW_TEST_SIGNING=1` escape hatch and is not a public Release.
- Release builds require external keystore path, alias, store password, and key password. No key or password is stored in Git.
- The CycloneDX 1.5 SBOM generator emits the app component, APK hash when supplied, and the optional sherpa-onnx/model bundle hash and unresolved model-license notice.
- Configuration schema migration v1 maps legacy endpoint/model names, removes legacy keys, copies no token plaintext, and is idempotent. The installed app created `config_schema=1`.
- On the exact latest offline-ASR debug candidate, the real-device harness completed 100 deterministic actions: 100 success, zero dispatcher failure, zero fatal/ANR signature, and zero active recording client.
- Repository verification passed with 21 required artifacts, three valid JSON schemas, valid local links, no forbidden binary, no detected credential, and no private path/device identifier.
- Alpha.2 (`versionCode=3`) adds a bilingual threat model and an exact-APK static gate. The combined candidate passed signing, safe ZIP paths, credential signatures, permission allowlist, exported-component permission gates, and native-library inventory; SHA-256 was recorded. CycloneDX now covers the app plus pinned ASR and KWS bundles.
- Real-device migration passed: normal code 2→3 upgrade preserved config schema v1; normal 3→2 downgrade was rejected by Android; explicit maintenance downgrade preserved config; re-upgrade restored code 3 and retained the Assistant role.
- Transactional uninstall from code 3 stopped/verified DSP `DETACHED`, removed the app and Assistant role, and preserved the Companion. Fresh install restored code 3 and the Assistant role with Accessibility disabled.
- The reusable surface harness opened 14 Android Settings surfaces and 11 system apps on the physical phone: 25/25 expected package or documented system permission/Safety Center interstitials, zero launch failure, and zero Xiaohei Fatal/ANR. It did not auto-grant Contacts or Clock permissions.
- Fresh alpha.2 exposed and fixed a first-install camera permission issue caused by declaring CAMERA for torch support. Denial produced zero action and a recoverable state. With the stress harness temporarily granting and then restoring CAMERA, the exact alpha.2 candidate passed 100/100 actions, zero failures, zero Fatal/ANR, and zero recording residue.

## Open M6 gates

The candidate is not a public Beta yet. Required evidence still missing: 8–24 hour unplugged idle/power, external malware/dependency review beyond the local static gate, production signing-key governance, model redistribution approval, backup/restore coverage, full diagnostic bundle, and a genuinely production-signed reproducible APK. The 25-surface launch matrix does not replace the separate 10–15-app Phone Agent task matrix.
