package io.github.toolazytoname.xiaohei;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.RemoteException;
import android.speech.RecognitionService;
import android.speech.SpeechRecognizer;
import android.util.Log;
import java.util.ArrayList;

/** App-owned, bounded offline Chinese ASR service. */
public final class XiaoheiRecognitionService extends RecognitionService {
    private static final String TAG = "XiaoheiLocalAsr";
    private volatile boolean cancelled;
    private Thread worker;

    @Override protected void onStartListening(Intent recognizerIntent, Callback callback) {
        if (!LocalAsrEngine.isBundled() || worker != null) {
            error(callback, LocalAsrEngine.isBundled()
                ? SpeechRecognizer.ERROR_RECOGNIZER_BUSY : SpeechRecognizer.ERROR_CLIENT);
            return;
        }
        cancelled = false;
        long maximumMs = 8000;
        if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            maximumMs = Math.max(8000, Math.min(120000,
                recognizerIntent.getLongExtra(VoiceCommandSession.EXTRA_MAX_DURATION_MS, 8000)));
        }
        final long boundedMaximumMs = maximumMs;
        worker = new Thread(() -> recognize(callback, boundedMaximumMs), "xiaohei-local-asr");
        worker.start();
    }

    @Override protected void onStopListening(Callback callback) { cancelled = true; }

    @Override protected void onCancel(Callback callback) { cancelled = true; }

    private void recognize(Callback callback, long maximumMs) {
        AudioRecord audio = null;
        try (LocalAsrEngine engine = new LocalAsrEngine(this)) {
            int minimum = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
            audio = new AudioRecord(MediaRecorder.AudioSource.MIC, 16000,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                Math.max(minimum * 2, 3200));
            if (audio.getState() != AudioRecord.STATE_INITIALIZED) {
                error(callback, SpeechRecognizer.ERROR_AUDIO);
                return;
            }
            callback.readyForSpeech(new Bundle());
            audio.startRecording();
            callback.beginningOfSpeech();
            short[] buffer = new short[1600];
            long deadline = System.currentTimeMillis() + maximumMs;
            String last = "";
            while (!cancelled && System.currentTimeMillis() < deadline) {
                int count = audio.read(buffer, 0, buffer.length);
                if (count <= 0) continue;
                engine.accept(buffer, count);
                String text = engine.text();
                if (!text.isEmpty() && !text.equals(last)) {
                    last = text;
                    callback.partialResults(results(text));
                }
                if (engine.isEndpoint() && !text.isEmpty()) break;
            }
            callback.endOfSpeech();
            if (cancelled) return;
            String text = engine.text();
            if (text.isEmpty()) error(callback, SpeechRecognizer.ERROR_NO_MATCH);
            else callback.results(results(text));
        } catch (SecurityException denied) {
            Log.e(TAG, "microphone permission denied", denied);
            error(callback, SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS);
        } catch (Exception failure) {
            Log.e(TAG, "local ASR failed: " + failure.getClass().getSimpleName()
                + ": " + String.valueOf(failure.getMessage()), failure);
            error(callback, SpeechRecognizer.ERROR_CLIENT);
        } finally {
            if (audio != null) {
                try { audio.stop(); } catch (IllegalStateException ignored) { }
                audio.release();
            }
            worker = null;
        }
    }

    private static Bundle results(String text) {
        Bundle bundle = new Bundle();
        ArrayList<String> values = new ArrayList<>();
        values.add(text);
        bundle.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, values);
        return bundle;
    }

    private static void error(Callback callback, int code) {
        try { callback.error(code); } catch (RemoteException ignored) { }
    }
}
