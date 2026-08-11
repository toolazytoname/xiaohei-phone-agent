package io.github.toolazytoname.xiaohei;

/**
 * Explicit recognition intent. Command tuning must never silently bias open
 * conversation transcription.
 */
enum AsrProfile {
    COMMAND("command", true, "modified_beam_search", 2.0f),
    CONVERSATION("conversation", false, "greedy_search", 0.0f);

    private final String id;
    private final boolean commandHotwords;
    private final String decodingMethod;
    private final float hotwordScore;

    AsrProfile(String id, boolean commandHotwords, String decodingMethod, float hotwordScore) {
        this.id = id;
        this.commandHotwords = commandHotwords;
        this.decodingMethod = decodingMethod;
        this.hotwordScore = hotwordScore;
    }

    String id() { return id; }
    boolean usesCommandHotwords() { return commandHotwords; }
    String decodingMethod() { return decodingMethod; }
    float hotwordScore() { return hotwordScore; }

    static AsrProfile fromId(String value) {
        if (value == null) return null;
        for (AsrProfile profile : values()) {
            if (profile.id.equals(value)) return profile;
        }
        return null;
    }
}
