package io.github.toolazytoname.xiaohei;

import java.util.HashSet;

public final class SemanticAppAdapterRegistryTest {
    public static void main(String[] args) {
        if (SemanticAppAdapterRegistry.all().size() != 15) throw new AssertionError("expected 15 adapters");
        HashSet<String> ids = new HashSet<>();
        HashSet<String> packages = new HashSet<>();
        for (SemanticAppAdapterRegistry.Descriptor value : SemanticAppAdapterRegistry.all()) {
            if (!ids.add(value.id) || !packages.add(value.packageName)
                    || value.revision != 1 || !AgentPolicy.packageAllowed(value.packageName))
                throw new AssertionError("invalid descriptor");
        }
        expect(SemanticAppAdapterRegistry.Code.OK, "com.android.settings", 1,
            SemanticAppAdapterRegistry.Action.SELECT_EXACT_LABEL, "网络和互联网");
        expect(SemanticAppAdapterRegistry.Code.OK, "com.android.chrome", 1,
            SemanticAppAdapterRegistry.Action.SCROLL, null);
        expect(SemanticAppAdapterRegistry.Code.TARGET_DENIED, "com.android.settings", 1,
            SemanticAppAdapterRegistry.Action.SELECT_EXACT_LABEL, "允许");
        expect(SemanticAppAdapterRegistry.Code.UNSUPPORTED_ACTION, "com.android.camera2", 1,
            SemanticAppAdapterRegistry.Action.SCROLL, null);
        expect(SemanticAppAdapterRegistry.Code.UNSUPPORTED_REVISION, "com.android.settings", 2,
            SemanticAppAdapterRegistry.Action.BACK, null);
        expect(SemanticAppAdapterRegistry.Code.UNKNOWN_APP, "com.example.unknown", 1,
            SemanticAppAdapterRegistry.Action.BACK, null);
        System.out.println("PASS semantic-app-adapters descriptors=15 safe_actions=3 failures=4 app_ui_assumptions=0");
    }
    private static void expect(SemanticAppAdapterRegistry.Code expected, String pkg, int revision,
            SemanticAppAdapterRegistry.Action action, String target) {
        SemanticAppAdapterRegistry.Code actual = SemanticAppAdapterRegistry.assess(pkg, revision, action, target);
        if (actual != expected) throw new AssertionError("expected=" + expected + " actual=" + actual);
    }
}
