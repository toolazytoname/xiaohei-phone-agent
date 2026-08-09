# Compatibility model

[简体中文](compatibility.zh-CN.md)

Xiaohei separates the portable phone-agent experience from device-specific always-on wake implementations. A user can install and use the base product without Qualcomm hardware, root, a custom ROM, or a proprietary wake-word model.

## Capability tiers

| Tier | Portable features | Availability | Main limitation |
|---|---|---|---|
| A — Base invocation | App button, Quick Settings tile, launcher shortcut, supported hardware/headset intent | Broad Android target; exact minimum SDK will be declared with the first APK | User actively invokes Xiaohei |
| B — Android assistant | Assistant gesture/button and voice-interaction session | Depends on the user selecting Xiaohei and OEM SystemUI behavior | Does not itself guarantee access to vendor DSP |
| C — CPU wake word | Optional foreground KWS service | Generic lifecycle verified on Android 14 ARM64; acoustic support remains experimental | Noticeably higher power use; visible foreground service required; speaker robustness is not qualified |
| D — Vendor DSP | Screen-off low-power keyword | Only explicit, validated hardware/ROM profiles | Usually needs system/root/OEM integration and legal local assets |

Android documents that the user-selected `VoiceInteractionService` is kept running by the system for voice interaction and hotword-related use cases, but the public service contract does not promise that an arbitrary downloaded app can use every vendor's DSP. Xiaohei therefore treats assistant invocation and DSP wake as separate capabilities.

Notification summaries use Android's user-authorized `NotificationListenerService`. Visible cross-app interaction may use an accessibility service only after the user explicitly enables it in Settings. Both permissions are optional; the product must remain understandable and partially useful when they are denied.

Official references:

- [VoiceInteractionService](https://developer.android.com/reference/android/service/voice/VoiceInteractionService.html)
- [NotificationListenerService](https://developer.android.com/reference/android/service/notification/NotificationListenerService.html)
- [AccessibilityService](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService.html)

## Runtime behavior

Every backend reports one of:

- `available`: requirements appear present but activation has not been attempted.
- `permission_required`: the user must complete a system-controlled permission or role step.
- `ready`: a bounded start/stop acceptance check passed.
- `unsupported`: the current hardware/ROM/profile does not match.
- `error`: a previously available backend failed, with one actionable reason and rollback.

The installer never injects DSP libraries based only on a SoC name. Device backends are separate artifacts or local build options and cannot block Tier A.

## Promotion boundary

Safe public claim: “Xiaohei is an Android phone-agent product with a broadly compatible base mode and optional low-power device backends.”

The custom phrase “Xiaohei Xiaohei” currently means the optional foreground CPU fallback only. The OnePlus DSP profile uses its validated OEM phrase; Xiaohei does not claim a portable custom DSP keyword.

Unsafe claim: “Download this APK on any Android phone and get always-on DSP wake word.”
