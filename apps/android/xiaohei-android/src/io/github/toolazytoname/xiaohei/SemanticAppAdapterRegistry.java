package io.github.toolazytoname.xiaohei;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Closed registry for semantic Accessibility targets. These are adapter revisions, not claims
 * about an installed app's UI version; a missing exact label remains an explicit failure.
 */
final class SemanticAppAdapterRegistry {
    static final int ADAPTER_REVISION = 1;
    enum Action { SELECT_EXACT_LABEL, SCROLL, BACK }
    enum Code { OK, UNKNOWN_APP, UNSUPPORTED_REVISION, UNSUPPORTED_ACTION, TARGET_DENIED }

    static final class Descriptor {
        final String id;
        final String packageName;
        final int revision;
        final List<Action> actions;
        private Descriptor(String id, String packageName, Action... actions) {
            this.id = id;
            this.packageName = packageName;
            this.revision = ADAPTER_REVISION;
            this.actions = Collections.unmodifiableList(Arrays.asList(actions));
        }
    }

    private static final List<Descriptor> ALL = Collections.unmodifiableList(Arrays.asList(
        app("aosp-settings", "com.android.settings", Action.SELECT_EXACT_LABEL, Action.SCROLL, Action.BACK),
        app("aosp-calculator", "com.android.calculator2", Action.SELECT_EXACT_LABEL, Action.BACK),
        app("aosp-contacts", "com.android.contacts", Action.SELECT_EXACT_LABEL, Action.SCROLL, Action.BACK),
        app("aosp-clock", "com.android.deskclock", Action.SELECT_EXACT_LABEL, Action.SCROLL, Action.BACK),
        app("aosp-dialer", "com.android.dialer", Action.SELECT_EXACT_LABEL, Action.SCROLL, Action.BACK),
        app("aosp-documents", "com.android.documentsui", Action.SELECT_EXACT_LABEL, Action.SCROLL, Action.BACK),
        app("chrome", "com.android.chrome", Action.SELECT_EXACT_LABEL, Action.SCROLL, Action.BACK),
        app("aosp-calendar", "com.android.calendar", Action.SELECT_EXACT_LABEL, Action.SCROLL, Action.BACK),
        app("aosp-camera", "com.android.camera2", Action.SELECT_EXACT_LABEL, Action.BACK),
        app("aosp-gallery", "com.android.gallery3d", Action.SELECT_EXACT_LABEL, Action.SCROLL, Action.BACK),
        app("aosp-messaging", "com.android.messaging", Action.SELECT_EXACT_LABEL, Action.SCROLL, Action.BACK),
        app("webview-shell", "org.chromium.webview_shell", Action.SELECT_EXACT_LABEL, Action.SCROLL, Action.BACK),
        app("fennec", "org.mozilla.fennec_fdroid", Action.SELECT_EXACT_LABEL, Action.SCROLL, Action.BACK),
        app("lineage-aperture", "org.lineageos.aperture", Action.SELECT_EXACT_LABEL, Action.BACK),
        app("lineage-glimpse", "org.lineageos.glimpse", Action.SELECT_EXACT_LABEL, Action.SCROLL, Action.BACK)
    ));
    private static final Map<String, Descriptor> BY_PACKAGE;
    static {
        Map<String, Descriptor> values = new HashMap<>();
        for (Descriptor descriptor : ALL) {
            if (values.put(descriptor.packageName, descriptor) != null || !AgentPolicy.packageAllowed(descriptor.packageName))
                throw new IllegalStateException("invalid semantic adapter " + descriptor.packageName);
        }
        BY_PACKAGE = Collections.unmodifiableMap(values);
    }

    static List<Descriptor> all() { return ALL; }
    static Descriptor lookup(String packageName) { return BY_PACKAGE.get(packageName); }

    static Code assess(String packageName, int revision, Action action, String target) {
        Descriptor descriptor = lookup(packageName);
        if (descriptor == null) return Code.UNKNOWN_APP;
        if (revision != ADAPTER_REVISION) return Code.UNSUPPORTED_REVISION;
        if (action == null || !descriptor.actions.contains(action)) return Code.UNSUPPORTED_ACTION;
        SemanticAccessibilityOperationPolicy.Operation operation = action == Action.SELECT_EXACT_LABEL
            ? SemanticAccessibilityOperationPolicy.Operation.SELECT_EXACT_LABEL
            : action == Action.SCROLL ? SemanticAccessibilityOperationPolicy.Operation.SCROLL_FORWARD
            : SemanticAccessibilityOperationPolicy.Operation.NAVIGATE_BACK;
        return SemanticAccessibilityOperationPolicy.assess(operation, target)
            == SemanticAccessibilityOperationPolicy.Decision.ALLOW ? Code.OK : Code.TARGET_DENIED;
    }

    private static Descriptor app(String id, String packageName, Action... actions) {
        return new Descriptor(id, packageName, actions);
    }
    private SemanticAppAdapterRegistry() {}
}
