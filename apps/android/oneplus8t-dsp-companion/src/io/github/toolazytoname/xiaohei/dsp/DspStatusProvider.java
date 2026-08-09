package io.github.toolazytoname.xiaohei.dsp;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

/** Read-only signature-gated status endpoint; it never arms or loads a model. */
public final class DspStatusProvider extends ContentProvider {
    @Override public boolean onCreate() { return true; }

    @Override public Bundle call(String method, String arg, Bundle extras) {
        Bundle result = new Bundle();
        SoundTriggerGateway.ProbeResult probe = SoundTriggerGateway.probe(getContext());
        result.putBoolean("ok", probe.ok);
        result.putString("state", SoundTriggerGateway.currentState());
        result.putString("detail", probe.detail);
        return result;
    }

    @Override public String getType(Uri uri) { return null; }
    @Override public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) { return null; }
    @Override public Uri insert(Uri uri, ContentValues values) { throw new UnsupportedOperationException(); }
    @Override public int delete(Uri uri, String selection, String[] selectionArgs) { throw new UnsupportedOperationException(); }
    @Override public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) { throw new UnsupportedOperationException(); }
}
