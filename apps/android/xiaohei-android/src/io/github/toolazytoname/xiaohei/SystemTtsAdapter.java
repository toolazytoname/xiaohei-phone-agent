package io.github.toolazytoname.xiaohei;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import java.util.Locale;
import java.util.UUID;

/** System TTS only: bounded text, timeout, stop, and complete shutdown. */
final class SystemTtsAdapter {
    interface Listener { void onState(TtsLifecycle.State state, String detail); }
    private static final int MAX_CHARS = 2048;
    private static final long TIMEOUT_MS = 30000;
    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final TtsLifecycle lifecycle = new TtsLifecycle();
    private TextToSpeech engine;
    private Listener listener;
    private String utteranceId;
    private final Runnable timeout = () -> { if (lifecycle.state() == TtsLifecycle.State.SPEAKING) { stop("播报超时，已停止"); } };

    SystemTtsAdapter(Context context) { this.context = context.getApplicationContext(); }
    TtsLifecycle.State state() { return lifecycle.state(); }

    void initialize(Listener listener) {
        this.listener = listener;
        if (!lifecycle.initialize()) { report("TTS 当前状态不可初始化"); return; }
        report("正在初始化系统 TTS");
        engine = new TextToSpeech(context, status -> {
            if (status != TextToSpeech.SUCCESS) { lifecycle.initialized(false); report("系统 TTS 初始化失败"); return; }
            engine.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override public void onStart(String id) { }
                @Override public void onDone(String id) { if (id.equals(utteranceId) && lifecycle.finished()) report("播报完成"); }
                @Override public void onError(String id) { if (id.equals(utteranceId)) { lifecycle.fail(); report("系统 TTS 播报失败"); } }
            });
            lifecycle.initialized(true);
            report("系统 TTS 已就绪");
        });
    }

    void speak(String text) {
        if (text == null || text.trim().isEmpty() || text.length() > MAX_CHARS) { lifecycle.fail(); report("播报文本为空或超过限制"); return; }
        if (!lifecycle.speak() || engine == null) { report("系统 TTS 未就绪"); return; }
        utteranceId = UUID.randomUUID().toString();
        int result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, new Bundle(), utteranceId);
        if (result != TextToSpeech.SUCCESS) { lifecycle.fail(); report("系统拒绝播报请求"); return; }
        handler.postDelayed(timeout, TIMEOUT_MS);
        report("正在播报");
    }

    void stop(String detail) {
        handler.removeCallbacks(timeout);
        if (engine != null) engine.stop();
        if (lifecycle.stop()) report(detail);
    }

    void destroy() {
        handler.removeCallbacksAndMessages(null);
        if (engine != null) { engine.stop(); engine.shutdown(); engine = null; }
        lifecycle.destroy();
        report("系统 TTS 已释放");
    }

    private void report(String detail) { if (listener != null) listener.onState(lifecycle.state(), detail); }
}
