package io.github.toolazytoname.xiaohei;

import java.util.HashMap;
import java.util.Map;

public final class ConfigMigrationTest {
    public static void main(String[] args) {
        Map<String, Object> old = new HashMap<>();
        old.put("phone_agent_url", "https://relay.example/v1");
        old.put("phone_agent_model", "example-model");
        old.put("agent_enabled", true);
        Map<String, Object> migrated = ConfigMigration.migrate(old);
        if (!"https://relay.example/v1".equals(migrated.get("agent_endpoint"))) fail();
        if (!"example-model".equals(migrated.get("agent_model"))) fail();
        if (!Integer.valueOf(3).equals(migrated.get("config_schema"))) fail();
        if (!Boolean.FALSE.equals(migrated.get(ChannelProfileConfig.CONVERSATION_ENABLED))) fail();
        if (!TtsChannelConfig.Provider.OFF.id.equals(migrated.get(TtsChannelConfig.PROVIDER))) fail();
        if (!"".equals(migrated.get(TtsChannelConfig.RELAY_ENDPOINT))) fail();
        if (!"".equals(migrated.get(TtsChannelConfig.VOICE))) fail();
        if (migrated.containsKey("phone_agent_url") || migrated.containsKey("phone_agent_model")) fail();
        if (!migrated.equals(ConfigMigration.migrate(migrated)))
            throw new AssertionError("migration must be idempotent");
        System.out.println("PASS config-migration schema=3 idempotent=true secrets_copied=0");
    }
    private static void fail() { throw new AssertionError("legacy config was not migrated"); }
}
