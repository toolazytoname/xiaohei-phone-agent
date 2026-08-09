package io.github.toolazytoname.xiaohei;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/** Explicit, non-secret backup for model-channel preferences. Tokens never enter this format. */
final class ModelChannelBackup {
    private static final String HEADER = "xiaohei-model-channels.v1";

    static String export(int asrMode, boolean agentEnabled, String endpoint, String model) {
        if (asrMode != 0 && asrMode != 1) throw new IllegalArgumentException("invalid ASR mode");
        return HEADER + "\n"
            + "asr_mode=" + asrMode + "\n"
            + "agent_enabled=" + agentEnabled + "\n"
            + "agent_endpoint_b64=" + encode(clean(endpoint, 2048)) + "\n"
            + "agent_model_b64=" + encode(clean(model, 256)) + "\n";
    }

    static Data parse(String document) {
        if (document == null || document.length() > 8192) throw new IllegalArgumentException("backup too large");
        String[] lines = document.replace("\r\n", "\n").split("\n");
        if (lines.length < 5 || !HEADER.equals(lines[0])) throw new IllegalArgumentException("unsupported backup");
        Map<String, String> values = new HashMap<>();
        for (int index = 1; index < lines.length; index++) {
            int equals = lines[index].indexOf('=');
            if (equals <= 0 || values.put(lines[index].substring(0, equals),
                    lines[index].substring(equals + 1)) != null)
                throw new IllegalArgumentException("malformed backup");
        }
        int asrMode;
        try { asrMode = Integer.parseInt(required(values, "asr_mode")); }
        catch (NumberFormatException badNumber) { throw new IllegalArgumentException("invalid ASR mode"); }
        if (asrMode != 0 && asrMode != 1) throw new IllegalArgumentException("invalid ASR mode");
        String enabled = required(values, "agent_enabled");
        if (!"true".equals(enabled) && !"false".equals(enabled)) throw new IllegalArgumentException("invalid Agent state");
        return new Data(asrMode, Boolean.parseBoolean(enabled),
            clean(decode(required(values, "agent_endpoint_b64")), 2048),
            clean(decode(required(values, "agent_model_b64")), 256));
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
        final boolean agentEnabled;
        final String endpoint;
        final String model;
        Data(int asrMode, boolean agentEnabled, String endpoint, String model) {
            this.asrMode = asrMode;
            this.agentEnabled = agentEnabled;
            this.endpoint = endpoint;
            this.model = model;
        }
    }
}
