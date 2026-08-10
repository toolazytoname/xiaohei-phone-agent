package io.github.toolazytoname.xiaohei;

import java.util.HashMap;
import java.util.Map;

public final class ChannelProfileConfigTest {
    public static void main(String[] args) {
        Map<String, Object> initial = new HashMap<>();
        initial.put(ChannelProfileConfig.AGENT_ENABLED, true);
        initial.put(ChannelProfileConfig.AGENT_ENDPOINT, "https://agent.example/v1");
        initial.put(ChannelProfileConfig.AGENT_MODEL, "agent-model");
        String agentBefore = ChannelProfileConfig.fingerprint(initial, false);
        Map<String, Object> conversation = ChannelProfileConfig.withConversation(initial,
            true, "https://conversation.example/v1", "conversation-model");
        if (!agentBefore.equals(ChannelProfileConfig.fingerprint(conversation, false))) fail("conversation changed agent");
        String conversationBefore = ChannelProfileConfig.fingerprint(conversation, true);
        Map<String, Object> agent = ChannelProfileConfig.withAgent(conversation,
            false, "https://agent-two.example/v1", "agent-two");
        if (!conversationBefore.equals(ChannelProfileConfig.fingerprint(agent, true))) fail("agent changed conversation");
        try {
            ChannelProfileConfig.withConversation(agent, true, "https://ok.example", "bad\nmodel");
            fail("newline accepted");
        } catch (IllegalArgumentException expected) { }
        System.out.println("PASS channel-profiles independent=true secrets=0");
    }

    private static void fail(String message) { throw new AssertionError(message); }
}
