# Public release checklist

A public artifact must satisfy every item below. A locally signed acceptance APK is not a public release.

After generating the exact bundle, run `bash scripts/verify-release-bundle.sh /absolute/path/to/bundle` from the repository. It checks checksums in the correct bundle directory, scans the APK, and verifies the APK/SBOM/provenance binding before upload.

- Release variant has no `application-debuggable` flag and uses a non-debug signing key kept outside the repository; follow [signing governance](signing-governance.md).
- Version code/name are monotonic; APK SHA-256 and CycloneDX SBOM are published together.
- If the candidate embeds offline-ASR/KWS assets, their exact redistribution rights have been reviewed and recorded; see [model redistribution review](model-redistribution-review.md). A generic candidate with no embedded model must record that scope in provenance. Upstream code licensing alone is insufficient for a model-bearing binary.
- Generic APK and device-specific OnePlus enhancement are separate artifacts. Private OEM assets never enter Git, SBOM, APK, logs, or Release uploads unless redistribution rights are proven.
- Fresh install, upgrade preserving configuration, transactional rollback, downgrade behavior, and complete uninstall have current evidence.
- Notification and Accessibility permissions are disabled by default and explained before Android Settings is opened.
- Privacy notice, compatibility matrix, release notes, vulnerability reporting route, and known limitations are bilingual and current.
- 100-task stress, 20+ surface regression, 8–24 hour idle/power test, and malware/dependency scans pass on the exact release candidate.
