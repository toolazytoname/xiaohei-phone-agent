package io.github.toolazytoname.xiaohei;

import java.util.HashMap;
import java.util.Map;

/** Pure migration core. Migrations are additive and never copy secret plaintext. */
final class ConfigMigration {
    static final int CURRENT_SCHEMA = 1;

    static Map<String, Object> migrate(Map<String, Object> source) {
        Map<String, Object> result = new HashMap<>(source);
        int schema = source.get("config_schema") instanceof Number
            ? ((Number) source.get("config_schema")).intValue() : 0;
        if (schema < 1) {
            copyIfMissing(result, "phone_agent_url", "agent_endpoint");
            copyIfMissing(result, "phone_agent_model", "agent_model");
            result.remove("phone_agent_url");
            result.remove("phone_agent_model");
            result.put("config_schema", 1);
        }
        return result;
    }

    private static void copyIfMissing(Map<String, Object> values, String oldKey, String newKey) {
        if (!values.containsKey(newKey) && values.containsKey(oldKey))
            values.put(newKey, values.get(oldKey));
    }
}
