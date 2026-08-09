package io.github.toolazytoname.xiaohei;

import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import java.util.ArrayList;

/** A bounded, visible command session. It never keeps audio after a result or error. */
final class VoiceCommandSession implements RecognitionListener {
    interface Listener {
        void onSpeechReady();
        void onPartialTranscript(String text);
        void onFinalTranscript(String text);
        void onSpeechError(String safeDetail);
    }

    private final Context context;
    private final Listener listener;
    private final AudioManager audioManager;
    private final AudioFocusRequest focusRequest;
    private SpeechRecognizer recognizer;
    private boolean active;
    private boolean focusHeld;

    VoiceCommandSession(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        this.audioManager = this.context.getSystemService(AudioManager.class);
        this.focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
            .setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build())
            .setOnAudioFocusChangeListener(this::onAudioFocusChange)
            .build();
    }

    boolean isAvailable() {
        return usesLocalAsr() ? LocalAsrEngine.isBundled()
            : SpeechRecognizer.isRecognitionAvailable(context);
    }

    boolean isActive() { return active; }

    String channelDescription() {
        if (usesLocalAsr()) return LocalAsrEngine.isBundled()
            ? "小黑离线中文 ASR（已内置）" : "小黑离线中文 ASR（模型未内置）";
        return SpeechRecognizer.isRecognitionAvailable(context)
            ? "Android 系统识别服务" : "Android 系统识别服务（不可用）";
    }

    void start() {
        if (!isAvailable()) {
            listener.onSpeechError("当前系统没有可用的语音识别服务；请配置独立 ASR 渠道");
            return;
        }
        stop();
        if (audioManager == null || audioManager.requestAudioFocus(focusRequest)
                != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            listener.onSpeechError("音频焦点不可用；请结束通话或其他独占音频后重试");
            return;
        }
        focusHeld = true;
        recognizer = usesLocalAsr()
            ? SpeechRecognizer.createSpeechRecognizer(context,
                new ComponentName(context, XiaoheiRecognitionService.class))
            : SpeechRecognizer.createSpeechRecognizer(context);
        recognizer.setRecognitionListener(this);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1200);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 900);
        active = true;
        recognizer.startListening(intent);
    }

    void stop() {
        active = false;
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.destroy();
            recognizer = null;
        }
        if (focusHeld && audioManager != null) {
            audioManager.abandonAudioFocusRequest(focusRequest);
            focusHeld = false;
        }
    }

    private void onAudioFocusChange(int change) {
        if (change != AudioManager.AUDIOFOCUS_LOSS
                && change != AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                && change != AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) return;
        boolean wasActive = active;
        stop();
        if (wasActive) listener.onSpeechError("音频被系统中断；已停止听取并释放麦克风");
    }

    @Override public void onReadyForSpeech(Bundle params) { listener.onSpeechReady(); }
    @Override public void onBeginningOfSpeech() { }
    @Override public void onRmsChanged(float rmsdB) { }
    @Override public void onBufferReceived(byte[] buffer) { }
    @Override public void onEndOfSpeech() { }

    @Override public void onError(int error) {
        boolean wasActive = active;
        stop();
        if (wasActive) listener.onSpeechError(errorMessage(error));
    }

    @Override public void onResults(Bundle results) {
        String text = firstResult(results);
        stop();
        if (text == null) listener.onSpeechError("没有识别到命令，请重试一次");
        else listener.onFinalTranscript(text);
    }

    @Override public void onPartialResults(Bundle results) {
        String text = firstResult(results);
        if (text != null) listener.onPartialTranscript(text);
    }

    @Override public void onEvent(int eventType, Bundle params) { }

    private static String firstResult(Bundle results) {
        ArrayList<String> texts = results == null
            ? null : results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        return texts == null || texts.isEmpty() ? null : texts.get(0);
    }

    private boolean usesLocalAsr() {
        int defaultMode = LocalAsrEngine.isBundled() ? 0 : 1;
        return context.getSharedPreferences("model_channels", Context.MODE_PRIVATE)
            .getInt("asr_mode", defaultMode) == 0;
    }

    private static String errorMessage(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO: return "语音输入发生音频错误";
            case SpeechRecognizer.ERROR_CLIENT: return "语音识别客户端不可用";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "没有麦克风权限";
            case SpeechRecognizer.ERROR_NETWORK: return "语音识别网络不可用";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "语音识别网络超时";
            case SpeechRecognizer.ERROR_NO_MATCH: return "没有识别到命令，请重试一次";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "语音识别服务忙，请稍后重试";
            case SpeechRecognizer.ERROR_SERVER: return "语音识别服务返回错误";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return "等待命令超时";
            default: return "语音识别失败（错误 " + error + "）";
        }
    }
}
