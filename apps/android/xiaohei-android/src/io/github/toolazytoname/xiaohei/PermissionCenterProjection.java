package io.github.toolazytoname.xiaohei;

/** Metadata-only permission center. It cannot request, grant, or revoke any capability. */
final class PermissionCenterProjection {
    static final class Snapshot {
        final boolean microphoneGranted, cameraGranted, notificationsGranted;
        final boolean notificationListenerConnected, accessibilityConnected;
        Snapshot(boolean microphoneGranted, boolean cameraGranted, boolean notificationsGranted,
                boolean notificationListenerConnected, boolean accessibilityConnected) {
            this.microphoneGranted = microphoneGranted;
            this.cameraGranted = cameraGranted;
            this.notificationsGranted = notificationsGranted;
            this.notificationListenerConnected = notificationListenerConnected;
            this.accessibilityConnected = accessibilityConnected;
        }
    }

    static String visibleText(Snapshot value) {
        if (value == null) return "权限中心 / Permission center：状态不可用；不申请、不授予任何权限";
        return "权限中心 / Permission center（只读）\n"
            + entry("麦克风 / Microphone", value.microphoneGranted, "用于用户主动语音命令", "Android 设置 > 小黑 > 权限", "本版本不保存最近使用时间")
            + entry("相机 / Camera", value.cameraGranted, "仅用于用户主动手电筒/相机动作", "Android 设置 > 小黑 > 权限", "本版本不保存最近使用时间")
            + entry("通知 / Notifications", value.notificationsGranted, "用于显示状态和停止入口", "Android 设置 > 小黑 > 通知", "本版本不保存最近使用时间")
            + entry("通知读取 / Notification access", value.notificationListenerConnected, "仅在用户请求未读摘要或手动回复草稿时读取", "Android 设置 > 通知访问", "未连接时不读取；最近使用不公开")
            + entry("无障碍 / Accessibility", value.accessibilityConnected, "仅执行用户确认的受限 Phone Agent 步骤", "Android 设置 > 无障碍", "未连接时不执行；最近使用不公开")
            + "Root broker：不支持 / unsupported；当前未接线，不能授予 root，也没有撤销对象。\n"
            + "“打开系统权限设置”只跳转 Android 系统页；真正授予或撤销始终由你完成。";
    }

    private static String entry(String name, boolean enabled, String purpose, String revoke, String lastUse) {
        return name + "：" + (enabled ? "已授予或已连接 / enabled" : "未授予、未连接或未知 / unavailable")
            + "\n用途：" + purpose + "\n最近使用：" + lastUse + "\n撤销：" + revoke + "\n";
    }
}
