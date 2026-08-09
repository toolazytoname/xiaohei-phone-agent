# 0.2.0-alpha.3 release scope

[简体中文](release-scope-0.2.0-alpha.3.zh-CN.md)

Status: candidate scope frozen; public upload is still blocked by the gates listed below.

## Public source and generic APK

The publishable candidate is one non-debuggable `arm64-v8a` generic Android APK built from this repository without embedded ASR or KWS model assets. It supports explicit app/Quick Settings/Assistant entry where the device permits it, deterministic actions, optional notification summaries and confirmed drafts, the visible policy-gated Phone Agent, global stop, diagnostics, and configuration migration.

The generic APK does not promise offline speech recognition on devices without a compatible system recognizer. It does not contain or install a OnePlus DSP companion, OEM model, root module, provider credential, private endpoint, conversation content, or device identifier.

## Local acceptance package

The approximately 64 MB combined-ASR/KWS package is a private, locally built acceptance artifact for the owner's OnePlus 8T and the AOSP profile. It is not uploaded to GitHub Releases while exact model redistribution rights remain unresolved. Its CPU “Xiaohei Xiaohei” mode is experimental, visible, disabled by default, and not a DSP claim.

## Device enhancement

The OnePlus 8T DSP companion/profile remains a separate source and locally built artifact. Its current evidence covers the exact OnePlus 8T / LineageOS 21 profile and the validated OEM phrase only. It is neither bundled into the generic APK nor advertised for arbitrary Android devices.

## Publication gates

Before any APK is uploaded as a public Alpha, the exact generic candidate must have: production/release signature provenance, CycloneDX SBOM and checksums, independent malware scan, fresh install and rollback evidence, and the current bilingual release/security documents. Physical idle/power still blocks low-power and endurance claims, but does not convert the generic package into a DSP artifact. Embedded-model publication additionally requires explicit model-rights approval.
