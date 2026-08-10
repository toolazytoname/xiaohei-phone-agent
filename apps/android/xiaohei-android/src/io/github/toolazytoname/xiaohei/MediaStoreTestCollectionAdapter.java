package io.github.toolazytoname.xiaohei;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Android adapter limited to the app-designated disposable MediaStore test collection. */
final class MediaStoreTestCollectionAdapter implements ToolExecutionCoordinator.Adapter {
    private static final String[] PROJECTION = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME};
    private final ContentResolver resolver;
    private final Map<String, Undo> undo = new HashMap<>();

    private static final class Undo {
        final MediaTestCollectionPolicy.Operation operation; final Uri uri; final String originalName;
        Undo(MediaTestCollectionPolicy.Operation operation, Uri uri, String originalName) {
            this.operation = operation; this.uri = uri; this.originalName = originalName;
        }
    }

    MediaStoreTestCollectionAdapter(Context context) { resolver = context.getApplicationContext().getContentResolver(); }

    @Override public ToolExecutionCoordinator.AdapterResponse execute(ToolGateway.Call call,
            ToolExecutionCoordinator.CancellationSignal cancellation) throws ToolExecutionCoordinator.AdapterFailure {
        if (call == null || !"android.media_test_collection".equals(call.tool)
                || MediaTestCollectionPolicy.validate(call.arguments) != MediaTestCollectionPolicy.Code.OK)
            throw new ToolExecutionCoordinator.AdapterFailure(ToolExecutionCoordinator.ErrorCode.INVALID_OUTPUT);
        MediaTestCollectionPolicy.Request request = MediaTestCollectionPolicy.parse(call.arguments);
        if (request == null || cancellation.isCancelled())
            throw new ToolExecutionCoordinator.AdapterFailure(ToolExecutionCoordinator.ErrorCode.USER_CANCELLED);
        switch (request.operation) {
            case QUERY: return response(queryCount());
            case COPY: return copy(request, cancellation);
            case MOVE: return move(request);
            case ROLLBACK: return rollback(request);
            default: throw new ToolExecutionCoordinator.AdapterFailure(ToolExecutionCoordinator.ErrorCode.INVALID_OUTPUT);
        }
    }

    private ToolExecutionCoordinator.AdapterResponse response(int count) {
        Map<String, String> result = new HashMap<>(); result.put("collection_count", String.valueOf(count));
        return new ToolExecutionCoordinator.AdapterResponse(ToolExecutionCoordinator.AdapterStatus.SUCCESS, result);
    }

    private ToolExecutionCoordinator.AdapterResponse copy(MediaTestCollectionPolicy.Request request,
            ToolExecutionCoordinator.CancellationSignal cancellation) throws ToolExecutionCoordinator.AdapterFailure {
        Source source = source(request.sourceId); if (source == null) throw denied();
        ContentValues values = new ContentValues(); values.put(MediaStore.Images.Media.DISPLAY_NAME, request.destinationName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); values.put(MediaStore.Images.Media.RELATIVE_PATH, MediaTestCollectionPolicy.RELATIVE_PATH);
        Uri target = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values); if (target == null) throw denied();
        try (InputStream input = resolver.openInputStream(source.uri); OutputStream output = resolver.openOutputStream(target)) {
            if (input == null || output == null) throw denied(); byte[] buffer = new byte[8192]; int read;
            while ((read = input.read(buffer)) >= 0) { if (cancellation.isCancelled()) { resolver.delete(target, null, null); throw cancelled(); } output.write(buffer, 0, read); }
        } catch (java.io.IOException failure) { resolver.delete(target, null, null); throw denied(); }
        String id = UUID.randomUUID().toString(); undo.put(id, new Undo(MediaTestCollectionPolicy.Operation.COPY, target, ""));
        Map<String, String> result = new HashMap<>(); result.put("rollback_id", id); result.put("operation", "copy");
        return new ToolExecutionCoordinator.AdapterResponse(ToolExecutionCoordinator.AdapterStatus.SUCCESS, result);
    }

    private ToolExecutionCoordinator.AdapterResponse move(MediaTestCollectionPolicy.Request request) throws ToolExecutionCoordinator.AdapterFailure {
        Source source = source(request.sourceId); if (source == null) throw denied(); ContentValues values = new ContentValues(); values.put(MediaStore.Images.Media.DISPLAY_NAME, request.destinationName);
        if (resolver.update(source.uri, values, null, null) != 1) throw denied(); String id = UUID.randomUUID().toString(); undo.put(id, new Undo(MediaTestCollectionPolicy.Operation.MOVE, source.uri, source.name));
        Map<String, String> result = new HashMap<>(); result.put("rollback_id", id); result.put("operation", "move");
        return new ToolExecutionCoordinator.AdapterResponse(ToolExecutionCoordinator.AdapterStatus.SUCCESS, result);
    }

    private ToolExecutionCoordinator.AdapterResponse rollback(MediaTestCollectionPolicy.Request request) throws ToolExecutionCoordinator.AdapterFailure {
        Undo prior = undo.remove(request.rollbackId); if (prior == null) throw denied(); boolean restored;
        if (prior.operation == MediaTestCollectionPolicy.Operation.COPY) restored = resolver.delete(prior.uri, null, null) == 1;
        else { ContentValues values = new ContentValues(); values.put(MediaStore.Images.Media.DISPLAY_NAME, prior.originalName); restored = resolver.update(prior.uri, values, null, null) == 1; }
        if (!restored) throw denied(); Map<String, String> result = new HashMap<>(); result.put("rolled_back", "true"); return new ToolExecutionCoordinator.AdapterResponse(ToolExecutionCoordinator.AdapterStatus.SUCCESS, result);
    }

    private int queryCount() { try (Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, PROJECTION, MediaStore.Images.Media.RELATIVE_PATH + "=?", new String[] {MediaTestCollectionPolicy.RELATIVE_PATH}, null)) { return cursor == null ? 0 : cursor.getCount(); } }
    private Source source(long id) { Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id); try (Cursor cursor = resolver.query(uri, new String[] {MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.RELATIVE_PATH}, null, null, null)) { if (cursor == null || !cursor.moveToFirst() || !MediaTestCollectionPolicy.RELATIVE_PATH.equals(cursor.getString(1))) return null; return new Source(uri, cursor.getString(0)); } }
    private static final class Source { final Uri uri; final String name; Source(Uri uri, String name) { this.uri = uri; this.name = name; } }
    private static ToolExecutionCoordinator.AdapterFailure denied() { return new ToolExecutionCoordinator.AdapterFailure(ToolExecutionCoordinator.ErrorCode.ADAPTER_FAILURE); }
    private static ToolExecutionCoordinator.AdapterFailure cancelled() { return new ToolExecutionCoordinator.AdapterFailure(ToolExecutionCoordinator.ErrorCode.USER_CANCELLED); }
}
