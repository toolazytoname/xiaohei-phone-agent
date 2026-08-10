package io.github.toolazytoname.xiaohei;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Read-only TTS capability probe. It never speaks or starts an engine download. */
final class SystemTtsProbe {
    interface Listener { void onResult(Result result); }
    static final class Result {
        final String state;
        final String detail;
        Result(String state, String detail) { this.state = state; this.detail = detail; }
    }
    private static final String TAG = "XiaoheiTts";
    private final Context context;
    private TextToSpeech engine;

    SystemTtsProbe(Context context) { this.context = context.getApplicationContext(); }

    void refresh(Listener listener) {
        List<ResolveInfo> services = context.getPackageManager().queryIntentServices(
            new Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE), 0);
        if (services == null || services.isEmpty()) {
            deliver(listener, new Result("UNAVAILABLE", "未发现已注册系统 TTS 引擎；不会自动下载"));
            return;
        }
        shutdown();
        engine = new TextToSpeech(context, status -> {
            if (status != TextToSpeech.SUCCESS) {
                shutdown();
                deliver(listener, new Result("ERROR", "系统 TTS 初始化失败；未播放或下载任何内容"));
                return;
            }
            int simplified = engine.isLanguageAvailable(Locale.SIMPLIFIED_CHINESE);
            int traditional = engine.isLanguageAvailable(Locale.TRADITIONAL_CHINESE);
            Set<Voice> voices = engine.getVoices();
            int chinese = 0;
            int offline = 0;
            if (voices != null) for (Voice voice : voices) {
                if ("zh".equalsIgnoreCase(voice.getLocale().getLanguage())) {
                    chinese++;
                    if (!voice.isNetworkConnectionRequired()) offline++;
                }
            }
            String name = engine.getDefaultEngine();
            shutdown();
            deliver(listener, new Result("READY", "引擎=" + safe(name) + "；中文音色=" + chinese
                + "（离线=" + offline + "）；简体=" + simplified + "；繁体=" + traditional));
        });
    }

    void shutdown() { if (engine != null) { engine.shutdown(); engine = null; } }
    private static void deliver(Listener listener, Result result) { Log.i(TAG, result.state + " " + result.detail); listener.onResult(result); }
    private static String safe(String value) { return value == null ? "unknown" : value.substring(0, Math.min(value.length(), 96)); }
}
