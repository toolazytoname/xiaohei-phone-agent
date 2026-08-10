package io.github.toolazytoname.xiaohei;

import java.util.HashMap;
import java.util.Map;

public final class TtsChannelConfigTest {
    public static void main(String[] args) {
        switchingTtsPreservesConversationAndAgent();
        adapterSelectionHasNoSideEffect();
        relayEndpointIsBounded();
        System.out.println("PASS TtsChannelConfigTest providers=3 conversation_unchanged=true agent_unchanged=true side_effects=0");
    }

    private static void switchingTtsPreservesConversationAndAgent() {
        Map<String, Object> initial = new HashMap<>();
        initial.put(ChannelProfileConfig.CONVERSATION_ENABLED, true);
        initial.put(ChannelProfileConfig.CONVERSATION_ENDPOINT, "https://conversation.example/v1");
        initial.put(ChannelProfileConfig.CONVERSATION_MODEL, "conversation-model");
        initial.put(ChannelProfileConfig.AGENT_ENABLED, true);
        initial.put(ChannelProfileConfig.AGENT_ENDPOINT, "https://agent.example/v1");
        initial.put(ChannelProfileConfig.AGENT_MODEL, "agent-model");
        String conversation = ChannelProfileConfig.fingerprint(initial, true);
        String agent = ChannelProfileConfig.fingerprint(initial, false);

        Map<String, Object> relay = TtsChannelConfig.withTts(initial, TtsChannelConfig.Provider.RELAY,
                "https://speech.example/v1", "voice-zh");
        check(conversation.equals(ChannelProfileConfig.fingerprint(relay, true)), "relay changed Conversation");
        check(agent.equals(ChannelProfileConfig.fingerprint(relay, false)), "relay changed Agent");
        String tts = TtsChannelConfig.fingerprint(relay);

        Map<String, Object> system = TtsChannelConfig.withTts(relay, TtsChannelConfig.Provider.SYSTEM,
                "https://speech.example/v1", "system-zh");
        check(conversation.equals(ChannelProfileConfig.fingerprint(system, true)), "system changed Conversation");
        check(agent.equals(ChannelProfileConfig.fingerprint(system, false)), "system changed Agent");
        check(!tts.equals(TtsChannelConfig.fingerprint(system)), "TTS switch had no TTS effect");
    }

    private static void adapterSelectionHasNoSideEffect() {
        Map<String, Object> values = new HashMap<>();
        values.put(TtsChannelConfig.PROVIDER, "off");
        check(TtsChannelConfig.activeAdapter(values) == TtsChannelConfig.Provider.OFF, "off route");
        values.put(TtsChannelConfig.PROVIDER, "system");
        check(TtsChannelConfig.activeAdapter(values) == TtsChannelConfig.Provider.SYSTEM, "system route");
        values.put(TtsChannelConfig.PROVIDER, "relay");
        check(TtsChannelConfig.activeAdapter(values) == TtsChannelConfig.Provider.RELAY, "relay route");
        check(values.size() == 1, "selection mutated config");
    }

    private static void relayEndpointIsBounded() {
        TtsChannelConfig.withTts(new HashMap<>(), TtsChannelConfig.Provider.RELAY,
                "http://127.0.0.1:9999/v1", "voice");
        expectInvalid("http://speech.example/v1", "voice");
        expectInvalid("https:///missing-host", "voice");
        expectInvalid("", "voice");
        expectInvalid("https://speech.example/v1", "bad\nvoice");
    }

    private static void expectInvalid(String endpoint, String voice) {
        boolean rejected = false;
        try { TtsChannelConfig.withTts(new HashMap<>(), TtsChannelConfig.Provider.RELAY, endpoint, voice); }
        catch (IllegalArgumentException expected) { rejected = true; }
        check(rejected, "invalid relay accepted");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
