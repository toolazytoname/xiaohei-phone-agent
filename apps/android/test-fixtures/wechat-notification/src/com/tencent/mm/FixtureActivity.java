package com.tencent.mm;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Bundle;
import android.widget.TextView;
import java.util.Locale;

/** Test-only notification producer. It never impersonates a network service or sends a message. */
public final class FixtureActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        String mode = getIntent().getStringExtra("visibility");
        mode = mode == null ? "public" : mode.toLowerCase(Locale.ROOT);
        int visibility = mode.equals("secret") ? Notification.VISIBILITY_SECRET
            : mode.equals("private") ? Notification.VISIBILITY_PRIVATE
            : Notification.VISIBILITY_PUBLIC;
        TextView warning = new TextView(this);
        warning.setText("TEST FIXTURE — NOT WECHAT\nPosts one local " + mode
            + " notification only.");
        warning.setTextSize(22);
        setContentView(warning);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(new NotificationChannel(
            "fixture", "Xiaohei test fixture", NotificationManager.IMPORTANCE_DEFAULT));
        manager.cancelAll();
        manager.notify(1, new Notification.Builder(this, "fixture")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("测试联系人")
            .setContentText("这是一条本地验收通知")
            .setVisibility(visibility)
            .setAutoCancel(true)
            .build());
    }
}
