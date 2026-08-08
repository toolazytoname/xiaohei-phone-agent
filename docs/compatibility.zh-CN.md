# 兼容性模型

[English](compatibility.md)

小黑把可迁移的 Phone Agent 体验与设备专属常驻唤醒实现分开。用户不需要 Qualcomm 硬件、root、定制 ROM 或私有唤醒模型，也能安装并使用基础产品。

## 能力层级

| 层级 | 可迁移功能 | 可用范围 | 主要限制 |
|---|---|---|---|
| A — 基础唤起 | App 按钮、快捷设置、桌面快捷方式、设备支持的硬件/耳机 Intent | 面向广泛 Android；首个 APK 发布时再声明精确最低 SDK | 需要用户主动唤起 |
| B — Android 助手 | 助手手势/按键和 VoiceInteraction 会话 | 取决于用户选择小黑以及 OEM SystemUI 行为 | 本身不保证能使用厂商 DSP |
| C — CPU 唤醒词 | 可选前台 KWS 服务 | 取得麦克风权限后可覆盖较多硬件 | 明显更耗电，必须展示前台服务 |
| D — 厂商 DSP | 息屏低功耗唤醒词 | 只支持明确通过验证的硬件/ROM profile | 通常需要 system/root/OEM 集成与合法本地资产 |

Android 官方说明，被用户选中的 `VoiceInteractionService` 会由系统保持运行，用于语音交互和 hotword 相关场景；但公开服务契约并没有承诺任意下载 APK 都能使用各厂商 DSP。因此小黑把“系统助手唤起”和“DSP 唤醒”作为两个独立能力。

通知汇总通过用户授权的 `NotificationListenerService`；跨 App 可见操作只在用户到系统设置显式开启无障碍服务后使用。这两类权限都是可选项，用户拒绝后产品仍应保持可理解和部分可用。

官方参考：

- [VoiceInteractionService](https://developer.android.com/reference/android/service/voice/VoiceInteractionService.html)
- [NotificationListenerService](https://developer.android.com/reference/android/service/notification/NotificationListenerService.html)
- [AccessibilityService](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService.html)

## 运行时行为

每个后端必须返回以下一种状态：

- `available`：只读检查发现条件存在，尚未尝试启用。
- `permission_required`：需要用户完成系统控制的权限或角色设置。
- `ready`：有边界的启停验收已经通过。
- `unsupported`：当前硬件/ROM/profile 不匹配。
- `error`：原本可用的后端失败，并给出一个可操作原因与回滚方式。

安装器绝不能只看 SoC 名称就注入 DSP 私有库。设备后端必须是独立工件或本地构建选项，也不能阻塞 A 层。

## 宣发边界

可以说：“小黑是一款具有广泛兼容基础模式、并支持可选低功耗设备后端的 Android Phone Agent 产品。”

不能说：“任意 Android 手机下载这个 APK，就能获得常驻 DSP 语音唤醒。”
