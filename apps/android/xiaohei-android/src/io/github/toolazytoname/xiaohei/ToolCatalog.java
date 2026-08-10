package io.github.toolazytoname.xiaohei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Immutable versioned tool metadata. Catalog lookup never invokes a tool. */
final class ToolCatalog {
    static final int SCHEMA_VERSION = 1;
    static final int TOOL_VERSION = 1;
    static final int MAX_TOOLS = 64;

    enum Risk { OBSERVE, LOW, REVERSIBLE, HIGH }
    enum Audience { ANDROID_GATEWAY, OPENCODE_GATEWAY, ROOT_BROKER }
    enum RollbackMode { NONE, RESTORE_SNAPSHOT, REVERSE_TOOL, MANUAL }
    enum Code {
        OK,
        EMPTY,
        TOO_MANY,
        INVALID_NAME,
        DUPLICATE,
        UNKNOWN_VERSION,
        MISSING_RISK,
        MISSING_SCHEMA,
        INVALID_ROLLBACK,
        MISSING_AUDIENCE,
        INVALID_TIMEOUT
    }

    static final class Descriptor {
        final String name;
        final int version;
        final Risk risk;
        final String inputSchema;
        final String outputSchema;
        final RollbackMode rollbackMode;
        final String rollbackTool;
        final Audience audience;
        final int timeoutMs;

        Descriptor(String name, int version, Risk risk, String inputSchema, String outputSchema,
                RollbackMode rollbackMode, String rollbackTool, Audience audience, int timeoutMs) {
            this.name = name;
            this.version = version;
            this.risk = risk;
            this.inputSchema = inputSchema;
            this.outputSchema = outputSchema;
            this.rollbackMode = rollbackMode;
            this.rollbackTool = rollbackTool == null ? "" : rollbackTool;
            this.audience = audience;
            this.timeoutMs = timeoutMs;
        }
    }

    private static final List<Descriptor> DESCRIPTORS;
    private static final Map<String, Descriptor> BY_KEY;

    static {
        List<Descriptor> descriptors = new ArrayList<>();
        descriptors.add(descriptor("android.open_settings", Risk.LOW,
                "xiaohei.tool.input.empty.v1", "xiaohei.tool.output.activity.v1",
                RollbackMode.NONE, "", 5000));
        descriptors.add(descriptor("android.open_gallery", Risk.LOW,
                "xiaohei.tool.input.empty.v1", "xiaohei.tool.output.activity.v1",
                RollbackMode.NONE, "", 5000));
        descriptors.add(descriptor("android.open_dialer", Risk.LOW,
                "xiaohei.tool.input.empty.v1", "xiaohei.tool.output.activity.v1",
                RollbackMode.NONE, "", 5000));
        descriptors.add(descriptor("android.adjust_volume", Risk.REVERSIBLE,
                "xiaohei.tool.input.volume.v1", "xiaohei.tool.output.volume.v1",
                RollbackMode.RESTORE_SNAPSHOT, "android.adjust_volume", 3000));
        descriptors.add(descriptor("android.observe", Risk.OBSERVE,
                "xiaohei.tool.input.observe.v1", "xiaohei.tool.output.observation.v1",
                RollbackMode.NONE, "", 3000));
        Code code = validate(descriptors);
        if (code != Code.OK) throw new IllegalStateException("invalid built-in tool catalog: " + code);
        DESCRIPTORS = Collections.unmodifiableList(new ArrayList<>(descriptors));
        Map<String, Descriptor> byKey = new HashMap<>();
        for (Descriptor descriptor : descriptors) byKey.put(key(descriptor.name, descriptor.version), descriptor);
        BY_KEY = Collections.unmodifiableMap(byKey);
    }

    private ToolCatalog() {}

    static List<Descriptor> all() {
        return DESCRIPTORS;
    }

    static Descriptor lookup(String name, int version) {
        return BY_KEY.get(key(name, version));
    }

    static Risk risk(String name) {
        Descriptor descriptor = lookup(name, TOOL_VERSION);
        return descriptor == null ? null : descriptor.risk;
    }

    static boolean allowed(String name, Risk requested) {
        Descriptor descriptor = lookup(name, TOOL_VERSION);
        return descriptor != null && descriptor.risk == requested;
    }

    static Code validate(List<Descriptor> descriptors) {
        if (descriptors == null || descriptors.isEmpty()) return Code.EMPTY;
        if (descriptors.size() > MAX_TOOLS) return Code.TOO_MANY;
        Set<String> identities = new HashSet<>();
        for (Descriptor descriptor : descriptors) {
            if (descriptor == null || !validName(descriptor.name)) return Code.INVALID_NAME;
            if (descriptor.version != TOOL_VERSION) return Code.UNKNOWN_VERSION;
            if (!identities.add(key(descriptor.name, descriptor.version))) return Code.DUPLICATE;
            if (descriptor.risk == null) return Code.MISSING_RISK;
            if (!validSchema(descriptor.inputSchema, "input")
                    || !validSchema(descriptor.outputSchema, "output"))
                return Code.MISSING_SCHEMA;
            if (descriptor.audience == null) return Code.MISSING_AUDIENCE;
            if (descriptor.timeoutMs < 100 || descriptor.timeoutMs > 60000)
                return Code.INVALID_TIMEOUT;
        }
        for (Descriptor descriptor : descriptors) {
            if (!validRollback(descriptor, identities)) return Code.INVALID_ROLLBACK;
        }
        return Code.OK;
    }

    private static Descriptor descriptor(String name, Risk risk, String inputSchema,
            String outputSchema, RollbackMode rollbackMode, String rollbackTool, int timeoutMs) {
        return new Descriptor(name, TOOL_VERSION, risk, inputSchema, outputSchema,
                rollbackMode, rollbackTool, Audience.ANDROID_GATEWAY, timeoutMs);
    }

    private static boolean validRollback(Descriptor descriptor, Set<String> identities) {
        if (descriptor.rollbackMode == null) return false;
        if (descriptor.rollbackMode == RollbackMode.NONE
                || descriptor.rollbackMode == RollbackMode.MANUAL)
            return descriptor.rollbackTool.isEmpty();
        return validName(descriptor.rollbackTool)
                && identities.contains(key(descriptor.rollbackTool, descriptor.version));
    }

    private static boolean validName(String value) {
        return value != null && value.matches("[a-z][a-z0-9_.-]{2,63}");
    }

    private static boolean validSchema(String value, String direction) {
        return value != null && value.matches(
                "xiaohei\\.tool\\." + direction + "\\.[a-z0-9_.-]{1,96}\\.v1");
    }

    private static String key(String name, int version) {
        return String.valueOf(name) + "@" + version;
    }
}
