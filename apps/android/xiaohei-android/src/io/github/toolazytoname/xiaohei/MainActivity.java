package io.github.toolazytoname.xiaohei;

import android.app.Activity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.app.KeyguardManager;
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
    private static final int REQUEST_NOTIFICATIONS = 43;
    private TextView stateView;
    private TextView historyView;
    private TextView draftView;
    private Button confirmDraftButton;
    private Button cancelDraftButton;
    private String pendingReplyContent;
    private XiaoheiNotificationListener.MessageTarget pendingReplyTarget;
    private Button armButton;
    private TextView dspStateView;
    private TextView cpuKwsStateView;
    private DspProfileClient dspProfile;
    private final BroadcastReceiver dspStatusReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String state = intent.getStringExtra("state");
            String detail = intent.getStringExtra("detail");
            dspStateView.setText("DSP：" + state + "\n" + detail);
        }
    };
    private final BroadcastReceiver cpuKwsStatusReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            showCpuKwsStatus(intent.getStringExtra("state"), intent.getStringExtra("detail"));
        }
    };
    private final BroadcastReceiver notificationAccessReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (!intent.getBooleanExtra("granted", false))
                clearReplyDraft("通知访问已撤销；已清除待确认草稿，未打开消息 App");
        }
    };
    private final ActionDispatcher actions = new ActionDispatcher();
    private CommandRouter.Request pendingCameraRequest;
    private String pendingCameraText;
    private WakewordBroker broker;
    private VoiceCommandSession voiceSession;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean notificationAccessWatchActive;
    private final Runnable notificationAccessWatch = new Runnable() {
        @Override public void run() {
            if (!notificationAccessWatchActive) return;
            if (pendingReplyContent != null && !XiaoheiNotificationListener.accessGranted(MainActivity.this))
                clearReplyDraft("通知访问已撤销；已清除待确认草稿，未打开消息 App");
            if (notificationAccessWatchActive) mainHandler.postDelayed(this, 1000);
        }
    };
    private boolean privacyLockedAtLaunch;
    private boolean pendingCpuKwsStart;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfigMigrator.run(this);
        privacyLockedAtLaunch = privacyLockedNow();
        broker = new WakewordBroker(this);
        voiceSession = new VoiceCommandSession(this, this);
        dspProfile = new DspProfileClient(this);
        setShowWhenLocked(true);
        setTurnScreenOn(true);
        setContentView(buildView());
        if (!getSharedPreferences("user_controls", MODE_PRIVATE).getBoolean("onboarding_seen", false))
            startActivity(new Intent(this, OnboardingActivity.class));
        onStateChanged(broker.state(), "尚未启用");
        consumeControlIntent(getIntent());
        consumeWakeIntent(getIntent());
        consumeDebugTranscript(getIntent());
        consumeTalkIntent(getIntent());
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        privacyLockedAtLaunch = privacyLockedNow();
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
        if (intent != null && intent.getBooleanExtra("global_stop", false)) {
            voiceSession.stop();
            dspProfile.disarm();
            stopService(new Intent(this, CpuWakewordService.class));
            broker.disarm();
            intent.removeExtra("global_stop");
            refreshDspStatus();
            return;
        }
        if (intent == null || !intent.getBooleanExtra("rollback_disarm", false)) return;
        dspProfile.disarm();
        intent.removeExtra("rollback_disarm");
    }

    private void consumeWakeIntent(Intent intent) {
        if (intent == null || !intent.hasExtra(WakewordReceiver.EXTRA_KEYWORD_ID)) return;
        if ("CPU_KWS".equals(intent.getStringExtra(WakewordReceiver.EXTRA_SOURCE))) {
            if (broker.state() == WakewordBroker.State.OFF) broker.armManualMode();
            broker.dispatchCpuHit(intent.getStringExtra(WakewordReceiver.EXTRA_KEYWORD_ID));
        } else {
            if (broker.state() == WakewordBroker.State.OFF) broker.armDspMode();
            broker.dispatchDspHit(
                intent.getStringExtra(WakewordReceiver.EXTRA_KEYWORD_ID),
                intent.getIntExtra(WakewordReceiver.EXTRA_CONFIDENCE, -1),
                intent.getBooleanExtra(WakewordReceiver.EXTRA_CAPTURE_AVAILABLE, false));
        }
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

        cpuKwsStateView = new TextView(this);
        cpuKwsStateView.setPadding(0, pad, 0, 0);
        root.addView(cpuKwsStateView);
        refreshCpuKwsStatus();

        Button cpuKwsStart = new Button(this);
        cpuKwsStart.setText("启动“小黑小黑”CPU 实验唤醒");
        cpuKwsStart.setEnabled(LocalKwsEngine.isBundled());
        cpuKwsStart.setOnClickListener(v -> startCpuKws());
        root.addView(cpuKwsStart);

        Button cpuKwsStop = new Button(this);
        cpuKwsStop.setText("停止 CPU 唤醒并释放麦克风");
        cpuKwsStop.setEnabled(LocalKwsEngine.isBundled());
        cpuKwsStop.setOnClickListener(v -> stopService(new Intent(this, CpuWakewordService.class)));
        root.addView(cpuKwsStop);

        Button talkButton = new Button(this);
        talkButton.setText("按一下开始说话（通用模式）");
        talkButton.setOnClickListener(v -> {
            if (broker.state() == WakewordBroker.State.OFF || broker.state() == WakewordBroker.State.ERROR)
                broker.armManualMode();
            broker.dispatchManualHit();
        });
        root.addView(talkButton);

        if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            Button callInterruptionTestButton = new Button(this);
            callInterruptionTestButton.setText("来电中断验收（最多 2 分钟）");
            callInterruptionTestButton.setOnClickListener(v -> {
                if (broker.state() == WakewordBroker.State.OFF
                        || broker.state() == WakewordBroker.State.ERROR) broker.armManualMode();
                if (broker.beginVoiceCommand()) voiceSession.startInterruptionTest();
            });
            root.addView(callInterruptionTestButton);
        }

        Button stopAllButton = new Button(this);
        stopAllButton.setText("全部停止：语音 + DSP + CPU 唤醒");
        stopAllButton.setOnClickListener(v -> {
            voiceSession.stop();
            dspProfile.disarm();
            stopService(new Intent(this, CpuWakewordService.class));
            broker.disarm();
            dspStateView.setText("DSP：正在停止并释放");
            historyView.setText("已请求全局停止；不会继续执行待处理命令");
            refreshDspStatus();
        });
        root.addView(stopAllButton);

        Button notificationButton = new Button(this);
        notificationButton.setText("开启常驻状态通知");
        notificationButton.setOnClickListener(v -> enableStatusNotification());
        root.addView(notificationButton);

        Button fixedCommandButton = new Button(this);
        fixedCommandButton.setText("测试固定命令：“打开相册”");
        fixedCommandButton.setOnClickListener(v -> dispatchTranscript("打开相册"));
        root.addView(fixedCommandButton);

        Button channelsButton = new Button(this);
        channelsButton.setText("模型渠道：ASR / Phone Agent 独立配置");
        channelsButton.setOnClickListener(v -> startActivity(new Intent(this, ModelConfigActivity.class)));
        root.addView(channelsButton);

        Button agentButton = new Button(this);
        agentButton.setText("可见 Phone Agent：观察 / 执行 / 停止");
        agentButton.setOnClickListener(v -> startActivity(new Intent(this, AgentActivity.class)));
        root.addView(agentButton);

        Button diagnosticsButton = new Button(this);
        diagnosticsButton.setText("导出脱敏诊断");
        diagnosticsButton.setOnClickListener(v -> shareDiagnostics());
        root.addView(diagnosticsButton);

        Button notificationAccessButton = new Button(this);
        notificationAccessButton.setText("通知助手：授权 / 检查未读");
        notificationAccessButton.setOnClickListener(v -> {
            if (!XiaoheiNotificationListener.accessGranted(this)) {
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            } else showNotificationSummary(false);
        });
        root.addView(notificationAccessButton);

        draftView = new TextView(this);
        draftView.setPadding(0, pad, 0, 0);
        draftView.setVisibility(View.GONE);
        root.addView(draftView);
        confirmDraftButton = new Button(this);
        confirmDraftButton.setText("确认目标与内容，打开消息 App（不自动发送）");
        confirmDraftButton.setVisibility(View.GONE);
        confirmDraftButton.setOnClickListener(v -> openMessagingForManualSend());
        root.addView(confirmDraftButton);
        cancelDraftButton = new Button(this);
        cancelDraftButton.setText("取消草稿");
        cancelDraftButton.setVisibility(View.GONE);
        cancelDraftButton.setOnClickListener(v -> clearReplyDraft("草稿已取消，没有打开消息 App"));
        root.addView(cancelDraftButton);

        historyView = new TextView(this);
        historyView.setGravity(Gravity.START);
        historyView.setPadding(0, pad, 0, 0);
        root.addView(historyView);
        TextView supported = new TextView(this);
        supported.setPadding(0, pad, 0, pad);
        supported.setText("ASR：" + voiceSession.channelDescription()
            + "\n支持：相册、设置、Wi‑Fi、蓝牙、相机、浏览器、拨号盘、闹钟、导航到…、手电筒开关、音量大小");
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
        registerReceiver(cpuKwsStatusReceiver, new IntentFilter(CpuWakewordService.STATUS_EVENT),
            Context.RECEIVER_NOT_EXPORTED);
        registerReceiver(notificationAccessReceiver,
            new IntentFilter(XiaoheiNotificationListener.ACCESS_CHANGED_EVENT),
            Context.RECEIVER_NOT_EXPORTED);
        refreshDspStatus();
        refreshCpuKwsStatus();
        notificationAccessWatchActive = true;
        mainHandler.post(notificationAccessWatch);
    }

    @Override protected void onStop() {
        unregisterReceiver(dspStatusReceiver);
        unregisterReceiver(cpuKwsStatusReceiver);
        unregisterReceiver(notificationAccessReceiver);
        notificationAccessWatchActive = false;
        mainHandler.removeCallbacks(notificationAccessWatch);
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

    private void refreshCpuKwsStatus() {
        android.content.SharedPreferences prefs = getSharedPreferences("cpu_wakeword", MODE_PRIVATE);
        showCpuKwsStatus(prefs.getString("state", LocalKwsEngine.isBundled() ? "OFF" : "UNAVAILABLE"),
            prefs.getString("detail", LocalKwsEngine.isBundled()
                ? "默认关闭；启用后会持续占用 CPU 和麦克风" : "当前安装包未包含 KWS 模型"));
    }

    private void showCpuKwsStatus(String state, String detail) {
        if (cpuKwsStateView != null) cpuKwsStateView.setText("CPU 唤醒：" + state + "\n" + detail
            + "\n这是高功耗通用模式，不是 DSP 低功耗模式。");
    }

    private void startCpuKws() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            pendingCpuKwsStart = true;
            requestPermissions(new String[] { Manifest.permission.RECORD_AUDIO }, REQUEST_RECORD_AUDIO);
            return;
        }
        startForegroundService(new Intent(this, CpuWakewordService.class)
            .setAction(CpuWakewordService.ACTION_START));
    }

    @Override public void onStateChanged(WakewordBroker.State state, String detail) {
        if (stateView == null) return;
        stateView.setText("状态：" + state + "\n" + detail);
        armButton.setText(state == WakewordBroker.State.ARMED ? "关闭基础模式" : "启用基础模式");
        if (getSharedPreferences("user_controls", MODE_PRIVATE)
                .getBoolean("status_notification", false))
            StatusNotification.show(this, state);
    }

    @Override public void onWakewordHit(WakewordEvent event) {
        historyView.setText("最近唤醒：" + event.source + " · " + event.keywordId + "\n正在开启短命令会话…");
        // The VoiceInteraction handoff may replace the current Activity instance.
        // Let that lifecycle settle before binding the recognizer to this instance.
        mainHandler.postDelayed(this::startVoiceCommand, 300);
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
            if (results.length == 1 && results[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingCpuKwsStart) startCpuKws(); else voiceSession.start();
            }
            else broker.failCommand("未授予麦克风权限；已停止命令会话");
            pendingCpuKwsStart = false;
        } else if (requestCode == REQUEST_CAMERA && pendingCameraRequest != null) {
            CommandRouter.Request pending = pendingCameraRequest;
            String pendingText = pendingCameraText;
            pendingCameraRequest = null;
            pendingCameraText = null;
            if (results.length == 1 && results[0] == PackageManager.PERMISSION_GRANTED)
                executeRequest(pendingText == null ? "相机功能" : pendingText, pending);
            else broker.finishCommand("未授予相机权限；相机相关动作未执行；已重新就绪");
        } else if (requestCode == REQUEST_NOTIFICATIONS) {
            if (results.length == 1 && results[0] == PackageManager.PERMISSION_GRANTED) {
                getSharedPreferences("user_controls", MODE_PRIVATE).edit()
                    .putBoolean("status_notification", true).apply();
                StatusNotification.show(this, broker.state());
                historyView.setText("常驻状态通知已开启；通知内可全部停止");
            } else historyView.setText("未开启状态通知；不影响语音与 DSP 功能");
        }
    }

    @Override public void onSpeechReady() {
        Log.i("XiaoheiVoice", "speech_ready");
        historyView.setText("提示音后开始说，请说：打开相册");
        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 55);
        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 120);
        mainHandler.postDelayed(tone::release, 180);
    }

    @Override public void onPartialTranscript(String text) {
        historyView.setText("正在听：" + text);
    }

    @Override public void onFinalTranscript(String text) {
        dispatchTranscript(text);
        resumeCpuKwsIfEnabled();
    }

    private void dispatchTranscript(String text) {
        broker.beginThinking();
        CommandRouter.Request request = CommandRouter.route(text);
        if (request.action == CommandRouter.Action.QUERY_UNREAD_WECHAT
                || request.action == CommandRouter.Action.QUERY_UNREAD_ALL) {
            showNotificationSummary(request.action == CommandRouter.Action.QUERY_UNREAD_WECHAT);
            return;
        }
        if (request.action == CommandRouter.Action.DRAFT_WECHAT_REPLY) {
            prepareMessageDraft(request.argument, "com.tencent.mm");
            return;
        }
        if (request.action == CommandRouter.Action.DRAFT_MESSAGE_REPLY) {
            prepareMessageDraft(request.argument, null);
            return;
        }
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
        if ((request.action == CommandRouter.Action.OPEN_CAMERA
                || request.action == CommandRouter.Action.TORCH_ON
                || request.action == CommandRouter.Action.TORCH_OFF)
                && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            pendingCameraRequest = request;
            pendingCameraText = text;
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

    private void showNotificationSummary(boolean wechatOnly) {
        XiaoheiNotificationListener.Summary summary =
            XiaoheiNotificationListener.summarize(this, wechatOnly, privacyLockedAtLaunch);
        historyView.setText("通知助手：" + summary.detail);
        broker.finishCommand(summary.detail + "；已重新就绪");
    }

    private void prepareMessageDraft(String content, String preferredPackage) {
        clearReplyDraft(null);
        if (content == null || content.isEmpty()) {
            historyView.setText("回复草稿：还缺少完整回复内容；请说“回复消息说……”");
            broker.finishCommand("需要回复内容；没有打开消息 App；已重新就绪");
            return;
        }
        XiaoheiNotificationListener.MessageTarget target =
            XiaoheiNotificationListener.latestMessageTarget(this, preferredPackage, privacyLockedAtLaunch);
        if (target == null) {
            historyView.setText("回复草稿：受支持消息通知已消失、服务未连接或仍在锁屏；未创建草稿");
            broker.finishCommand("没有可绑定的消息通知；未执行；已重新就绪");
            return;
        }
        pendingReplyContent = content;
        pendingReplyTarget = target;
        draftView.setText("待确认草稿\nApp：" + target.appLabel + "\n目标：" + target.conversation
            + "\n完整内容：" + content + "\n确认后只打开消息 App，仍由你亲自点击发送。");
        draftView.setVisibility(View.VISIBLE);
        confirmDraftButton.setText("确认目标与内容，打开 " + target.appLabel + "（不自动发送）");
        confirmDraftButton.setVisibility(View.VISIBLE);
        cancelDraftButton.setVisibility(View.VISIBLE);
        historyView.setText("草稿已生成，等待你确认目标和完整内容");
        broker.finishCommand("草稿待确认；没有自动发送；已重新就绪");
    }

    private void openMessagingForManualSend() {
        if (pendingReplyContent == null || !XiaoheiNotificationListener.messageTargetStillActive(
                this, pendingReplyTarget, privacyLockedNow())) {
            clearReplyDraft("通知已消失或手机已锁屏；操作已取消");
            return;
        }
        Intent launch = getPackageManager().getLaunchIntentForPackage(pendingReplyTarget.packageName);
        if (launch == null) {
            clearReplyDraft("消息 App 不可启动；草稿未发送");
            return;
        }
        startActivity(launch);
        clearReplyDraft("已打开消息 App；小黑没有输入或点击发送");
    }

    private void clearReplyDraft(String message) {
        pendingReplyContent = null;
        pendingReplyTarget = null;
        if (draftView != null) draftView.setVisibility(View.GONE);
        if (confirmDraftButton != null) confirmDraftButton.setVisibility(View.GONE);
        if (cancelDraftButton != null) cancelDraftButton.setVisibility(View.GONE);
        if (message != null && historyView != null) historyView.setText(message);
    }

    @Override public void onSpeechError(String safeDetail) {
        historyView.setText("短命令会话结束：" + safeDetail);
        broker.finishCommand(safeDetail + "；已重新就绪");
        resumeCpuKwsIfEnabled();
    }

    private void resumeCpuKwsIfEnabled() {
        String state = getSharedPreferences("cpu_wakeword", MODE_PRIVATE).getString("state", "OFF");
        if ("DETECTED".equals(state)) startForegroundService(new Intent(this, CpuWakewordService.class)
            .setAction(CpuWakewordService.ACTION_RESUME));
    }

    private void shareDiagnostics() {
        DspProfileClient.Status dsp = dspProfile.status();
        String report = DiagnosticReport.build(this, broker.state(), dsp);
        Log.i("XiaoheiDiagnostics", report);
        Intent share = new Intent(Intent.ACTION_SEND).setType("text/plain")
            .putExtra(Intent.EXTRA_SUBJECT, "Xiaohei redacted diagnostics v1")
            .putExtra(Intent.EXTRA_TEXT, report);
        startActivity(Intent.createChooser(share, "导出脱敏诊断"));
    }

    private void enableStatusNotification() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.POST_NOTIFICATIONS }, REQUEST_NOTIFICATIONS);
            return;
        }
        getSharedPreferences("user_controls", MODE_PRIVATE).edit()
            .putBoolean("status_notification", true).apply();
        StatusNotification.show(this, broker.state());
        historyView.setText("常驻状态通知已开启；通知内可全部停止");
    }

    private boolean privacyLockedNow() {
        return !getSystemService(PowerManager.class).isInteractive()
            || getSystemService(KeyguardManager.class).isKeyguardLocked();
    }

    @Override protected void onDestroy() {
        mainHandler.removeCallbacksAndMessages(null);
        voiceSession.stop();
        super.onDestroy();
    }
}
