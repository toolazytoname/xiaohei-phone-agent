package io.github.toolazytoname.xiaohei;

import android.content.Intent;
import android.os.RemoteException;
import android.speech.RecognitionService;
import android.speech.SpeechRecognizer;

/** Stable ASR interface; returns an honest error until an independent engine is configured. */
public final class XiaoheiRecognitionService extends RecognitionService {
    @Override protected void onStartListening(Intent recognizerIntent, Callback callback) {
        unavailable(callback);
    }

    @Override protected void onStopListening(Callback callback) { }

    @Override protected void onCancel(Callback callback) { }

    private static void unavailable(Callback callback) {
        try {
            callback.error(SpeechRecognizer.ERROR_CLIENT);
        } catch (RemoteException ignored) { }
    }
}
