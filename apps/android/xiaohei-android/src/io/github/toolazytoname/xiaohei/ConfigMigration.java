package io.github.toolazytoname.xiaohei;

import java.util.HashMap;
import java.util.Map;

/** Pure migration core. Migrations are additive and never copy secret plaintext. */
final class ConfigMigration {
    static final int CURRENT_SCHEMA = 3;

    static Map<String, Object> migrate(Map<String, Object> source) {
        Map<String, Object> result = new HashMap<>(source);
        int schema = source.get("config_schema") instanceof Number
            ? ((Number) source.get("config_schema")).intValue() : 0;
        if (schema < 1) {
            copyIfMissing(result, "phone_agent_url", "agent_endpoint");
            copyIfMissing(result, "phone_agent_model", "agent_model");
            result.remove("phone_agent_url");
            result.remove("phone_agent_model");
            schema = 1;
        }
        if (schema < 2) {
            if (!result.containsKey(ChannelProfileConfig.CONVERSATION_ENABLED))
                result.put(ChannelProfileConfig.CONVERSATION_ENABLED, false);
            if (!result.containsKey(ChannelProfileConfig.CONVERSATION_ENDPOINT))
                result.put(ChannelProfileConfig.CONVERSATION_ENDPOINT, "");
            if (!result.containsKey(ChannelProfileConfig.CONVERSATION_MODEL))
                result.put(ChannelProfileConfig.CONVERSATION_MODEL, "");
            result.put("config_schema", 2);
            schema = 2;
        }
        if (schema < 3) {
            if (!result.containsKey(TtsChannelConfig.PROVIDER))
                result.put(TtsChannelConfig.PROVIDER, TtsChannelConfig.Provider.OFF.id);
            if (!result.containsKey(TtsChannelConfig.RELAY_ENDPOINT))
                result.put(TtsChannelConfig.RELAY_ENDPOINT, "");
            if (!result.containsKey(TtsChannelConfig.VOICE))
                result.put(TtsChannelConfig.VOICE, "");
            result.put("config_schema", 3);
        }
        return result;
    }

    private static void copyIfMissing(Map<String, Object> values, String oldKey, String newKey) {
        if (!values.containsKey(newKey) && values.containsKey(oldKey))
            values.put(newKey, values.get(oldKey));
    }
}
