package io.github.toolazytoname.xiaohei;

public final class ModelChannelBackupTest {
    public static void main(String[] args) {
        String exported = ModelChannelBackup.export(0, true, "https://chat.example/v1", "chat-model",
            true, "https://relay.example/v1", "small-model");
        if (exported.contains("secret-token")) throw new AssertionError("token must never be exportable");
        ModelChannelBackup.Data restored = ModelChannelBackup.parse(exported);
        if (restored.asrMode != 0 || !restored.conversationEnabled
                || !"https://chat.example/v1".equals(restored.conversationEndpoint)
                || !"chat-model".equals(restored.conversationModel) || !restored.agentEnabled
                || !"https://relay.example/v1".equals(restored.endpoint)
                || !"small-model".equals(restored.model)) throw new AssertionError("round trip failed");
        expectInvalid("xiaohei-model-channels.v2\nasr_mode=9\nconversation_enabled=false\nconversation_endpoint_b64=\nconversation_model_b64=\nagent_enabled=false\nagent_endpoint_b64=\nagent_model_b64=\n");
        expectInvalid("wrong-header");
        System.out.println("PASS model-channel-backup roundtrip=1 token_exported=0 invalid=2");
    }
    private static void expectInvalid(String value) {
        try { ModelChannelBackup.parse(value); throw new AssertionError("expected invalid backup"); }
        catch (IllegalArgumentException expected) { }
    }
}
