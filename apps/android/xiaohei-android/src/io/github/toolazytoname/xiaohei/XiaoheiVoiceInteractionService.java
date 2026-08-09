package io.github.toolazytoname.xiaohei;

import android.os.Bundle;
import android.service.voice.VoiceInteractionService;

/** System-selected Assistant service; it owns legal session display on Android 14. */
public final class XiaoheiVoiceInteractionService extends VoiceInteractionService {
    private static XiaoheiVoiceInteractionService active;

    @Override public void onReady() {
        super.onReady();
        active = this;
    }

    @Override public void onShutdown() {
        if (active == this) active = null;
        super.onShutdown();
    }

    static boolean showWakeSession(Bundle args) {
        XiaoheiVoiceInteractionService service = active;
        if (service == null) return false;
        service.showSession(args, 0);
        return true;
    }
}
