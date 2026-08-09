package io.github.toolazytoname.xiaohei.dsp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/** Signature-gated foreground owner for the device-specific DSP lifecycle. */
public final class DspControlService extends Service {
    public static final String ACTION_ARM = "io.github.toolazytoname.xiaohei.dsp.action.ARM";
    public static final String ACTION_DISARM = "io.github.toolazytoname.xiaohei.dsp.action.DISARM";
    public static final String ACTION_STATUS = "io.github.toolazytoname.xiaohei.dsp.action.STATUS";
    public static final String STATUS_EVENT = "io.github.toolazytoname.xiaohei.action.DSP_STATUS";
    public static final String EXTRA_OK = "ok";
    public static final String EXTRA_STATE = "state";
    public static final String EXTRA_DETAIL = "detail";
    private static final int NOTIFICATION_ID = 81;
    private static final String CHANNEL_ID = "xiaohei_dsp_active";
    private boolean foreground;

    @Override public void onCreate() {
        super.onCreate();
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID,
            "小黑低功耗唤醒", NotificationManager.IMPORTANCE_LOW));
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? ACTION_STATUS : intent.getAction();
        if (ACTION_ARM.equals(action)) {
            ensureForeground("正在启动 DSP 低功耗唤醒");
            SoundTriggerGateway.ProbeResult result = SoundTriggerGateway.startRecognition(this);
            if (result.ok) updateNotification("DSP 低功耗唤醒已开启");
            else stopOwnedLifecycle();
            publish(result);
        } else if (ACTION_DISARM.equals(action)) {
            SoundTriggerGateway.ProbeResult result = SoundTriggerGateway.detach(this);
            publish(result);
            stopOwnedLifecycle();
        } else {
            publish(SoundTriggerGateway.probe(this));
            if (!foreground) stopSelf(startId);
        }
        return foreground ? START_STICKY : START_NOT_STICKY;
    }

    @Override public void onDestroy() {
        SoundTriggerGateway.detach(this);
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }

    private void ensureForeground(String text) {
        if (!foreground) {
            startForeground(NOTIFICATION_ID, notification(text));
            foreground = true;
        }
    }

    private void updateNotification(String text) {
        getSystemService(NotificationManager.class).notify(NOTIFICATION_ID, notification(text));
    }

    private Notification notification(String text) {
        Intent ui = new Intent(this, ProbeActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 81, ui,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("小黑 / Xiaohei")
            .setContentText(text)
            .setContentIntent(pending)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build();
    }

    private void publish(SoundTriggerGateway.ProbeResult result) {
        Intent status = new Intent(STATUS_EVENT)
            .setPackage("io.github.toolazytoname.xiaohei")
            .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            .putExtra(EXTRA_OK, result.ok)
            .putExtra(EXTRA_STATE, SoundTriggerGateway.currentState())
            .putExtra(EXTRA_DETAIL, result.detail);
        sendBroadcast(status, "io.github.toolazytoname.xiaohei.permission.WAKEWORD_EVENT");
    }

    private void stopOwnedLifecycle() {
        foreground = false;
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }
}
