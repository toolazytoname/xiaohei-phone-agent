package io.github.toolazytoname.xiaohei;

public final class ConversationControlPolicyTest {
    public static void main(String[] args) {
        parsesExactButtonAndAsrText();
        substringsRemainConversation();
        stopIsIdempotentAndCancelsOnce();
        repeatIsZeroCallAndStateStable();
        clearIsIdempotentAndForgetsReply();
        continueIsIdempotent();
        endMatchesClearBoundary();
        pausedStateRejectsRequestsUntilContinue();
        everyControlDeclaresZeroModelCalls();
        System.out.println("PASS ConversationControlPolicyTest cases=9 phrases=23 controls=5 model_calls=0 idempotent=true");
    }

    private static void parsesExactButtonAndAsrText() {
        assertParses(ConversationControlPolicy.Action.STOP,
                "停止", "停一下。", "暂停", "stop!", "pause");
        assertParses(ConversationControlPolicy.Action.REPEAT,
                "重说", "再说一遍！", "repeat", "say that again");
        assertParses(ConversationControlPolicy.Action.CLEAR,
                "清空", "清空聊天", "清除上下文", "clear", "clear chat");
        assertParses(ConversationControlPolicy.Action.CONTINUE,
                "继续", "继续聊", "continue", "continue chat");
        assertParses(ConversationControlPolicy.Action.END,
                "结束聊天", "退出聊天", "不用聊了", "end chat", "goodbye");
    }

    private static void substringsRemainConversation() {
        for (String text : new String[] {
                "停止是什么意思", "帮我重说这个故事", "清空数组怎么写", "继续聊这个话题吗",
                "结束聊天功能如何设计", "do not stop", "repeat after me", "clear skies"
        }) check(ConversationControlPolicy.parse(text) == ConversationControlPolicy.Action.NONE,
                "substring remains text: " + text);
    }

    private static void stopIsIdempotentAndCancelsOnce() {
        ConversationControlPolicy.State state = new ConversationControlPolicy.State();
        check(state.markRequestStarted(), "request starts");
        ConversationControlPolicy.Outcome first = state.apply(ConversationControlPolicy.Action.STOP);
        check(first.changed && first.cancelRequest, "first stop cancels");
        ConversationControlPolicy.Outcome second = state.apply(ConversationControlPolicy.Action.STOP);
        check(!second.changed && !second.cancelRequest, "second stop no-op");
        check(state.mode() == ConversationControlPolicy.Mode.PAUSED, "paused");
    }

    private static void repeatIsZeroCallAndStateStable() {
        ConversationControlPolicy.State state = new ConversationControlPolicy.State();
        check(!state.apply(ConversationControlPolicy.Action.REPEAT).repeatLastReply, "nothing to repeat");
        state.markRequestStarted();
        state.markRequestFinished(true);
        ConversationControlPolicy.Outcome first = state.apply(ConversationControlPolicy.Action.REPEAT);
        ConversationControlPolicy.Outcome second = state.apply(ConversationControlPolicy.Action.REPEAT);
        check(first.repeatLastReply && second.repeatLastReply, "repeat remains locally available");
        check(!first.changed && !second.changed && state.mode() == ConversationControlPolicy.Mode.ACTIVE,
                "repeat does not mutate conversation state");
    }

    private static void clearIsIdempotentAndForgetsReply() {
        ConversationControlPolicy.State state = new ConversationControlPolicy.State();
        state.markRequestStarted();
        state.markRequestFinished(true);
        ConversationControlPolicy.Outcome first = state.apply(ConversationControlPolicy.Action.CLEAR);
        ConversationControlPolicy.Outcome second = state.apply(ConversationControlPolicy.Action.CLEAR);
        check(first.changed && first.clearContext, "first clear");
        check(!second.changed && second.clearContext, "second clear safe no-op");
        check(!state.canRepeat() && state.mode() == ConversationControlPolicy.Mode.CLEARED, "reply forgotten");
    }

    private static void continueIsIdempotent() {
        ConversationControlPolicy.State state = new ConversationControlPolicy.State();
        state.apply(ConversationControlPolicy.Action.STOP);
        ConversationControlPolicy.Outcome first = state.apply(ConversationControlPolicy.Action.CONTINUE);
        ConversationControlPolicy.Outcome second = state.apply(ConversationControlPolicy.Action.CONTINUE);
        check(first.changed && !second.changed && state.canSend(), "continue once");
    }

    private static void endMatchesClearBoundary() {
        ConversationControlPolicy.State state = new ConversationControlPolicy.State();
        state.markRequestStarted();
        ConversationControlPolicy.Outcome end = state.apply(ConversationControlPolicy.Action.END);
        check(end.cancelRequest && end.clearContext && state.mode() == ConversationControlPolicy.Mode.CLEARED,
                "end clears and cancels");
        check(!state.apply(ConversationControlPolicy.Action.END).changed, "repeat end no-op");
    }

    private static void pausedStateRejectsRequestsUntilContinue() {
        ConversationControlPolicy.State state = new ConversationControlPolicy.State();
        state.apply(ConversationControlPolicy.Action.STOP);
        check(!state.markRequestStarted() && !state.canSend(), "paused rejects send");
        state.apply(ConversationControlPolicy.Action.CONTINUE);
        check(state.markRequestStarted() && state.requestInFlight(), "continue enables send");
    }

    private static void everyControlDeclaresZeroModelCalls() {
        for (ConversationControlPolicy.Action action : new ConversationControlPolicy.Action[] {
                ConversationControlPolicy.Action.STOP,
                ConversationControlPolicy.Action.REPEAT,
                ConversationControlPolicy.Action.CLEAR,
                ConversationControlPolicy.Action.CONTINUE,
                ConversationControlPolicy.Action.END
        }) {
            ConversationControlPolicy.State state = new ConversationControlPolicy.State();
            state.markRequestStarted();
            check(state.apply(action).modelCalls == 0, "zero model call " + action);
        }
    }

    private static void assertParses(ConversationControlPolicy.Action expected, String... phrases) {
        for (String phrase : phrases) {
            check(ConversationControlPolicy.parse(phrase) == expected, "parse " + phrase);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
