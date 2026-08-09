package io.github.toolazytoname.xiaohei;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;

final class XiaoheiVoiceInteractionSession extends VoiceInteractionSession {
    XiaoheiVoiceInteractionSession(Context context) { super(context); }

    @Override public void onShow(Bundle args, int flags) {
        super.onShow(args, flags);
        Intent main = new Intent(getContext(), MainActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtras(args == null ? new Bundle() : args);
        startAssistantActivity(main);
        finish();
    }
}
