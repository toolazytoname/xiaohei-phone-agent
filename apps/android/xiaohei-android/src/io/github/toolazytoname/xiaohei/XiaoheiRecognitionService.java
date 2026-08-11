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
    private final Object audioLock = new Object();
    private volatile boolean cancelled;
    private Thread worker;
    private AudioRecord activeAudio;

    @Override protected void onStartListening(Intent recognizerIntent, Callback callback) {
        if (!LocalAsrEngine.isBundled() || worker != null) {
            error(callback, LocalAsrEngine.isBundled()
                ? SpeechRecognizer.ERROR_RECOGNIZER_BUSY : SpeechRecognizer.ERROR_CLIENT);
            return;
        }
        AsrProfile profile = AsrProfile.fromId(
            recognizerIntent.getStringExtra(VoiceCommandSession.EXTRA_ASR_PROFILE));
        if (profile == null) {
            Log.i(TAG, "capture_rejected invalid_asr_profile=true");
            error(callback, SpeechRecognizer.ERROR_CLIENT);
            return;
        }
        ProcessAudioDuplex.Lease inputLease = ProcessAudioDuplex.shared().acquireInput();
        if (inputLease == null) {
            Log.i(TAG, "capture_rejected output_owner_active=true");
            error(callback, SpeechRecognizer.ERROR_RECOGNIZER_BUSY);
            return;
        }
        cancelled = false;
        long maximumMs = 8000;
        if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            maximumMs = Math.max(8000, Math.min(120000,
                recognizerIntent.getLongExtra(VoiceCommandSession.EXTRA_MAX_DURATION_MS, 8000)));
        }
        final long boundedMaximumMs = maximumMs;
        worker = new Thread(() -> recognize(callback, boundedMaximumMs, inputLease, profile),
            "xiaohei-local-asr");
        worker.start();
    }

    @Override protected void onStopListening(Callback callback) { cancelCapture(); }

    @Override protected void onCancel(Callback callback) { cancelCapture(); }

    @Override public void onDestroy() {
        cancelCapture();
        super.onDestroy();
    }

    private void cancelCapture() {
        cancelled = true;
        synchronized (audioLock) {
            if (activeAudio != null) {
                try { activeAudio.stop(); } catch (IllegalStateException ignored) { }
            }
        }
    }

    private void recognize(Callback callback, long maximumMs, ProcessAudioDuplex.Lease inputLease,
                           AsrProfile profile) {
        AudioRecord audio = null;
        long startedAt = System.currentTimeMillis();
        try (LocalAsrEngine engine = new LocalAsrEngine(this, profile)) {
            int minimum = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
            audio = createRecognitionAudioRecord(minimum);
            if (audio.getState() != AudioRecord.STATE_INITIALIZED) {
                error(callback, SpeechRecognizer.ERROR_AUDIO);
                return;
            }
            synchronized (audioLock) {
                if (cancelled) {
                    Log.i(TAG, "capture_start_cancelled before_audio_start=true");
                    return;
                }
                activeAudio = audio;
                audio.startRecording();
                Log.i(TAG, "capture_started source=" + audio.getAudioSource()
                    + " maximum_ms=" + maximumMs + " profile=" + profile.id());
            }
            if (cancelled) return;
            callback.readyForSpeech(new Bundle());
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
            if (text.isEmpty()) {
                Log.i(TAG, "recognition_finished outcome=no_match elapsed_ms="
                    + (System.currentTimeMillis() - startedAt));
                error(callback, SpeechRecognizer.ERROR_NO_MATCH);
            } else {
                // Transcript logging is intentionally debug-only: release
                // builds expose an outcome and length, never spoken content.
                String detail = (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0
                    ? " transcript=" + text : " transcript_chars=" + text.length();
                Log.i(TAG, "recognition_finished outcome=result elapsed_ms="
                    + (System.currentTimeMillis() - startedAt) + detail);
                callback.results(results(text));
            }
        } catch (SecurityException denied) {
            Log.e(TAG, "microphone permission denied", denied);
            error(callback, SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS);
        } catch (Exception failure) {
            Log.e(TAG, "local ASR failed: " + failure.getClass().getSimpleName()
                + ": " + String.valueOf(failure.getMessage()), failure);
            error(callback, SpeechRecognizer.ERROR_CLIENT);
        } finally {
            if (audio != null) {
                synchronized (audioLock) {
                    if (activeAudio == audio) activeAudio = null;
                }
                try { audio.stop(); } catch (IllegalStateException ignored) { }
                audio.release();
            }
            ProcessAudioDuplex.shared().release(inputLease);
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

    /** Prefer the platform's voice path; some devices apply recognition-tuned
     * processing there. Keep a MIC fallback for ROMs that do not implement it. */
    private static AudioRecord createRecognitionAudioRecord(int minimum) {
        int bufferSize = Math.max(minimum * 2, 3200);
        AudioRecord recognition = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
            16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        if (recognition.getState() == AudioRecord.STATE_INITIALIZED) return recognition;
        recognition.release();
        return new AudioRecord(MediaRecorder.AudioSource.MIC, 16000,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
    }

    private static void error(Callback callback, int code) {
        try { callback.error(code); } catch (RemoteException ignored) { }
    }
}
