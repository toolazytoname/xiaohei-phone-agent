package io.github.toolazytoname.xiaohei;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/** Explicit signature-bound control client for an optional device profile. */
final class DspProfileClient {
    private static final String TAG = "XiaoheiDspClient";
    static final String PACKAGE = "io.github.toolazytoname.xiaohei.dsp";
    static final String SERVICE = PACKAGE + ".DspControlService";
    static final String ACTION_ARM = PACKAGE + ".action.ARM";
    static final String ACTION_DISARM = PACKAGE + ".action.DISARM";
    static final String ACTION_STATUS = PACKAGE + ".action.STATUS";
    static final String STATUS_EVENT = "io.github.toolazytoname.xiaohei.action.DSP_STATUS";

    static final class Status {
        final boolean ok;
        final String state;
        final String detail;
        Status(boolean ok, String state, String detail) {
            this.ok = ok; this.state = state; this.detail = detail;
        }
    }

    private final Context context;
    DspProfileClient(Context context) { this.context = context.getApplicationContext(); }

    boolean isInstalled() {
        try {
            context.getPackageManager().getPackageInfo(PACKAGE, 0);
            return true;
        } catch (Exception unavailable) { return false; }
    }

    boolean arm() { return send(ACTION_ARM, true); }
    boolean disarm() {
        try {
            Bundle result = context.getContentResolver().call(
                Uri.parse("content://io.github.toolazytoname.xiaohei.dsp.stop"),
                "disarm", null, null);
            return result != null && result.getBoolean("ok");
        } catch (RuntimeException unavailable) {
            Log.e(TAG, "DSP stop unavailable: " + unavailable.getClass().getSimpleName()
                + ": " + String.valueOf(unavailable.getMessage()));
            return false;
        }
    }
    Status status() {
        try {
            Bundle result = context.getContentResolver().call(
                Uri.parse("content://io.github.toolazytoname.xiaohei.dsp.status"),
                "status", null, null);
            if (result == null) return null;
            return new Status(result.getBoolean("ok"), result.getString("state"),
                result.getString("detail"));
        } catch (RuntimeException unavailable) {
            Log.e(TAG, "DSP status unavailable: " + unavailable.getClass().getSimpleName()
                + ": " + String.valueOf(unavailable.getMessage()));
            return null;
        }
    }

    private boolean send(String action, boolean foreground) {
        Intent intent = new Intent(action).setComponent(new ComponentName(PACKAGE, SERVICE));
        try {
            if (foreground) context.startForegroundService(intent); else context.startService(intent);
            return true;
        } catch (RuntimeException unavailable) {
            Log.e(TAG, "DSP control unavailable: " + unavailable.getClass().getSimpleName()
                + ": " + String.valueOf(unavailable.getMessage()));
            return false;
        }
    }
}
