package io.github.toolazytoname.xiaohei;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ToolCatalogTest {
    public static void main(String[] args) {
        builtInDescriptorsAreComplete();
        rejectsThreeDuplicates();
        rejectsThreeUnknownVersions();
        rejectsFiveMissingFields();
        rejectsFourRollbackErrors();
        rejectsUnknownToolsAndRiskMismatch();
        System.out.println("PASS ToolCatalogTest descriptors=7 lookup=7 duplicate=3 unknown_version=3 missing=5 rollback=4 unknown_tool=5 risk_mismatch=4 immutable=true execution_paths=0");
    }

    private static void builtInDescriptorsAreComplete() {
        List<ToolCatalog.Descriptor> descriptors = ToolCatalog.all();
        check(descriptors.size() == 7, "exact built-in count");
        for (String name : new String[] {
                "android.open_settings", "android.open_gallery", "android.open_dialer",
                "android.adjust_volume", "android.observe", "android.media_test_collection", "android.calendar_test_account"
        }) {
            ToolCatalog.Descriptor descriptor = ToolCatalog.lookup(name, 1);
            check(descriptor != null && descriptor.version == 1, "lookup " + name);
            check(descriptor.inputSchema.startsWith("xiaohei.tool.input."), "input schema");
            check(descriptor.outputSchema.startsWith("xiaohei.tool.output."), "output schema");
            check(descriptor.audience == ToolCatalog.Audience.ANDROID_GATEWAY, "audience");
            check(descriptor.timeoutMs >= 100 && descriptor.timeoutMs <= 60000, "timeout");
        }
        expectDescriptor("android.open_settings", ToolCatalog.Risk.LOW,
                "xiaohei.tool.input.empty.v1", "xiaohei.tool.output.activity.v1",
                ToolCatalog.RollbackMode.NONE, "", 5000);
        expectDescriptor("android.open_gallery", ToolCatalog.Risk.LOW,
                "xiaohei.tool.input.empty.v1", "xiaohei.tool.output.activity.v1",
                ToolCatalog.RollbackMode.NONE, "", 5000);
        expectDescriptor("android.open_dialer", ToolCatalog.Risk.LOW,
                "xiaohei.tool.input.empty.v1", "xiaohei.tool.output.activity.v1",
                ToolCatalog.RollbackMode.NONE, "", 5000);
        expectDescriptor("android.adjust_volume", ToolCatalog.Risk.REVERSIBLE,
                "xiaohei.tool.input.volume.v1", "xiaohei.tool.output.volume.v1",
                ToolCatalog.RollbackMode.RESTORE_SNAPSHOT, "android.adjust_volume", 3000);
        expectDescriptor("android.observe", ToolCatalog.Risk.OBSERVE,
                "xiaohei.tool.input.observe.v1", "xiaohei.tool.output.observation.v1",
                ToolCatalog.RollbackMode.NONE, "", 3000);
        expectDescriptor("android.media_test_collection", ToolCatalog.Risk.REVERSIBLE,
                "xiaohei.tool.input.media_test_collection.v1", "xiaohei.tool.output.media_test_collection.v1",
                ToolCatalog.RollbackMode.REVERSE_TOOL, "android.media_test_collection", 10000);
        expectDescriptor("android.calendar_test_account", ToolCatalog.Risk.REVERSIBLE,
                "xiaohei.tool.input.calendar_test_account.v1", "xiaohei.tool.output.calendar_test_account.v1",
                ToolCatalog.RollbackMode.REVERSE_TOOL, "android.calendar_test_account", 10000);
        boolean immutable = false;
        try { descriptors.clear(); } catch (UnsupportedOperationException expected) { immutable = true; }
        check(immutable, "catalog list mutable");
        for (java.lang.reflect.Field field : ToolCatalog.Descriptor.class.getDeclaredFields())
            check(Modifier.isFinal(field.getModifiers()), "descriptor field not final");
    }

    private static void rejectsThreeDuplicates() {
        ToolCatalog.Descriptor base = valid("android.observe");
        for (List<ToolCatalog.Descriptor> descriptors : Arrays.asList(
                Arrays.asList(base, base),
                Arrays.asList(base, valid("android.observe")),
                Arrays.asList(valid("android.open_gallery"), valid("android.open_gallery"))))
            expect(ToolCatalog.Code.DUPLICATE, descriptors);
    }

    private static void rejectsThreeUnknownVersions() {
        for (int version : new int[] {0, 2, 255}) {
            ToolCatalog.Descriptor value = new ToolCatalog.Descriptor("android.observe", version,
                    ToolCatalog.Risk.OBSERVE, "xiaohei.tool.input.observe.v1",
                    "xiaohei.tool.output.observation.v1", ToolCatalog.RollbackMode.NONE, "",
                    ToolCatalog.Audience.ANDROID_GATEWAY, 3000);
            expect(ToolCatalog.Code.UNKNOWN_VERSION, Collections.singletonList(value));
        }
    }

    private static void rejectsFiveMissingFields() {
        expect(ToolCatalog.Code.INVALID_NAME, Collections.singletonList(descriptor(
                null, ToolCatalog.Risk.OBSERVE, "xiaohei.tool.input.observe.v1",
                "xiaohei.tool.output.observation.v1", ToolCatalog.RollbackMode.NONE,
                "", ToolCatalog.Audience.ANDROID_GATEWAY)));
        expect(ToolCatalog.Code.MISSING_RISK, Collections.singletonList(descriptor(
                "android.observe", null, "xiaohei.tool.input.observe.v1",
                "xiaohei.tool.output.observation.v1", ToolCatalog.RollbackMode.NONE,
                "", ToolCatalog.Audience.ANDROID_GATEWAY)));
        expect(ToolCatalog.Code.MISSING_SCHEMA, Collections.singletonList(descriptor(
                "android.observe", ToolCatalog.Risk.OBSERVE, null,
                "xiaohei.tool.output.observation.v1", ToolCatalog.RollbackMode.NONE,
                "", ToolCatalog.Audience.ANDROID_GATEWAY)));
        expect(ToolCatalog.Code.MISSING_SCHEMA, Collections.singletonList(descriptor(
                "android.observe", ToolCatalog.Risk.OBSERVE, "xiaohei.tool.input.observe.v1",
                null, ToolCatalog.RollbackMode.NONE, "", ToolCatalog.Audience.ANDROID_GATEWAY)));
        expect(ToolCatalog.Code.MISSING_AUDIENCE, Collections.singletonList(descriptor(
                "android.observe", ToolCatalog.Risk.OBSERVE, "xiaohei.tool.input.observe.v1",
                "xiaohei.tool.output.observation.v1", ToolCatalog.RollbackMode.NONE, "", null)));
    }

    private static void rejectsFourRollbackErrors() {
        expect(ToolCatalog.Code.INVALID_ROLLBACK, Collections.singletonList(descriptor(
                "android.observe", ToolCatalog.Risk.OBSERVE, "xiaohei.tool.input.observe.v1",
                "xiaohei.tool.output.observation.v1", null, "", ToolCatalog.Audience.ANDROID_GATEWAY)));
        expect(ToolCatalog.Code.INVALID_ROLLBACK, Collections.singletonList(descriptor(
                "android.observe", ToolCatalog.Risk.OBSERVE, "xiaohei.tool.input.observe.v1",
                "xiaohei.tool.output.observation.v1", ToolCatalog.RollbackMode.NONE,
                "android.observe", ToolCatalog.Audience.ANDROID_GATEWAY)));
        expect(ToolCatalog.Code.INVALID_ROLLBACK, Collections.singletonList(descriptor(
                "android.adjust_volume", ToolCatalog.Risk.REVERSIBLE, "xiaohei.tool.input.volume.v1",
                "xiaohei.tool.output.volume.v1", ToolCatalog.RollbackMode.RESTORE_SNAPSHOT,
                "", ToolCatalog.Audience.ANDROID_GATEWAY)));
        expect(ToolCatalog.Code.INVALID_ROLLBACK, Collections.singletonList(descriptor(
                "android.adjust_volume", ToolCatalog.Risk.REVERSIBLE, "xiaohei.tool.input.volume.v1",
                "xiaohei.tool.output.volume.v1", ToolCatalog.RollbackMode.REVERSE_TOOL,
                "android.missing_reverse", ToolCatalog.Audience.ANDROID_GATEWAY)));
    }

    private static void rejectsUnknownToolsAndRiskMismatch() {
        for (String unknown : new String[] {
                "root.shell", "android.tap", "opencode.run", "android.send", "missing.tool"
        }) check(ToolCatalog.lookup(unknown, 1) == null, "unknown lookup");
        for (ToolCatalog.Risk wrong : new ToolCatalog.Risk[] {
                ToolCatalog.Risk.OBSERVE, ToolCatalog.Risk.REVERSIBLE,
                ToolCatalog.Risk.HIGH, null
        }) check(!ToolCatalog.allowed("android.open_gallery", wrong), "risk mismatch");
        check(ToolCatalog.allowed("android.open_gallery", ToolCatalog.Risk.LOW), "known risk denied");
    }

    private static ToolCatalog.Descriptor valid(String name) {
        return descriptor(name, ToolCatalog.Risk.OBSERVE, "xiaohei.tool.input.observe.v1",
                "xiaohei.tool.output.observation.v1", ToolCatalog.RollbackMode.NONE,
                "", ToolCatalog.Audience.ANDROID_GATEWAY);
    }

    private static ToolCatalog.Descriptor descriptor(String name, ToolCatalog.Risk risk,
            String input, String output, ToolCatalog.RollbackMode rollback, String rollbackTool,
            ToolCatalog.Audience audience) {
        return new ToolCatalog.Descriptor(name, 1, risk, input, output,
                rollback, rollbackTool, audience, 3000);
    }

    private static void expectDescriptor(String name, ToolCatalog.Risk risk, String input,
            String output, ToolCatalog.RollbackMode rollback, String rollbackTool, int timeoutMs) {
        ToolCatalog.Descriptor actual = ToolCatalog.lookup(name, 1);
        check(actual != null && actual.risk == risk && input.equals(actual.inputSchema)
                && output.equals(actual.outputSchema) && actual.rollbackMode == rollback
                && rollbackTool.equals(actual.rollbackTool) && actual.timeoutMs == timeoutMs,
                "descriptor mismatch " + name);
    }

    private static void expect(ToolCatalog.Code expected, List<ToolCatalog.Descriptor> descriptors) {
        ToolCatalog.Code actual = ToolCatalog.validate(new ArrayList<>(descriptors));
        check(actual == expected, "expected=" + expected + " actual=" + actual);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
