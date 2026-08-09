package io.github.toolazytoname.xiaohei;

import android.app.Activity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** First vertical slice: a manual wake event is routed to the safe gallery action. */
public final class MainActivity extends Activity implements WakewordBroker.Listener, VoiceCommandSession.Listener {
    private static final int REQUEST_RECORD_AUDIO = 41;
    private TextView stateView;
    private TextView historyView;
    private Button armButton;
    private final GalleryActionAdapter gallery = new GalleryActionAdapter();
    private WakewordBroker broker;
    private VoiceCommandSession voiceSession;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        broker = new WakewordBroker(this);
        voiceSession = new VoiceCommandSession(this, this);
        setShowWhenLocked(true);
        setTurnScreenOn(true);
        setContentView(buildView());
        onStateChanged(broker.state(), "尚未启用");
        consumeWakeIntent(getIntent());
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        consumeWakeIntent(intent);
    }

    private void consumeWakeIntent(Intent intent) {
        if (intent == null || !intent.hasExtra(WakewordReceiver.EXTRA_KEYWORD_ID)) return;
        if (broker.state() == WakewordBroker.State.OFF) broker.armDspMode();
        broker.dispatchDspHit(
            intent.getStringExtra(WakewordReceiver.EXTRA_KEYWORD_ID),
            intent.getIntExtra(WakewordReceiver.EXTRA_CONFIDENCE, -1),
            intent.getBooleanExtra(WakewordReceiver.EXTRA_CAPTURE_AVAILABLE, false));
        intent.removeExtra(WakewordReceiver.EXTRA_KEYWORD_ID);
    }

    private View buildView() {
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("小黑 / Xiaohei\nM1：短命令 → 打开相册");
        title.setTextSize(24);
        root.addView(title);

        stateView = new TextView(this);
        stateView.setTextSize(17);
        stateView.setPadding(0, pad, 0, pad);
        root.addView(stateView);

        armButton = new Button(this);
        armButton.setText("启用基础模式");
        armButton.setOnClickListener(v -> {
            if (broker.state() == WakewordBroker.State.OFF || broker.state() == WakewordBroker.State.ERROR) broker.armManualMode();
            else broker.disarm();
        });
        root.addView(armButton);

        Button testButton = new Button(this);
        testButton.setText("模拟“小布小布”命中 → 听取命令");
        testButton.setOnClickListener(v -> broker.dispatchManualHit());
        root.addView(testButton);

        Button fixedCommandButton = new Button(this);
        fixedCommandButton.setText("测试固定命令：“打开相册”");
        fixedCommandButton.setOnClickListener(v -> dispatchTranscript("打开相册"));
        root.addView(fixedCommandButton);

        historyView = new TextView(this);
        historyView.setGravity(Gravity.START);
        historyView.setPadding(0, pad, 0, 0);
        root.addView(historyView);
        return root;
    }

    @Override public void onStateChanged(WakewordBroker.State state, String detail) {
        if (stateView == null) return;
        stateView.setText("状态：" + state + "\n" + detail);
        armButton.setText(state == WakewordBroker.State.ARMED ? "关闭基础模式" : "启用基础模式");
    }

    @Override public void onWakewordHit(WakewordEvent event) {
        historyView.setText("最近唤醒：" + event.source + " · " + event.keywordId + "\n正在开启短命令会话…");
        startVoiceCommand();
    }

    private void startVoiceCommand() {
        if (!broker.beginVoiceCommand()) return;
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.RECORD_AUDIO }, REQUEST_RECORD_AUDIO);
            return;
        }
        voiceSession.start();
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (results.length == 1 && results[0] == PackageManager.PERMISSION_GRANTED) voiceSession.start();
            else broker.failCommand("未授予麦克风权限；已停止命令会话");
        }
    }

    @Override public void onSpeechReady() {
        historyView.setText("正在听，请说：打开相册");
    }

    @Override public void onPartialTranscript(String text) {
        historyView.setText("正在听：" + text);
    }

    @Override public void onFinalTranscript(String text) {
        dispatchTranscript(text);
    }

    private void dispatchTranscript(String text) {
        broker.beginThinking();
        if (CommandRouter.route(text) == CommandRouter.Action.OPEN_GALLERY) {
            broker.beginAction("已识别“" + text + "”；正在打开相册");
            boolean opened = gallery.openGallery(this);
            historyView.setText("命令：" + text + "\n动作：打开相册/系统图片选择器" +
                (opened ? "（已发起）" : "（设备无可用图片应用）"));
            broker.finishCommand(opened ? "动作已发起；已重新就绪" : "没有可用图片应用；已重新就绪");
        } else {
            historyView.setText("命令：" + text + "\n暂只支持：打开相册");
            broker.finishCommand("未匹配命令；已重新就绪");
        }
    }

    @Override public void onSpeechError(String safeDetail) {
        historyView.setText("短命令会话结束：" + safeDetail);
        broker.finishCommand(safeDetail + "；已重新就绪");
    }

    @Override protected void onDestroy() {
        voiceSession.stop();
        super.onDestroy();
    }
}
