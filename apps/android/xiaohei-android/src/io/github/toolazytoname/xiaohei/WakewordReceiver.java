package io.github.toolazytoname.xiaohei;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/** Signature-gated bridge from a device-profile Companion into the ordinary app. */
public final class WakewordReceiver extends BroadcastReceiver {
    static final String ACTION = "io.github.toolazytoname.xiaohei.action.WAKEWORD";
    static final String EXTRA_KEYWORD_ID = "keyword_id";
    static final String EXTRA_CONFIDENCE = "confidence";
    static final String EXTRA_CAPTURE_AVAILABLE = "capture_available";

    @Override public void onReceive(Context context, Intent event) {
        if (!ACTION.equals(event.getAction())) return;
        context.getSharedPreferences("wakeword_events", Context.MODE_PRIVATE).edit()
            .putString(EXTRA_KEYWORD_ID, event.getStringExtra(EXTRA_KEYWORD_ID))
            .putInt(EXTRA_CONFIDENCE, event.getIntExtra(EXTRA_CONFIDENCE, -1))
            .putBoolean(EXTRA_CAPTURE_AVAILABLE,
                event.getBooleanExtra(EXTRA_CAPTURE_AVAILABLE, false))
            .apply();
        Bundle args = new Bundle();
        args.putString(EXTRA_KEYWORD_ID, event.getStringExtra(EXTRA_KEYWORD_ID));
        args.putInt(EXTRA_CONFIDENCE, event.getIntExtra(EXTRA_CONFIDENCE, -1));
        args.putBoolean(EXTRA_CAPTURE_AVAILABLE,
            event.getBooleanExtra(EXTRA_CAPTURE_AVAILABLE, false));
        if (!XiaoheiVoiceInteractionService.showWakeSession(args)) {
            showFallbackNotification(context, args);
        }
    }

    private static void showFallbackNotification(Context context, Bundle args) {
        String channelId = "xiaohei_wakeword";
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(new NotificationChannel(
            channelId, "小黑唤醒事件", NotificationManager.IMPORTANCE_HIGH));
        Intent ui = new Intent(context, MainActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtras(args);
        PendingIntent pending = PendingIntent.getActivity(context, 52, ui,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new Notification.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("小黑已听到唤醒词")
            .setContentText("点按后说出一条短命令")
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_STATUS)
            .build();
        manager.notify(52, notification);
    }
}
