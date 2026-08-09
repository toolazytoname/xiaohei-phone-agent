package io.github.toolazytoname.xiaohei;

import android.app.KeyguardManager;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.util.ArrayList;
import java.util.List;

/** Reads only the current system notification surface. No notification content is persisted. */
public final class XiaoheiNotificationListener extends NotificationListenerService {
    static final String ACCESS_CHANGED_EVENT =
        "io.github.toolazytoname.xiaohei.NOTIFICATION_ACCESS_CHANGED";
    static final class Summary {
        final boolean ok;
        final String detail;
        Summary(boolean ok, String detail) { this.ok = ok; this.detail = detail; }
    }

    private static volatile XiaoheiNotificationListener active;

    @Override public void onListenerConnected() {
        active = this;
        publishAccessChanged(true);
    }
    @Override public void onListenerDisconnected() {
        active = null;
        publishAccessChanged(false);
    }
    @Override public void onDestroy() {
        if (active == this) {
            active = null;
            publishAccessChanged(false);
        }
        super.onDestroy();
    }

    private void publishAccessChanged(boolean granted) {
        // No notification metadata or draft content crosses this local event.
        sendBroadcast(new android.content.Intent(ACCESS_CHANGED_EVENT)
            .setPackage(getPackageName()).putExtra("granted", granted));
    }

    static boolean accessGranted(Context context) {
        String flat = Settings.Secure.getString(context.getContentResolver(),
            "enabled_notification_listeners");
        if (flat == null) return false;
        ComponentName ours = new ComponentName(context, XiaoheiNotificationListener.class);
        for (String item : flat.split(":"))
            if (ours.equals(ComponentName.unflattenFromString(item))) return true;
        return false;
    }

    static Summary summarize(Context context, boolean wechatOnly, boolean privacyLocked) {
        if (!accessGranted(context)) return new Summary(false,
            "尚未授权通知访问；小黑没有读取任何通知");
        if (privacyLocked || context.getSystemService(KeyguardManager.class).isKeyguardLocked())
            return new Summary(false, "手机仍在锁屏；为保护隐私，不显示通知内容");
        XiaoheiNotificationListener service = active;
        if (service == null) return new Summary(false, "通知服务正在连接，请稍后重试");
        StatusBarNotification[] values = service.getActiveNotifications();
        if (values == null) return new Summary(true, "当前没有可见通知");
        int count = 0;
        List<String> apps = new ArrayList<>();
        for (StatusBarNotification item : values) {
            if (item.getPackageName().equals(context.getPackageName())) continue;
            if (wechatOnly && !"com.tencent.mm".equals(item.getPackageName())) continue;
            Notification notification = item.getNotification();
            if ((notification.flags & Notification.FLAG_ONGOING_EVENT) != 0) continue;
            count++;
            String label;
            try {
                label = context.getPackageManager().getApplicationLabel(
                    context.getPackageManager().getApplicationInfo(item.getPackageName(), 0)).toString();
            } catch (Exception missing) { label = item.getPackageName(); }
            if (!apps.contains(label) && apps.size() < 4) apps.add(label);
        }
        if (wechatOnly) return new Summary(true, count == 0
            ? "当前系统通知栏没有微信未读通知" : "微信当前有 " + count + " 条可见通知；内容未持久化");
        return new Summary(true, count == 0 ? "当前没有可见未读通知"
            : "当前有 " + count + " 条可见通知，来自：" + join(apps));
    }

    static String latestWechatTarget(Context context, boolean privacyLocked) {
        if (!accessGranted(context) || privacyLocked
                || context.getSystemService(KeyguardManager.class).isKeyguardLocked()) return null;
        XiaoheiNotificationListener service = active;
        if (service == null) return null;
        StatusBarNotification newest = null;
        for (StatusBarNotification item : service.getActiveNotifications()) {
            if (!"com.tencent.mm".equals(item.getPackageName())) continue;
            if (newest == null || item.getPostTime() > newest.getPostTime()) newest = item;
        }
        if (newest == null) return null;
        if (newest.getNotification().visibility == Notification.VISIBILITY_SECRET)
            return "微信隐私会话";
        CharSequence title = newest.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE);
        return title == null || title.length() == 0 ? "最近微信会话" : title.toString();
    }

    private static String join(List<String> values) {
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (result.length() > 0) result.append("、");
            result.append(value);
        }
        return result.toString();
    }
}
