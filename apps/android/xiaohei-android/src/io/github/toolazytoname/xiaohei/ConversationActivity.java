package io.github.toolazytoname.xiaohei;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Map;

/** Bounded half-duplex chat. Model output is display-only and has zero action authority. */
public final class ConversationActivity extends Activity {
    static final String EXTRA_PREFILL_TEXT = "conversation_prefill_text";
    static final String EXTRA_START_VOICE_TURN = "conversation_start_voice_turn";
    private EditText input;
    private TextView output;
    private TextView state;
    private Button send;
    private Button talk;
    private Button cancel;
    private Button stop;
    private Button repeat;
    private Button clear;
    private Button continueChat;
    private Button end;
    private Button stopSpeech;
    private PendingConversationCall pending;
    private long generation;
    private boolean destroyed;
    private boolean receiverRegistered;
    private boolean audioRouteCallbackRegistered;
    private final StringBuilder visibleTranscript = new StringBuilder();
    private String lastAssistantReply;
    private SystemTtsAdapter systemTts;
    private VoiceCommandSession voiceSession;
    private final ConversationVoiceTurnCoordinator voiceTurn = new ConversationVoiceTurnCoordinator();
    private ApplicationStopHub.Registration globalStopRegistration;
    private final ConversationSessionCoordinator coordinator = new ConversationSessionCoordinator();
    private final ConversationControlPolicy.State controls = new ConversationControlPolicy.State();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable timeout = () -> {
        if (coordinator.expire(SystemClock.elapsedRealtime())
                == ConversationSessionCoordinator.Code.TIMEOUT_CLEARED) {
            closePending();
            clearVisible("会话总时长已到，内存上下文已清空 / Session timed out and was cleared");
        }
    };
    private final BroadcastReceiver screenOff = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (!Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) return;
            stopVoiceListening();
            coordinator.onLocked();
            closePending();
            clearVisible("设备已锁定，内存上下文已清空 / Device locked; session cleared");
        }
    };
    private final AudioDeviceCallback audioRouteCallback = new AudioDeviceCallback() {
        @Override public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
            onAudioRouteChanged();
        }
        @Override public void onAudioDevicesRemoved(AudioDeviceInfo[] removedDevices) {
            onAudioRouteChanged();
        }
    };

    @Override public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setTitle("小黑对话 / Xiaohei conversation");
        setContentView(build());
        consumePrefill(getIntent());
        initializeSystemTtsIfEnabled();
        consumeVoiceStart(getIntent());
        globalStopRegistration = ApplicationStopHub.register(GlobalStopRegistry.Resource.CONVERSATION,
                this::stopForGlobalRequest);
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        consumePrefill(intent);
        consumeVoiceStart(intent);
    }

    /** A route handoff can only populate the editable field; sending remains a separate gesture. */
    private void consumePrefill(Intent intent) {
        if (intent == null) return;
        String text = intent.getStringExtra(EXTRA_PREFILL_TEXT);
        intent.removeExtra(EXTRA_PREFILL_TEXT);
        if (text == null || text.trim().isEmpty() || input == null) return;
        input.setText(text.trim());
        state.setText("状态：已预填，等待你发送；零模型/动作调用 / Status: draft only; no model/action call");
    }

    /** Only the non-exported in-app wake route may request one immediately visible listening turn. */
    private void consumeVoiceStart(Intent intent) {
        if (intent == null || !intent.getBooleanExtra(EXTRA_START_VOICE_TURN, false)) return;
        intent.removeExtra(EXTRA_START_VOICE_TURN);
        mainHandler.post(() -> {
            if (!destroyed && voiceSession == null && pending == null) startVoiceTurn();
        });
    }

    private View build() {
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        TextView notice = new TextView(this);
        notice.setText(
                "最多 6 轮、5 分钟、2048 估算 token；离开页面或锁屏即清空。\n" +
                        "模型没有手机操作、工具、通知、文件或 root 权限。\n" +
                        "远端失败时只匹配带标记的本地固定 FAQ；未知问题不会猜。\n" +
                        "Up to 6 turns, 5 minutes, and 2048 estimated tokens; leaving or locking clears context."
        );
        notice.setContentDescription("conversation-authority-notice");
        root.addView(notice);

        state = new TextView(this);
        state.setText("状态：新会话 / Status: new session");
        state.setPadding(0, pad / 2, 0, 0);
        state.setContentDescription("conversation-state");
        root.addView(state);

        input = new EditText(this);
        input.setHint("输入追问，或输入“结束聊天” / Type a follow-up or 'end chat'");
        input.setMinLines(3);
        input.setMaxLines(8);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setContentDescription("conversation-input");
        root.addView(input);

        send = new Button(this);
        send.setText("发送（半双工、无动作权限）");
        send.setContentDescription("conversation-send");
        send.setOnClickListener(view -> send());
        root.addView(send);

        talk = new Button(this);
        talk.setText("说话（仅本轮聊天，不执行手机操作） / Talk");
        talk.setContentDescription("conversation-talk");
        talk.setOnClickListener(view -> startVoiceTurn());
        root.addView(talk);

        cancel = new Button(this);
        cancel.setText("取消当前模型请求");
        cancel.setContentDescription("conversation-cancel");
        cancel.setEnabled(false);
        cancel.setOnClickListener(view -> cancelCurrent());
        root.addView(cancel);

        stop = new Button(this);
        stop.setText("停止（暂停并取消在途请求）");
        stop.setContentDescription("conversation-stop");
        stop.setOnClickListener(view -> applyControl(ConversationControlPolicy.Action.STOP));
        root.addView(stop);

        repeat = new Button(this);
        repeat.setText("重说上一条回复（本地）");
        repeat.setContentDescription("conversation-repeat");
        repeat.setEnabled(false);
        repeat.setOnClickListener(view -> applyControl(ConversationControlPolicy.Action.REPEAT));
        root.addView(repeat);

        stopSpeech = new Button(this);
        stopSpeech.setText("停止播报（保留聊天） / Stop speech (keep chat)");
        stopSpeech.setContentDescription("conversation-stop-speech");
        stopSpeech.setEnabled(false);
        stopSpeech.setOnClickListener(view -> interruptSpeech("用户已停止播报 / User stopped speech"));
        root.addView(stopSpeech);

        clear = new Button(this);
        clear.setText("清空内存上下文");
        clear.setContentDescription("conversation-clear");
        clear.setOnClickListener(view -> applyControl(ConversationControlPolicy.Action.CLEAR));
        root.addView(clear);

        continueChat = new Button(this);
        continueChat.setText("继续聊（恢复输入，不调用模型）");
        continueChat.setContentDescription("conversation-continue");
        continueChat.setEnabled(false);
        continueChat.setOnClickListener(view -> applyControl(ConversationControlPolicy.Action.CONTINUE));
        root.addView(continueChat);

        end = new Button(this);
        end.setText("结束聊天并清空内存上下文");
        end.setContentDescription("conversation-end");
        end.setOnClickListener(view -> applyControl(ConversationControlPolicy.Action.END));
        root.addView(end);

        output = new TextView(this);
        output.setPadding(0, pad, 0, 0);
        output.setText("尚未发送请求");
        output.setTextIsSelectable(true);
        output.setContentDescription("conversation-output");
        root.addView(output);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        return scroll;
    }

    private void send() {
        String userText = input.getText().toString().trim();
        ConversationControlPolicy.Action localControl = ConversationControlPolicy.parse(userText);
        if (localControl != ConversationControlPolicy.Action.NONE) {
            applyControl(localControl);
            input.setText("");
            return;
        }
        ConversationPrivacyPolicy.Result privacy = ConversationPrivacyPolicy.evaluate(userText);
        if (privacy.denied) {
            input.setText("");
            state.setText("状态：本地隐私拒绝；零模型/动作调用 / Status: local privacy denial; zero model/action calls");
            output.setText(privacy.text);
            return;
        }
        if (!controls.canSend()) {
            state.setText("状态：已暂停；点“继续聊”后再发送 / Status: paused; press Continue");
            return;
        }
        boolean fromVoice = voiceTurn.state() == ConversationVoiceTurnCoordinator.State.REVIEWING;
        if (fromVoice && !voiceTurn.beginThinking()) {
            state.setText("状态：语音轮次已失效；请重新点说话 / Status: voice turn expired");
            return;
        }
        ConversationSessionCoordinator.BeginResult begin = coordinator.begin(
                userText, currentProfileFingerprint(), SystemClock.elapsedRealtime()
        );
        switch (begin.code) {
            case END_COMMAND_CLEARED:
                closePending();
                clearVisible("聊天已结束，内存上下文已清空 / Chat ended and cleared");
                input.setText("");
                return;
            case PROFILE_CHANGED_CLEARED:
                closePending();
                clearVisible("Conversation 模型配置已变化；旧上下文已清空，请再次发送 / Profile changed; resend fresh");
                return;
            case INVALID_TEXT:
                if (fromVoice) voiceTurn.fail();
                state.setText("状态：请输入有效文字 / Status: enter valid text");
                return;
            case BUSY:
                if (fromVoice) voiceTurn.fail();
                state.setText("状态：上一请求尚未完成 / Status: previous request still active");
                return;
            case TOKEN_BUDGET_CLEARED:
                if (fromVoice) voiceTurn.fail();
                clearVisible("Token 预算已到，内存上下文已清空 / Token budget reached; session cleared");
                return;
            case TIMEOUT_CLEARED:
                if (fromVoice) voiceTurn.fail();
                clearVisible("会话总时长已到，内存上下文已清空 / Session timed out and was cleared");
                return;
            case REQUEST_READY:
                break;
            default:
                if (fromVoice) voiceTurn.fail();
                state.setText("状态：会话不可用，请重新开始 / Status: start a new session");
                return;
        }
        if (!controls.markRequestStarted()) {
            coordinator.abort(SystemClock.elapsedRealtime());
            if (fromVoice) voiceTurn.fail();
            state.setText("状态：控制状态拒绝并发请求 / Status: local control rejected request");
            return;
        }

        PendingConversationCall previous = pending;
        if (previous != null) previous.requestCancel();
        PendingConversationCall call = new PendingConversationCall(++generation);
        pending = call;
        setRunning(true);
        state.setText("状态：模型回复中（不能输入下一轮） / Status: waiting; half-duplex");
        output.setText("正在聊天；不会执行手机操作…");
        scheduleTimeout();

        ConversationClient.Request next = ConversationClient.ask(
                this,
                begin.messages,
                result -> onResult(call, userText, result)
        );
        call.bind(next::cancel);
    }

    private void startVoiceTurn() {
        if (controls.requestInFlight() || pending != null) {
            state.setText("状态：正在等待模型回复，暂不能录音 / Status: waiting for model");
            return;
        }
        if (!controls.canSend()) {
            state.setText("状态：已暂停；点“继续聊”后再说 / Status: paused; press Continue");
            return;
        }
        interruptSpeech("开始听取前已停止播报 / Speech stopped before listening");
        if (voiceTurn.state() != ConversationVoiceTurnCoordinator.State.IDLE
                && voiceTurn.state() != ConversationVoiceTurnCoordinator.State.WAITING_FOLLOWUP) {
            voiceTurn.reset();
        }
        if (!voiceTurn.beginListening()) {
            state.setText("状态：语音状态忙；请重试一次 / Status: voice state busy");
            return;
        }
        voiceSession = new VoiceCommandSession(this, new VoiceCommandSession.Listener() {
            @Override public void onSpeechReady() {
                state.setText("状态：正在听；本轮结束后才会发送 / Status: listening");
            }
            @Override public void onPartialTranscript(String text) {
                if (!voiceTurn.partial()) return;
                input.setText(text);
                state.setText("状态：正在听（未发送） / Status: listening; not sent");
            }
            @Override public void onFinalTranscript(String text) {
                voiceSession = null;
                if (!voiceTurn.finalTranscript()) return;
                input.setText(text);
                state.setText("状态：转写完成，正在本地检查 / Status: transcript ready; local check");
                send();
            }
            @Override public void onSpeechError(String safeDetail) {
                voiceSession = null;
                voiceTurn.fail();
                state.setText("状态：" + safeDetail + "；未发送模型 / Status: no model call");
                setRunning(false);
            }
        }, AsrProfile.CONVERSATION);
        if (!voiceSession.isAvailable()) {
            voiceSession = null;
            voiceTurn.fail();
            state.setText("状态：聊天 ASR 不可用；未自动切换 / Status: conversation ASR unavailable");
            return;
        }
        voiceSession.start();
        setRunning(false);
    }

    private void cancelCurrent() {
        PendingConversationCall current = pending;
        if (current == null || !current.requestCancel()) return;
        cancel.setEnabled(false);
        state.setText("状态：正在取消 / Status: cancelling");
    }

    private void onResult(PendingConversationCall call, String userText, ConversationClient.Result result) {
        if (!call.finish()) return;
        runOnUiThread(() -> {
            if (destroyed || pending != call) return;
            pending = null;
            setRunning(false);
            long now = SystemClock.elapsedRealtime();
            if (result.cancelled) {
                coordinator.abort(now);
                controls.markRequestFinished(false);
                if (voiceTurn.state() == ConversationVoiceTurnCoordinator.State.THINKING) voiceTurn.fail();
                state.setText("状态：请求已取消，可继续当前会话 / Status: request cancelled");
                output.setText(visibleOr("当前请求已取消；未执行任何动作"));
                setRunning(false);
                return;
            }
            if (!result.ok) {
                OfflineFaqFallback.Result fallback = OfflineFaqFallback.answer(userText);
                if (fallback.handled) {
                    acceptReply(userText, fallback.text, now, true);
                    return;
                }
                coordinator.abort(now);
                controls.markRequestFinished(false);
                if (voiceTurn.state() == ConversationVoiceTurnCoordinator.State.THINKING) voiceTurn.fail();
                state.setText("状态：请求失败，本轮未加入上下文 / Status: failed; turn rolled back");
                output.setText(result.text);
                setRunning(false);
                return;
            }

            acceptReply(userText, result.text, now, false);
        });
    }

    private void acceptReply(String userText, String reply, long now, boolean localFallback) {
        ConversationSessionCoordinator.Code completion = coordinator.complete(reply, now);
        if (completion == ConversationSessionCoordinator.Code.REPLY_ACCEPTED) {
            controls.markRequestFinished(true);
            appendVisible(userText, reply);
            input.setText("");
            if (localFallback) showLocalFallbackStatus(now);
            else showReadyStatus(now);
            if (voiceTurn.state() == ConversationVoiceTurnCoordinator.State.THINKING) {
                voiceTurn.beginSpeaking();
            }
            speakReply(reply);
            setRunning(false);
        } else if (completion == ConversationSessionCoordinator.Code.TURN_LIMIT_CLEARED) {
            controls.markRequestFinished(true);
            controls.apply(ConversationControlPolicy.Action.CLEAR);
            visibleTranscript.setLength(0);
            lastAssistantReply = null;
            input.setText("");
            cancelTimeout();
            state.setText("状态：已达到 6 轮，会话结束并清空 / Status: turn limit; session cleared");
            output.setText("最后回复（不再保留上下文）\n" + reply);
            setRunning(false);
        } else if (completion == ConversationSessionCoordinator.Code.TIMEOUT_CLEARED) {
            controls.markRequestFinished(false);
            clearVisible("回复到达时会话已超时，内容未加入上下文 / Reply arrived after timeout; cleared");
        } else {
            controls.markRequestFinished(false);
            clearVisible("回复超过预算或无效，内存上下文已清空 / Reply exceeded budget or was invalid; cleared");
        }
    }

    private void appendVisible(String userText, String assistantText) {
        lastAssistantReply = assistantText;
        if (visibleTranscript.length() > 0) visibleTranscript.append("\n\n");
        visibleTranscript.append("你：").append(userText).append("\n小黑：").append(assistantText);
        output.setText(visibleTranscript.toString());
    }

    private void showReadyStatus(long now) {
        ConversationSessionCoordinator.SafeStatus safe = coordinator.status(now);
        state.setText("状态：等待追问 · " + safe.completedTurns + "/" + safe.maxTurns
                + " 轮 · " + safe.usedTokens + "/" + safe.tokenBudget
                + " 估算 token / Status: follow-up ready");
    }

    private void showLocalFallbackStatus(long now) {
        ConversationSessionCoordinator.SafeStatus safe = coordinator.status(now);
        state.setText("状态：远端未成功 · 本地固定 FAQ（不是模型） · "
                + safe.completedTurns + "/" + safe.maxTurns
                + " 轮 / Status: remote failed; local fixed FAQ, not a model");
    }

    private void applyControl(ConversationControlPolicy.Action action) {
        ConversationControlPolicy.Outcome outcome = controls.apply(action);
        long now = SystemClock.elapsedRealtime();
        if (outcome.cancelRequest) {
            coordinator.abort(now);
            closePending();
        }
        if (action == ConversationControlPolicy.Action.STOP
                || action == ConversationControlPolicy.Action.CLEAR
                || action == ConversationControlPolicy.Action.END) stopVoiceListening();
        switch (action) {
            case STOP:
                interruptSpeech("聊天已停止，播报已中断 / Chat stopped; speech interrupted");
                state.setText(outcome.changed
                        ? "状态：已停止并暂停；零模型调用 / Status: stopped and paused; zero model calls"
                        : "状态：已经是暂停状态 / Status: already paused");
                output.setText(visibleOr("没有在途请求；上下文保持不变"));
                break;
            case REPEAT:
                if (outcome.repeatLastReply) {
                    state.setText("状态：本地重说上一条；零模型调用 / Status: local repeat; zero model calls");
                    output.setText("重说上一条回复（本地）\n" + lastAssistantReply);
                    speakReply(lastAssistantReply);
                } else {
                    state.setText("状态：没有可重说的回复 / Status: nothing to repeat");
                }
                break;
            case CLEAR:
                coordinator.clearByUser();
                closePending();
                clearVisible("上下文已清空；零模型调用 / Context cleared; zero model calls");
                break;
            case CONTINUE:
                state.setText(outcome.changed
                        ? "状态：可继续输入；零模型调用 / Status: ready to continue; zero model calls"
                        : "状态：已经可以继续输入 / Status: already active");
                output.setText(visibleOr("当前上下文为空，可开始新会话"));
                break;
            case END:
                coordinator.clearByUser();
                closePending();
                clearVisible("聊天已结束，内存上下文已清空 / Chat ended and cleared");
                break;
            default:
                break;
        }
        setRunning(controls.requestInFlight());
    }

    private void scheduleTimeout() {
        cancelTimeout();
        ConversationSessionCoordinator.SafeStatus safe = coordinator.status(SystemClock.elapsedRealtime());
        if (safe.active && safe.remainingMs > 0) mainHandler.postDelayed(timeout, safe.remainingMs);
    }

    private void cancelTimeout() {
        mainHandler.removeCallbacks(timeout);
    }

    private void closePending() {
        PendingConversationCall current = pending;
        pending = null;
        generation++;
        if (current != null) current.requestCancel();
        controls.markRequestFinished(false);
        setRunning(false);
    }

    private void clearVisible(String message) {
        interruptSpeech("聊天已清空，播报已中断 / Chat cleared; speech interrupted");
        controls.apply(ConversationControlPolicy.Action.CLEAR);
        visibleTranscript.setLength(0);
        lastAssistantReply = null;
        if (input != null) input.setText("");
        cancelTimeout();
        state.setText(message);
        output.setText("上下文为空；模型没有执行任何手机动作 / Context empty; no phone action executed");
        setRunning(false);
    }

    private String visibleOr(String fallback) {
        return visibleTranscript.length() == 0 ? fallback : visibleTranscript.toString();
    }

    private void setRunning(boolean running) {
        if (send != null) send.setEnabled(!running && controls.canSend());
        if (cancel != null) cancel.setEnabled(running);
        if (input != null) input.setEnabled(!running && controls.canSend());
        if (talk != null) {
            talk.setText(voiceTurn.state() == ConversationVoiceTurnCoordinator.State.WAITING_FOLLOWUP
                    ? "继续说（新一轮聊天，不执行手机操作） / Continue talking"
                    : "说话（仅本轮聊天，不执行手机操作） / Talk");
            talk.setEnabled(!running && controls.canSend() && voiceSession == null);
        }
        if (stop != null) stop.setEnabled(true);
        if (repeat != null) repeat.setEnabled(controls.canRepeat());
        if (clear != null) clear.setEnabled(true);
        if (continueChat != null) continueChat.setEnabled(!running && !controls.canSend());
        if (end != null) end.setEnabled(true);
        if (stopSpeech != null) stopSpeech.setEnabled(systemTts != null
                && systemTts.state() == TtsLifecycle.State.SPEAKING);
    }

    private void initializeSystemTtsIfEnabled() {
        android.content.SharedPreferences prefs =
                getSharedPreferences("model_channels", Context.MODE_PRIVATE);
        if (TtsChannelConfig.Provider.fromId(prefs.getString(TtsChannelConfig.PROVIDER, "off"))
                != TtsChannelConfig.Provider.SYSTEM) return;
        systemTts = new SystemTtsAdapter(this);
        systemTts.initialize((ttsState, detail) -> runOnUiThread(() -> {
            if (destroyed || state == null) return;
            if (ttsState == TtsLifecycle.State.WAITING_FOLLOWUP
                    && voiceTurn.state() == ConversationVoiceTurnCoordinator.State.SPEAKING) {
                voiceTurn.speechFinished();
            } else if (ttsState == TtsLifecycle.State.FAILED
                    && voiceTurn.state() == ConversationVoiceTurnCoordinator.State.SPEAKING) {
                voiceTurn.fail();
            }
            state.setText("状态：" + detail + " / TTS: " + ttsState.name());
            setRunning(controls.requestInFlight());
        }));
    }

    private void speakReply(String reply) {
        if (systemTts == null || reply == null || reply.trim().isEmpty()) {
            if (voiceTurn.state() == ConversationVoiceTurnCoordinator.State.SPEAKING) {
                voiceTurn.speechFinished();
            }
            return;
        }
        systemTts.speak(reply);
        setRunning(controls.requestInFlight());
    }

    private void interruptSpeech(String detail) {
        if (systemTts == null) return;
        systemTts.interrupt(detail);
        setRunning(controls.requestInFlight());
    }

    private boolean stopForGlobalRequest() {
        stopVoiceListening();
        closePending();
        if (systemTts != null) systemTts.stop("全局停止已中断播报 / Global stop interrupted speech");
        coordinator.clearByUser();
        return true;
    }

    private String currentProfileFingerprint() {
        android.content.SharedPreferences prefs =
                getSharedPreferences("model_channels", Context.MODE_PRIVATE);
        Map<String, Object> values = new HashMap<>();
        values.put(ChannelProfileConfig.CONVERSATION_ENABLED,
                prefs.getBoolean(ChannelProfileConfig.CONVERSATION_ENABLED, false));
        values.put(ChannelProfileConfig.CONVERSATION_ENDPOINT,
                prefs.getString(ChannelProfileConfig.CONVERSATION_ENDPOINT, ""));
        values.put(ChannelProfileConfig.CONVERSATION_MODEL,
                prefs.getString(ChannelProfileConfig.CONVERSATION_MODEL, ""));
        return ChannelProfileConfig.fingerprint(values, true);
    }

    @Override protected void onStart() {
        super.onStart();
        if (receiverRegistered) return;
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(screenOff, filter, Context.RECEIVER_NOT_EXPORTED);
        else registerReceiver(screenOff, filter);
        receiverRegistered = true;
        AudioManager audio = getSystemService(AudioManager.class);
        if (!audioRouteCallbackRegistered && audio != null) {
            audio.registerAudioDeviceCallback(audioRouteCallback, mainHandler);
            audioRouteCallbackRegistered = true;
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (coordinator.checkProfile(currentProfileFingerprint())
                == ConversationSessionCoordinator.Code.PROFILE_CHANGED_CLEARED) {
            closePending();
            clearVisible("Conversation 模型配置已变化；旧上下文已清空 / Profile changed; session cleared");
        }
    }

    @Override protected void onStop() {
        unregisterAudioRouteCallback();
        if (receiverRegistered) {
            unregisterReceiver(screenOff);
            receiverRegistered = false;
        }
        ConversationSessionCoordinator.SafeStatus safe = coordinator.status(SystemClock.elapsedRealtime());
        if (!isChangingConfigurations()
                && (safe.active || visibleTranscript.length() > 0 || pending != null)) {
            KeyguardManager keyguard = getSystemService(KeyguardManager.class);
            if (keyguard != null && keyguard.isKeyguardLocked()) coordinator.onLocked();
            else coordinator.onBackgrounded();
            closePending();
            clearVisible("已离开聊天页面，内存上下文已清空 / Left chat; session cleared");
        }
        stopVoiceListening();
        super.onStop();
    }

    @Override protected void onDestroy() {
        destroyed = true;
        unregisterAudioRouteCallback();
        if (receiverRegistered) {
            unregisterReceiver(screenOff);
            receiverRegistered = false;
        }
        coordinator.onBackgrounded();
        stopVoiceListening();
        closePending();
        controls.apply(ConversationControlPolicy.Action.CLEAR);
        visibleTranscript.setLength(0);
        lastAssistantReply = null;
        cancelTimeout();
        if (systemTts != null) {
            systemTts.destroy();
            systemTts = null;
        }
        if (globalStopRegistration != null) {
            globalStopRegistration.close();
            globalStopRegistration = null;
        }
        super.onDestroy();
    }

    private void stopVoiceListening() {
        VoiceCommandSession current = voiceSession;
        voiceSession = null;
        if (current != null) current.stop();
        if (voiceTurn.state() == ConversationVoiceTurnCoordinator.State.LISTENING
                || voiceTurn.state() == ConversationVoiceTurnCoordinator.State.REVIEWING
                || voiceTurn.state() == ConversationVoiceTurnCoordinator.State.THINKING) {
            voiceTurn.stop();
        }
    }

    /** A new output/input device requires a deliberate fresh turn; old audio never follows it. */
    private void onAudioRouteChanged() {
        if (destroyed || (voiceSession == null && pending == null
                && (systemTts == null || systemTts.state() != TtsLifecycle.State.SPEAKING))) return;
        applyControl(ConversationControlPolicy.Action.STOP);
        state.setText("状态：音频设备已变化；已停止当前轮次，请手动继续 / Status: audio route changed; start a new turn");
    }

    private void unregisterAudioRouteCallback() {
        if (!audioRouteCallbackRegistered) return;
        AudioManager audio = getSystemService(AudioManager.class);
        if (audio != null) audio.unregisterAudioDeviceCallback(audioRouteCallback);
        audioRouteCallbackRegistered = false;
    }
}
