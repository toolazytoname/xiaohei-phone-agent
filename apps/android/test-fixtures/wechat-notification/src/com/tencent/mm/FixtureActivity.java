package com.tencent.mm;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Bundle;
import android.widget.TextView;

/** Test-only notification producer. It never impersonates a network service or sends a message. */
public final class FixtureActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        TextView warning = new TextView(this);
        warning.setText("TEST FIXTURE — NOT WECHAT\nPosts one local notification only.");
        warning.setTextSize(22);
        setContentView(warning);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(new NotificationChannel(
            "fixture", "Xiaohei test fixture", NotificationManager.IMPORTANCE_DEFAULT));
        manager.notify(1, new Notification.Builder(this, "fixture")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("测试联系人")
            .setContentText("这是一条本地验收通知")
            .setAutoCancel(true)
            .build());
    }
}
