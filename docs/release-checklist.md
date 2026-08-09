# Public release checklist

A public artifact must satisfy every item below. A locally signed acceptance APK is not a public release.

- Release variant has no `application-debuggable` flag and uses a non-debug signing key kept outside the repository; follow [signing governance](signing-governance.md).
- Version code/name are monotonic; APK SHA-256 and CycloneDX SBOM are published together.
- Offline-ASR/KWS model redistribution rights have been reviewed and recorded; see [model redistribution review](model-redistribution-review.md). Upstream code licensing alone is insufficient.
- Generic APK and device-specific OnePlus enhancement are separate artifacts. Private OEM assets never enter Git, SBOM, APK, logs, or Release uploads unless redistribution rights are proven.
- Fresh install, upgrade preserving configuration, transactional rollback, downgrade behavior, and complete uninstall have current evidence.
- Notification and Accessibility permissions are disabled by default and explained before Android Settings is opened.
- Privacy notice, compatibility matrix, release notes, vulnerability reporting route, and known limitations are bilingual and current.
- 100-task stress, 20+ surface regression, 8–24 hour idle/power test, and malware/dependency scans pass on the exact release candidate.
