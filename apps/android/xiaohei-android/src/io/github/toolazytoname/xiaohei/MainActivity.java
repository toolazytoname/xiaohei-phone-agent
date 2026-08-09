package io.github.toolazytoname.xiaohei;

import android.app.Activity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.media.AudioManager;
import android.media.ToneGenerator;
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
    private TextView dspStateView;
    private DspProfileClient dspProfile;
    private final BroadcastReceiver dspStatusReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String state = intent.getStringExtra("state");
            String detail = intent.getStringExtra("detail");
            dspStateView.setText("DSP：" + state + "\n" + detail);
        }
    };
    private final GalleryActionAdapter gallery = new GalleryActionAdapter();
    private WakewordBroker broker;
    private VoiceCommandSession voiceSession;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        broker = new WakewordBroker(this);
        voiceSession = new VoiceCommandSession(this, this);
        dspProfile = new DspProfileClient(this);
        setShowWhenLocked(true);
        setTurnScreenOn(true);
        setContentView(buildView());
        onStateChanged(broker.state(), "尚未启用");
        consumeControlIntent(getIntent());
        consumeWakeIntent(getIntent());
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        consumeControlIntent(intent);
        consumeWakeIntent(intent);
    }

    private void consumeControlIntent(Intent intent) {
        if (intent == null || !intent.getBooleanExtra("rollback_disarm", false)) return;
        dspProfile.disarm();
        intent.removeExtra("rollback_disarm");
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

        dspStateView = new TextView(this);
        dspStateView.setText(dspProfile.isInstalled()
            ? "DSP：正在读取状态" : "DSP：此设备未安装增强 profile");
        dspStateView.setPadding(0, pad, 0, 0);
        root.addView(dspStateView);

        Button dspArmButton = new Button(this);
        dspArmButton.setText("启动 DSP 低功耗唤醒");
        dspArmButton.setEnabled(dspProfile.isInstalled());
        dspArmButton.setOnClickListener(v -> {
            if (!dspProfile.arm()) dspStateView.setText("DSP：启动失败；请检查设备 profile");
        });
        root.addView(dspArmButton);

        Button dspDisarmButton = new Button(this);
        dspDisarmButton.setText("停止并释放 DSP");
        dspDisarmButton.setEnabled(dspProfile.isInstalled());
        dspDisarmButton.setOnClickListener(v -> {
            if (!dspProfile.disarm()) dspStateView.setText("DSP：停止失败；请检查设备 profile");
        });
        root.addView(dspDisarmButton);

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

    @Override protected void onStart() {
        super.onStart();
        registerReceiver(dspStatusReceiver, new IntentFilter(DspProfileClient.STATUS_EVENT),
            "io.github.toolazytoname.xiaohei.permission.WAKEWORD_EVENT", null,
            Context.RECEIVER_EXPORTED);
        if (dspProfile.isInstalled()) {
            DspProfileClient.Status status = dspProfile.status();
            if (status == null) dspStateView.setText("DSP：状态查询失败；请检查 Companion 版本");
            else dspStateView.setText("DSP：" + status.state + "\n" + status.detail);
        }
    }

    @Override protected void onStop() {
        unregisterReceiver(dspStatusReceiver);
        super.onStop();
    }

    @Override public void onStateChanged(WakewordBroker.State state, String detail) {
        if (stateView == null) return;
        stateView.setText("状态：" + state + "\n" + detail);
        armButton.setText(state == WakewordBroker.State.ARMED ? "关闭基础模式" : "启用基础模式");
    }

    @Override public void onWakewordHit(WakewordEvent event) {
        historyView.setText("最近唤醒：" + event.source + " · " + event.keywordId + "\n正在开启短命令会话…");
        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 55);
        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 120);
        mainHandler.postDelayed(() -> {
            tone.release();
            startVoiceCommand();
        }, 180);
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
        mainHandler.removeCallbacksAndMessages(null);
        voiceSession.stop();
        super.onDestroy();
    }
}
