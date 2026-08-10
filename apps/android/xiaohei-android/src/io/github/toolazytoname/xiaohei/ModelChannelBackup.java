package io.github.toolazytoname.xiaohei;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/** Explicit, non-secret backup for model-channel preferences. Tokens never enter this format. */
final class ModelChannelBackup {
    private static final String HEADER_V2 = "xiaohei-model-channels.v2";
    private static final String HEADER_V3 = "xiaohei-model-channels.v3";

    static String export(int asrMode, boolean conversationEnabled, String conversationEndpoint,
            String conversationModel, boolean agentEnabled, String endpoint, String model,
            TtsChannelConfig.Provider ttsProvider, String ttsRelayEndpoint, String ttsVoice) {
        if (asrMode != 0 && asrMode != 1) throw new IllegalArgumentException("invalid ASR mode");
        if (ttsProvider == null) throw new IllegalArgumentException("missing TTS provider");
        TtsChannelConfig.withTts(new HashMap<String, Object>(), ttsProvider, ttsRelayEndpoint, ttsVoice);
        return HEADER_V3 + "\n"
            + "asr_mode=" + asrMode + "\n"
            + "conversation_enabled=" + conversationEnabled + "\n"
            + "conversation_endpoint_b64=" + encode(clean(conversationEndpoint, 2048)) + "\n"
            + "conversation_model_b64=" + encode(clean(conversationModel, 256)) + "\n"
            + "agent_enabled=" + agentEnabled + "\n"
            + "agent_endpoint_b64=" + encode(clean(endpoint, 2048)) + "\n"
            + "agent_model_b64=" + encode(clean(model, 256)) + "\n"
            + "tts_provider=" + ttsProvider.id + "\n"
            + "tts_relay_endpoint_b64=" + encode(clean(ttsRelayEndpoint, 2048)) + "\n"
            + "tts_voice_b64=" + encode(clean(ttsVoice, 256)) + "\n";
    }

    static Data parse(String document) {
        if (document == null || document.length() > 8192) throw new IllegalArgumentException("backup too large");
        String[] lines = document.replace("\r\n", "\n").split("\n");
        if (lines.length < 8 || (!HEADER_V2.equals(lines[0]) && !HEADER_V3.equals(lines[0])))
            throw new IllegalArgumentException("unsupported backup");
        boolean version3 = HEADER_V3.equals(lines[0]);
        Map<String, String> values = new HashMap<>();
        for (int index = 1; index < lines.length; index++) {
            int equals = lines[index].indexOf('=');
            if (equals <= 0 || values.put(lines[index].substring(0, equals),
                    lines[index].substring(equals + 1)) != null)
                throw new IllegalArgumentException("malformed backup");
        }
        int expectedFields = version3 ? 10 : 7;
        if (values.size() != expectedFields) throw new IllegalArgumentException("unknown or missing field");
        int asrMode;
        try { asrMode = Integer.parseInt(required(values, "asr_mode")); }
        catch (NumberFormatException badNumber) { throw new IllegalArgumentException("invalid ASR mode"); }
        if (asrMode != 0 && asrMode != 1) throw new IllegalArgumentException("invalid ASR mode");
        String conversationEnabled = required(values, "conversation_enabled");
        String enabled = required(values, "agent_enabled");
        if (!"true".equals(conversationEnabled) && !"false".equals(conversationEnabled)) throw new IllegalArgumentException("invalid Conversation state");
        if (!"true".equals(enabled) && !"false".equals(enabled)) throw new IllegalArgumentException("invalid Agent state");
        TtsChannelConfig.Provider ttsProvider = TtsChannelConfig.Provider.OFF;
        String ttsRelayEndpoint = "";
        String ttsVoice = "";
        if (version3) {
            String provider = required(values, "tts_provider");
            ttsProvider = TtsChannelConfig.Provider.fromId(provider);
            if (!ttsProvider.id.equals(provider)) throw new IllegalArgumentException("invalid TTS provider");
            ttsRelayEndpoint = clean(decode(required(values, "tts_relay_endpoint_b64")), 2048);
            ttsVoice = clean(decode(required(values, "tts_voice_b64")), 256);
            TtsChannelConfig.withTts(new HashMap<String, Object>(), ttsProvider, ttsRelayEndpoint, ttsVoice);
        }
        return new Data(asrMode, Boolean.parseBoolean(conversationEnabled),
            clean(decode(required(values, "conversation_endpoint_b64")), 2048),
            clean(decode(required(values, "conversation_model_b64")), 256), Boolean.parseBoolean(enabled),
            clean(decode(required(values, "agent_endpoint_b64")), 2048),
            clean(decode(required(values, "agent_model_b64")), 256),
            ttsProvider, ttsRelayEndpoint, ttsVoice);
    }

    private static String required(Map<String, String> values, String key) {
        String value = values.get(key);
        if (value == null) throw new IllegalArgumentException("missing " + key);
        return value;
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        try { return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8); }
        catch (IllegalArgumentException badBase64) { throw new IllegalArgumentException("invalid encoded value"); }
    }

    private static String clean(String value, int max) {
        String result = value == null ? "" : value.trim();
        if (result.length() > max || result.indexOf('\n') >= 0 || result.indexOf('\r') >= 0)
            throw new IllegalArgumentException("invalid backup value");
        return result;
    }

    static final class Data {
        final int asrMode;
        final boolean conversationEnabled;
        final String conversationEndpoint;
        final String conversationModel;
        final boolean agentEnabled;
        final String endpoint;
        final String model;
        final TtsChannelConfig.Provider ttsProvider;
        final String ttsRelayEndpoint;
        final String ttsVoice;
        Data(int asrMode, boolean conversationEnabled, String conversationEndpoint,
                String conversationModel, boolean agentEnabled, String endpoint, String model,
                TtsChannelConfig.Provider ttsProvider, String ttsRelayEndpoint, String ttsVoice) {
            this.asrMode = asrMode;
            this.conversationEnabled = conversationEnabled;
            this.conversationEndpoint = conversationEndpoint;
            this.conversationModel = conversationModel;
            this.agentEnabled = agentEnabled;
            this.endpoint = endpoint;
            this.model = model;
            this.ttsProvider = ttsProvider;
            this.ttsRelayEndpoint = ttsRelayEndpoint;
            this.ttsVoice = ttsVoice;
        }
    }
}
