package io.github.toolazytoname.xiaohei;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/** Optional ongoing status surface with a deterministic global-stop action. */
final class StatusNotification {
    private static final String CHANNEL = "xiaohei_status";
    private static final int ID = 1208;

    static void show(Context context, WakewordBroker.State state) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(new NotificationChannel(
            CHANNEL, "小黑助手状态", NotificationManager.IMPORTANCE_LOW));
        PendingIntent open = PendingIntent.getActivity(context, 11,
            new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent stop = PendingIntent.getActivity(context, 12,
            new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra("global_stop", true),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new Notification.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_xiaohei_tile)
            .setContentTitle("小黑助手 · " + state)
            .setContentText("麦克风只在可见短命令会话中使用")
            .setContentIntent(open).setOngoing(true).setOnlyAlertOnce(true)
            .addAction(new Notification.Action.Builder(null, "全部停止", stop).build())
            .build();
        manager.notify(ID, notification);
    }
}
