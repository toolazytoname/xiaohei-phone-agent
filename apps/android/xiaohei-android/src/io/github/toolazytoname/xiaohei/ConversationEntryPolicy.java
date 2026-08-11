package io.github.toolazytoname.xiaohei;

/** Exact, local-only phrases that may turn a completed wake-command turn into a chat listen turn. */
final class ConversationEntryPolicy {
    private ConversationEntryPolicy() { }

    static boolean startsVoiceConversation(String transcript) {
        if (transcript == null) return false;
        String text = transcript.trim().replaceAll("\\s+", "");
        return "开始聊天".equals(text) || "陪我聊会儿".equals(text)
                || "陪我聊聊天".equals(text) || "进入聊天".equals(text);
    }
}
