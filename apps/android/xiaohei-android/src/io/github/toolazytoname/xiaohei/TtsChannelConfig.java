package io.github.toolazytoname.xiaohei;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/** Pure ownership and validation for the independent Conversation TTS channel. */
final class TtsChannelConfig {
    static final String PROVIDER = "tts_provider";
    static final String RELAY_ENDPOINT = "tts_relay_endpoint";
    static final String VOICE = "tts_voice";

    enum Provider {
        OFF("off"), SYSTEM("system"), RELAY("relay");
        final String id;
        Provider(String id) { this.id = id; }

        static Provider fromId(String id) {
            for (Provider value : values()) if (value.id.equals(id)) return value;
            return OFF;
        }
    }

    static Map<String, Object> withTts(Map<String, Object> source, Provider provider,
                                       String relayEndpoint, String voice) {
        if (provider == null) throw new IllegalArgumentException("missing TTS provider");
        String endpoint = clean(relayEndpoint, 2048);
        String voiceId = clean(voice, 256);
        if (provider == Provider.RELAY && !validEndpoint(endpoint)) {
            throw new IllegalArgumentException("relay TTS requires HTTPS or loopback HTTP");
        }
        Map<String, Object> result = new HashMap<>(source);
        result.put(PROVIDER, provider.id);
        result.put(RELAY_ENDPOINT, endpoint);
        result.put(VOICE, voiceId);
        return result;
    }

    static String fingerprint(Map<String, Object> source) {
        return value(source, PROVIDER) + "\u0000"
                + value(source, RELAY_ENDPOINT) + "\u0000"
                + value(source, VOICE);
    }

    static Provider activeAdapter(Map<String, Object> source) {
        return Provider.fromId(value(source, PROVIDER));
    }

    static boolean validEndpoint(String value) {
        try {
            URI uri = URI.create(value);
            String host = uri.getHost();
            return ("https".equalsIgnoreCase(uri.getScheme()) && host != null && !host.isEmpty())
                    || ("http".equalsIgnoreCase(uri.getScheme())
                    && ("127.0.0.1".equals(host) || "localhost".equalsIgnoreCase(host)
                    || "::1".equals(host)));
        } catch (RuntimeException invalid) {
            return false;
        }
    }

    private static String value(Map<String, Object> source, String key) {
        Object value = source.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private static String clean(String value, int max) {
        String result = value == null ? "" : value.trim();
        if (result.length() > max || result.indexOf('\n') >= 0 || result.indexOf('\r') >= 0)
            throw new IllegalArgumentException("invalid TTS channel value");
        return result;
    }
}
