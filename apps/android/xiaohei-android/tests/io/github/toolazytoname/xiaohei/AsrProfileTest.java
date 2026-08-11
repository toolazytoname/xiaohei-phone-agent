package io.github.toolazytoname.xiaohei;

/** Pure contract: a conversation turn cannot inherit command hotwords. */
public final class AsrProfileTest {
    public static void main(String[] args) {
        require(AsrProfile.fromId("command") == AsrProfile.COMMAND, "command resolves");
        require(AsrProfile.fromId("conversation") == AsrProfile.CONVERSATION,
                "conversation resolves");
        require(AsrProfile.fromId("COMMAND") == null, "case change rejects");
        require(AsrProfile.fromId("unknown") == null, "unknown rejects");
        require(AsrProfile.fromId(null) == null, "missing rejects");
        require(AsrProfile.COMMAND.usesCommandHotwords(), "command hotwords enabled");
        require(!AsrProfile.CONVERSATION.usesCommandHotwords(),
                "conversation hotwords disabled");
        require("modified_beam_search".equals(AsrProfile.COMMAND.decodingMethod()),
                "command decoding");
        require("greedy_search".equals(AsrProfile.CONVERSATION.decodingMethod()),
                "conversation decoding");
        require(AsrProfile.COMMAND.hotwordScore() > 0.0f, "command score");
        require(AsrProfile.CONVERSATION.hotwordScore() == 0.0f, "conversation score");
        System.out.println("PASS asr-profile command_hotwords=1 conversation_hotwords=0 unknown=reject");
    }

    private static void require(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
}
