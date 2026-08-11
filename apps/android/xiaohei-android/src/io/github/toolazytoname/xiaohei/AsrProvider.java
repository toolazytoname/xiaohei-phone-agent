package io.github.toolazytoname.xiaohei;

/** Stable provider identifiers. Selection is explicit; this enum never downloads or switches one. */
enum AsrProvider {
    LOCAL_COMMAND_14M("local_command_14m"),
    LOCAL_CONVERSATION_CANDIDATE("local_conversation_candidate"),
    ANDROID_SYSTEM("android_system");

    private final String id;
    AsrProvider(String id) { this.id = id; }
    String id() { return id; }
    static AsrProvider fromId(String value) {
        if (value == null) return null;
        for (AsrProvider provider : values()) if (provider.id.equals(value)) return provider;
        return null;
    }
}
