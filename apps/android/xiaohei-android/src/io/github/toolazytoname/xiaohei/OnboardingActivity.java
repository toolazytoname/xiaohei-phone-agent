package io.github.toolazytoname.xiaohei;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** First-run explanation only: no permission is requested from this screen. */
public final class OnboardingActivity extends Activity {
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        TextView title = new TextView(this);
        title.setText("欢迎使用小黑 / Welcome to Xiaohei");
        title.setTextSize(25);
        root.addView(title);
        TextView body = new TextView(this);
        body.setPadding(0, pad, 0, pad);
        body.setText("基础模式可以先直接使用：按一下说话、快捷设置或系统助手入口。\n\n"
            + "麦克风：只在你触发短命令或主动打开 CPU 唤醒时使用；CPU 唤醒有持续通知且默认关闭。\n\n"
            + "通知：可选，只读取当前系统通知来汇总未读；不读取聊天数据库，也不会自动发送消息。\n\n"
            + "无障碍：可选，仅用于你在可见 Phone Agent 中确认的操作；支付、密码和验证码默认拒绝。\n\n"
            + "OnePlus DSP：可选设备增强包，独立于基础模式；未安装或不兼容时不会阻止日常使用。\n\n"
            + "你可以随时在 Android 设置中撤销权限、停止服务或卸载。\n\n"
            + "Base mode works without these optional permissions. Microphone is used only for an invoked command or CPU wake word. Notification access and Accessibility are optional, visible, and revocable. The OnePlus DSP profile is a separate device enhancement.");
        body.setTextSize(16);
        root.addView(body);
        Button continueButton = new Button(this);
        continueButton.setText("进入基础模式 / Continue to base mode");
        continueButton.setOnClickListener(v -> finishOnboarding());
        root.addView(continueButton);
        TextView note = new TextView(this);
        note.setGravity(Gravity.CENTER_HORIZONTAL);
        note.setText("此页不会请求权限 / This screen requests no permission");
        root.addView(note);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
    }

    private void finishOnboarding() {
        getSharedPreferences("user_controls", MODE_PRIVATE).edit().putBoolean("onboarding_seen", true).apply();
        finish();
    }
}
