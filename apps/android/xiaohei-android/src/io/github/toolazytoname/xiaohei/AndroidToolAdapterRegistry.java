package io.github.toolazytoname.xiaohei;

import android.content.Context;

/** Closed platform-adapter registry; authorization and execution remain external. */
final class AndroidToolAdapterRegistry {
    private final ToolExecutionCoordinator.Adapter media;
    private final ToolExecutionCoordinator.Adapter calendar;

    AndroidToolAdapterRegistry(Context context) {
        if (context == null) throw new IllegalArgumentException("context required");
        media = new MediaStoreTestCollectionAdapter(context);
        calendar = new CalendarTestAccountAdapter(context);
    }

    ToolExecutionCoordinator.Adapter resolve(ToolGateway.Call call) {
        if (call == null || call.audience != ToolCatalog.Audience.ANDROID_GATEWAY) return null;
        if ("android.media_test_collection".equals(call.tool)) return media;
        if ("android.calendar_test_account".equals(call.tool)) return calendar;
        return null;
    }
}
