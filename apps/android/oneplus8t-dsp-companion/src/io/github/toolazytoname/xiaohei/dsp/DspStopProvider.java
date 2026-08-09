package io.github.toolazytoname.xiaohei.dsp;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

/** Signature-gated emergency stop path. It intentionally cannot arm DSP. */
public final class DspStopProvider extends ContentProvider {
    @Override public boolean onCreate() { return true; }

    @Override public Bundle call(String method, String arg, Bundle extras) {
        if (!"disarm".equals(method)) throw new IllegalArgumentException("unsupported method");
        SoundTriggerGateway.ProbeResult result = SoundTriggerGateway.detach(getContext());
        getContext().stopService(new Intent(getContext(), DspControlService.class));
        Bundle output = new Bundle();
        output.putBoolean("ok", result.ok);
        output.putString("state", SoundTriggerGateway.currentState());
        output.putString("detail", result.detail);
        return output;
    }

    @Override public String getType(Uri uri) { return null; }
    @Override public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) { return null; }
    @Override public Uri insert(Uri uri, ContentValues values) { throw new UnsupportedOperationException(); }
    @Override public int delete(Uri uri, String selection, String[] selectionArgs) { throw new UnsupportedOperationException(); }
    @Override public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) { throw new UnsupportedOperationException(); }
}
