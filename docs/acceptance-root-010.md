# ROOT-010 independent-device lifecycle acceptance

Date: 2026-08-11

Independent device: clean Android 14 AOSP ARM64 virtual device (`emu64a` / `Android SDK built for arm64`), not the OnePlus 8T and with no Xiaohei DSP Companion or root profile installed.

## Packages and signatures

Two source-only, no-model debug packages were built from the same revision with only version metadata changed:

| Version code | Version name | SHA-256 |
|---:|---|---|
| 4 | `0.2.0-root010.4` | `c2effca2d74552da49609538dbcf4bffd8cb95e540dbebfe7384c28267132586` |
| 5 | `0.2.0-root010.5` | `72ea64cf13ccaedb61d9401d3b05a2f66d4d970bd4f6be75da5e95973866e701` |

Both passed APK Signature Scheme v2 and v3 verification.

## Device lifecycle

1. Confirmed the main package was absent, then installed code 4 successfully.
2. Installed code 5 with `adb install -r`; package manager reported `versionCode=5`.
3. Attempted ordinary code-5 → code-4 downgrade. Android correctly rejected it with `INSTALL_FAILED_VERSION_DOWNGRADE`.
4. Performed the explicit, owner-controlled maintenance downgrade with `adb install -r -d`; package manager reported `versionCode=4`.
5. Ran the repository transactional uninstall script. It removed the main package and Assistant role; afterwards `pm path` for Xiaohei and DSP Companion returned empty and AudioFlinger reported no active record client.

## Scope boundary

This completes the independent-device install/upgrade/rollback/uninstall prerequisite. It does not install, enable, or execute a root adapter, `su`, shell command, profile transaction, or root capability. The current product still has no real root adapter; future root execution remains separately gated by confirmation, bounded adapter design, and device evidence.

The two temporary APKs and build logs were used only for this acceptance and are not release assets or committed artifacts.

[简体中文](acceptance-root-010.zh-CN.md)
