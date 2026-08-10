package io.github.toolazytoname.xiaohei;

import java.util.HashMap;
import java.util.Map;

/** Pure key ownership for independently selectable model channels. No secrets live here. */
final class ChannelProfileConfig {
    static final String CONVERSATION_ENABLED = "conversation_enabled";
    static final String CONVERSATION_ENDPOINT = "conversation_endpoint";
    static final String CONVERSATION_MODEL = "conversation_model";
    static final String AGENT_ENABLED = "agent_enabled";
    static final String AGENT_ENDPOINT = "agent_endpoint";
    static final String AGENT_MODEL = "agent_model";

    static Map<String, Object> withConversation(Map<String, Object> source,
            boolean enabled, String endpoint, String model) {
        Map<String, Object> result = new HashMap<>(source);
        result.put(CONVERSATION_ENABLED, enabled);
        result.put(CONVERSATION_ENDPOINT, clean(endpoint, 2048));
        result.put(CONVERSATION_MODEL, clean(model, 256));
        return result;
    }

    static Map<String, Object> withAgent(Map<String, Object> source,
            boolean enabled, String endpoint, String model) {
        Map<String, Object> result = new HashMap<>(source);
        result.put(AGENT_ENABLED, enabled);
        result.put(AGENT_ENDPOINT, clean(endpoint, 2048));
        result.put(AGENT_MODEL, clean(model, 256));
        return result;
    }

    static String fingerprint(Map<String, Object> source, boolean conversation) {
        String prefix = conversation ? "conversation_" : "agent_";
        return value(source, prefix + "enabled") + "\u0000"
            + value(source, prefix + "endpoint") + "\u0000"
            + value(source, prefix + "model");
    }

    private static String value(Map<String, Object> source, String key) {
        Object value = source.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private static String clean(String value, int max) {
        String result = value == null ? "" : value.trim();
        if (result.length() > max || result.indexOf('\n') >= 0 || result.indexOf('\r') >= 0)
            throw new IllegalArgumentException("invalid channel value");
        return result;
    }
}
