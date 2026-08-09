# Xiaohei threat model

## Protected assets

Xiaohei protects microphone audio, notification content, contacts and message targets, model-channel credentials, UI snapshots, user intent, device integrity, signing keys, and device-specific DSP assets.

## Trust boundaries and controls

- **Wake profile → main app:** a signature permission admits only a same-signing-identity Companion. The event contains keyword ID, confidence, and capture availability—not wake audio.
- **Microphone → ASR/KWS:** recording is local, bounded, visibly indicated, and released on result, error, stop, or lifecycle interruption. Portable CPU KWS is opt-in and separately controlled from DSP.
- **Notifications → assistant:** access is user-granted, current-notification-only, non-persistent, and denied while locked. Message drafts require fresh target/content confirmation and never click Send.
- **Model → executor:** the model proposes strict JSON but has no Accessibility handle. Package allowlists, risk decisions, exact semantic matching, step/time/recovery limits, duplicate guards, and global stop are enforced locally.
- **UI → exported trace:** snapshots, screenshots, prompts, notification content, and tokens are excluded. Sensitive targets are replaced before bounded JSONL persistence.
- **Build → release:** private OEM binaries and credentials are excluded from Git. Release signing must use an external non-debug key; APK hash, SBOM, permissions, payload paths, native libraries, and credential signatures are checked.

## Denied by design

Payment, banking, credential, password, and OTP surfaces are denied. Sending, deletion, installation, uninstallation, permission grants, calls, and security changes require a fresh, target-bound confirmation class and are not silently delegated. Private app protocols, security bypass, hidden UI automation, and background message sending are out of scope.

## Residual risks

Accessibility semantics and app UI versions can change; local or remote ASR can misrecognize speech; notification text can be adversarial; rooted device profiles enlarge the trusted computing base; CPU KWS consumes more energy; and a compromised signing key defeats the Companion boundary. Public release therefore also requires real-device surface, long-idle, upgrade/rollback, dependency, signing-governance, and model-license evidence.
