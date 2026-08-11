package io.github.toolazytoname.xiaohei;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import java.util.Locale;
import java.util.UUID;

/** System TTS only: bounded text, timeout, stop, and complete shutdown. */
final class SystemTtsAdapter {
    interface Listener { void onState(TtsLifecycle.State state, String detail); }
    private static final String TAG = "XiaoheiTtsQueue";
    private static final int MAX_CHARS = 2048;
    private static final long TIMEOUT_MS = 30000;
    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final TtsLifecycle lifecycle = new TtsLifecycle();
    private final SentenceTtsQueue queue = new SentenceTtsQueue();
    private TextToSpeech engine;
    private Listener listener;
    private volatile String utteranceId;
    private long utteranceGeneration;
    private int utteranceSequence;
    private long utteranceRequestedAt;
    private ProcessAudioDuplex.Lease outputLease;
    private final Runnable timeout = () -> { if (lifecycle.state() == TtsLifecycle.State.SPEAKING) { stop("播报超时，已停止"); } };

    SystemTtsAdapter(Context context) { this.context = context.getApplicationContext(); }
    TtsLifecycle.State state() { return lifecycle.state(); }

    void initialize(Listener listener) {
        this.listener = listener;
        if (!lifecycle.initialize()) { report("TTS 当前状态不可初始化"); return; }
        report("正在初始化系统 TTS");
        engine = new TextToSpeech(context, status -> {
            synchronized (SystemTtsAdapter.this) {
                if (lifecycle.state() != TtsLifecycle.State.INITIALIZING || engine == null) {
                    Log.i(TAG, "initialization_ignored terminal_state=" + lifecycle.state().name());
                    return;
                }
                if (status != TextToSpeech.SUCCESS) {
                    lifecycle.initialized(false);
                    report("系统 TTS 初始化失败");
                    return;
                }
                engine.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override public void onStart(String id) {
                    synchronized (SystemTtsAdapter.this) {
                        if (!id.equals(utteranceId)) {
                            Log.i(TAG, "start_ignored stale_utterance=true");
                            return;
                        }
                        long latency = Math.max(0, SystemClock.elapsedRealtime() - utteranceRequestedAt);
                        Log.i(TAG, "sentence_started generation=" + utteranceGeneration
                            + " sequence=" + utteranceSequence + " callback_latency_ms=" + latency);
                    }
                }
                @Override public void onDone(String id) {
                    synchronized (SystemTtsAdapter.this) {
                        if (!id.equals(utteranceId)) {
                            Log.i(TAG, "completion_ignored stale_utterance=true");
                            return;
                        }
                        if (!lifecycle.finished()) {
                            Log.i(TAG, "completion_ignored terminal_state=" + lifecycle.state().name());
                            return;
                        }
                        handler.removeCallbacks(timeout);
                        long completedGeneration = utteranceGeneration;
                        int completedSequence = utteranceSequence;
                        SentenceTtsQueue.Next next = queue.complete(utteranceGeneration);
                        if (next == null) {
                            releaseOutput();
                            Log.i(TAG, "queue_finished generation=" + completedGeneration
                                + " final_sequence=" + completedSequence + " pending=0");
                            report("播报完成；等待后续输入");
                            return;
                        }
                        speakNext(next);
                    }
                }
                @Override public void onError(String id) {
                    synchronized (SystemTtsAdapter.this) {
                        if (id.equals(utteranceId) && lifecycle.failSpeaking()) {
                            handler.removeCallbacks(timeout);
                            releaseOutput();
                            Log.i(TAG, "queue_failed generation=" + utteranceGeneration
                                + " sequence=" + utteranceSequence);
                            report("系统 TTS 播报失败");
                        }
                    }
                }
                });
                lifecycle.initialized(true);
                report("系统 TTS 已就绪");
            }
        });
    }

    synchronized void speak(String text) {
        if (text == null || text.trim().isEmpty() || text.length() > MAX_CHARS) { lifecycle.fail(); report("播报文本为空或超过限制"); return; }
        if (engine == null) { report("系统 TTS 未就绪"); return; }
        if (lifecycle.state() == TtsLifecycle.State.INTERRUPTED
                && !lifecycle.acknowledgeInterruption()) {
            report("系统 TTS 中断状态无法恢复");
            return;
        }
        if (!acquireOutput()) { report("麦克风正在使用；已拒绝同时播报"); return; }
        SentenceTtsQueue.Next first = queue.replace(text);
        if (first == null || !lifecycle.speak()) {
            releaseOutput();
            report("系统 TTS 未就绪");
            return;
        }
        Log.i(TAG, "queue_created generation=" + first.generation
            + " sentences=" + queue.pending());
        speakNext(first);
    }

    private synchronized void speakNext(SentenceTtsQueue.Next next) {
        if (lifecycle.state() == TtsLifecycle.State.WAITING_FOLLOWUP && !lifecycle.speak()) {
            releaseOutput();
            report("系统 TTS 未就绪");
            return;
        }
        utteranceGeneration = next.generation;
        utteranceSequence = next.sequence;
        utteranceRequestedAt = SystemClock.elapsedRealtime();
        utteranceId = UUID.randomUUID().toString();
        int result = engine.speak(next.sentence, TextToSpeech.QUEUE_FLUSH, new Bundle(), utteranceId);
        if (result != TextToSpeech.SUCCESS) {
            lifecycle.fail();
            releaseOutput();
            Log.i(TAG, "queue_rejected generation=" + utteranceGeneration
                + " sequence=" + utteranceSequence);
            report("系统拒绝播报请求");
            return;
        }
        handler.postDelayed(timeout, TIMEOUT_MS);
        Log.i(TAG, "sentence_submitted generation=" + utteranceGeneration
            + " sequence=" + utteranceSequence + " pending=" + queue.pending());
        report("正在播报");
    }

    void stop(String detail) {
        TextToSpeech current;
        boolean changed;
        synchronized (this) {
            handler.removeCallbacks(timeout);
            cancelQueue("stop");
            changed = lifecycle.stop();
            utteranceId = null;
            current = engine;
            releaseOutput();
        }
        if (current != null) current.stop();
        if (changed) report(detail);
    }

    /** User/system interruption is distinct from a completed utterance and never resumes audio. */
    void interrupt(String detail) {
        TextToSpeech current;
        boolean changed;
        synchronized (this) {
            handler.removeCallbacks(timeout);
            cancelQueue("interrupt");
            changed = lifecycle.interrupt();
            utteranceId = null;
            current = engine;
            releaseOutput();
        }
        if (current != null) current.stop();
        if (changed) report(detail);
    }

    void destroy() {
        TextToSpeech current;
        synchronized (this) {
            handler.removeCallbacksAndMessages(null);
            cancelQueue("destroy");
            lifecycle.destroy();
            utteranceId = null;
            current = engine;
            engine = null;
            releaseOutput();
        }
        if (current != null) { current.stop(); current.shutdown(); }
        report("系统 TTS 已释放");
    }

    private synchronized boolean acquireOutput() {
        if (outputLease != null) return true;
        outputLease = ProcessAudioDuplex.shared().acquireOutput();
        return outputLease != null;
    }

    private synchronized void releaseOutput() {
        ProcessAudioDuplex.Lease lease = outputLease;
        outputLease = null;
        ProcessAudioDuplex.shared().release(lease);
    }

    private void cancelQueue(String reason) {
        long cancelledGeneration = queue.generation();
        int dropped = queue.pending();
        queue.cancel();
        Log.i(TAG, "queue_cancelled reason=" + reason + " generation="
            + cancelledGeneration + " dropped=" + dropped);
    }

    private void report(String detail) { if (listener != null) listener.onState(lifecycle.state(), detail); }
}
