package io.github.toolazytoname.xiaohei;

import android.app.Activity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;

/** First vertical slice: a manual wake event is routed to the safe gallery action. */
public final class MainActivity extends Activity implements WakewordBroker.Listener, VoiceCommandSession.Listener {
    private static final String ACTION_TAG = "XiaoheiAction";
    private static final int REQUEST_RECORD_AUDIO = 41;
    private static final int REQUEST_CAMERA = 42;
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
    private final ActionDispatcher actions = new ActionDispatcher();
    private CommandRouter.Request pendingCameraRequest;
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
        consumeDebugTranscript(getIntent());
        consumeTalkIntent(getIntent());
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        consumeControlIntent(intent);
        consumeWakeIntent(intent);
        consumeDebugTranscript(intent);
        consumeTalkIntent(intent);
    }

    private void consumeTalkIntent(Intent intent) {
        if (intent == null || !intent.getBooleanExtra("start_talk", false)) return;
        intent.removeExtra("start_talk");
        if (broker.state() == WakewordBroker.State.OFF || broker.state() == WakewordBroker.State.ERROR)
            broker.armManualMode();
        broker.dispatchManualHit();
    }

    private void consumeDebugTranscript(Intent intent) {
        if (intent == null || (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) == 0)
            return;
        String transcript = intent.getStringExtra("debug_transcript");
        if (transcript == null || transcript.isEmpty()) return;
        intent.removeExtra("debug_transcript");
        if (broker.state() == WakewordBroker.State.OFF) broker.armManualMode();
        broker.beginVoiceCommand();
        dispatchTranscript(transcript);
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
        title.setText("小黑 / Xiaohei\nM2：离线短命令助手");
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
            else refreshDspStatus();
        });
        root.addView(dspDisarmButton);

        Button talkButton = new Button(this);
        talkButton.setText("按一下开始说话（通用模式）");
        talkButton.setOnClickListener(v -> {
            if (broker.state() == WakewordBroker.State.OFF || broker.state() == WakewordBroker.State.ERROR)
                broker.armManualMode();
            broker.dispatchManualHit();
        });
        root.addView(talkButton);

        Button stopAllButton = new Button(this);
        stopAllButton.setText("全部停止：语音 + DSP");
        stopAllButton.setOnClickListener(v -> {
            voiceSession.stop();
            dspProfile.disarm();
            broker.disarm();
            dspStateView.setText("DSP：正在停止并释放");
            historyView.setText("已请求全局停止；不会继续执行待处理命令");
            refreshDspStatus();
        });
        root.addView(stopAllButton);

        Button fixedCommandButton = new Button(this);
        fixedCommandButton.setText("测试固定命令：“打开相册”");
        fixedCommandButton.setOnClickListener(v -> dispatchTranscript("打开相册"));
        root.addView(fixedCommandButton);

        Button diagnosticsButton = new Button(this);
        diagnosticsButton.setText("导出脱敏诊断");
        diagnosticsButton.setOnClickListener(v -> shareDiagnostics());
        root.addView(diagnosticsButton);

        historyView = new TextView(this);
        historyView.setGravity(Gravity.START);
        historyView.setPadding(0, pad, 0, 0);
        root.addView(historyView);
        TextView supported = new TextView(this);
        supported.setPadding(0, pad, 0, pad);
        supported.setText("支持：相册、设置、Wi‑Fi、蓝牙、相机、浏览器、拨号盘、闹钟、导航到…、手电筒开关、音量大小");
        root.addView(supported);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        return scroll;
    }

    @Override protected void onStart() {
        super.onStart();
        registerReceiver(dspStatusReceiver, new IntentFilter(DspProfileClient.STATUS_EVENT),
            "io.github.toolazytoname.xiaohei.permission.WAKEWORD_EVENT", null,
            Context.RECEIVER_EXPORTED);
        refreshDspStatus();
    }

    @Override protected void onStop() {
        unregisterReceiver(dspStatusReceiver);
        super.onStop();
    }

    @Override protected void onPause() {
        if (voiceSession != null && voiceSession.isActive()) {
            voiceSession.stop();
            broker.finishCommand("会话被来电或界面切换中断；麦克风已释放，可重新唤起");
        }
        super.onPause();
    }

    private void refreshDspStatus() {
        if (!dspProfile.isInstalled()) return;
        DspProfileClient.Status status = dspProfile.status();
        if (status == null) dspStateView.setText("DSP：状态查询失败；请检查 Companion 版本");
        else dspStateView.setText("DSP：" + status.state + "\n" + status.detail);
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
        } else if (requestCode == REQUEST_CAMERA && pendingCameraRequest != null) {
            CommandRouter.Request pending = pendingCameraRequest;
            pendingCameraRequest = null;
            if (results.length == 1 && results[0] == PackageManager.PERMISSION_GRANTED)
                executeRequest("手电筒", pending);
            else broker.finishCommand("未授予相机权限；手电筒未改变；已重新就绪");
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
        CommandRouter.Request request = CommandRouter.route(text);
        if (request.action == CommandRouter.Action.AMBIGUOUS) {
            historyView.setText("命令：" + text + "\n检测到多个目标，未执行。请一次只说一个动作。");
            broker.finishCommand("需要确认：请一次只说一个动作；已重新就绪");
            return;
        }
        if (request.action == CommandRouter.Action.UNKNOWN) {
            historyView.setText("命令：" + text + "\n未匹配允许的短命令");
            broker.finishCommand("未匹配命令；已重新就绪");
            return;
        }
        if ((request.action == CommandRouter.Action.TORCH_ON
                || request.action == CommandRouter.Action.TORCH_OFF)
                && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            pendingCameraRequest = request;
            requestPermissions(new String[] { Manifest.permission.CAMERA }, REQUEST_CAMERA);
            return;
        }
        executeRequest(text, request);
    }

    private void executeRequest(String text, CommandRouter.Request request) {
        broker.beginAction("已识别“" + text + "”；正在执行 " + request.action);
        ActionDispatcher.Result result = actions.execute(this, request);
        Log.i(ACTION_TAG, "action=" + request.action + " ok=" + result.ok);
        historyView.setText("命令：" + text + "\n动作：" + result.detail);
        broker.finishCommand((result.ok ? "动作已完成" : "动作失败") + "；已重新就绪");
    }

    @Override public void onSpeechError(String safeDetail) {
        historyView.setText("短命令会话结束：" + safeDetail);
        broker.finishCommand(safeDetail + "；已重新就绪");
    }

    private void shareDiagnostics() {
        DspProfileClient.Status dsp = dspProfile.status();
        String report = "Xiaohei diagnostics\n"
            + "app=0.1.0-m2\n"
            + "android=" + Build.VERSION.RELEASE + " api=" + Build.VERSION.SDK_INT + "\n"
            + "device=" + Build.MANUFACTURER + " " + Build.MODEL + "\n"
            + "assistant_state=" + broker.state() + "\n"
            + "dsp=" + (dsp == null ? "unavailable" : dsp.state) + "\n"
            + "asr_available=" + voiceSession.isAvailable() + "\n";
        Intent share = new Intent(Intent.ACTION_SEND).setType("text/plain")
            .putExtra(Intent.EXTRA_SUBJECT, "Xiaohei diagnostics")
            .putExtra(Intent.EXTRA_TEXT, report);
        startActivity(Intent.createChooser(share, "导出脱敏诊断"));
    }

    @Override protected void onDestroy() {
        mainHandler.removeCallbacksAndMessages(null);
        voiceSession.stop();
        super.onDestroy();
    }
}
