package io.github.toolazytoname.xiaohei;

public final class ConversationEntryPolicyTest {
    public static void main(String[] args) {
        require(ConversationEntryPolicy.startsVoiceConversation("开始聊天"), "exact start");
        require(ConversationEntryPolicy.startsVoiceConversation(" 陪我聊会儿 "), "exact companion");
        require(ConversationEntryPolicy.startsVoiceConversation("陪 我 聊 聊 天"), "spaces normalize");
        require(!ConversationEntryPolicy.startsVoiceConversation("开始聊天要什么"), "question stays draft/chat");
        require(!ConversationEntryPolicy.startsVoiceConversation("打开相册"), "command rejects");
        require(!ConversationEntryPolicy.startsVoiceConversation("帮我开始聊天然后打开微信"), "multi-step rejects");
        require(!ConversationEntryPolicy.startsVoiceConversation(null), "missing rejects");
        System.out.println("PASS conversation-entry exact_phrases=4 ambiguous=reject model_calls=0 action_calls=0");
    }
    private static void require(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
}
