package io.github.toolazytoname.xiaohei;

import java.util.List;

public final class ConversationSessionCoordinatorTest {
    public static void main(String[] args) {
        carriesReferentialContext();
        exactEndCommandsClearWithoutRequest();
        totalTimeoutClears();
        modelSwitchClearsBeforeRequest();
        lockAndBackgroundClear();
        enforcesHalfDuplex();
        abortAllowsChangedRetry();
        clearsAtConfiguredTurnLimit();
        invalidReplyClears();
        statusContainsNoTranscript();
        rejectsWindowOutsideThreeToEight();
        System.out.println("PASS ConversationSessionCoordinatorTest cases=11 reference/end/timeout/model/lock=bounded half_duplex=true");
    }

    private static void carriesReferentialContext() {
        ConversationSessionCoordinator coordinator = new ConversationSessionCoordinator(4, 512, 5000);
        ConversationSessionCoordinator.BeginResult first = coordinator.begin("我叫小黑", "profile-a", 0);
        check(first.code == ConversationSessionCoordinator.Code.REQUEST_READY, "first ready");
        check(coordinator.complete("你好，小黑", 1) == ConversationSessionCoordinator.Code.REPLY_ACCEPTED, "first complete");
        ConversationSessionCoordinator.BeginResult follow = coordinator.begin("我刚才叫什么？", "profile-a", 2);
        List<MemoryConversationSession.Message> messages = follow.messages;
        check(messages.size() == 3, "referential context has prior pair");
        check("我叫小黑".equals(messages.get(0).text), "prior user retained");
        check("你好，小黑".equals(messages.get(1).text), "prior assistant retained");
        check("我刚才叫什么？".equals(messages.get(2).text), "follow-up appended");
    }

    private static void exactEndCommandsClearWithoutRequest() {
        ConversationSessionCoordinator coordinator = new ConversationSessionCoordinator();
        coordinator.begin("hello", "profile-a", 0);
        ConversationSessionCoordinator.BeginResult end = coordinator.begin("结束聊天。", "profile-a", 1);
        check(end.code == ConversationSessionCoordinator.Code.END_COMMAND_CLEARED, "Chinese end command");
        check(end.messages.isEmpty(), "end sends no request");
        check(!coordinator.status(1).active, "end releases session");
        check(ConversationSessionCoordinator.isEndCommand("Goodbye!"), "English end command");
        check(!ConversationSessionCoordinator.isEndCommand("聊聊结束语"), "substring does not end");
    }

    private static void totalTimeoutClears() {
        ConversationSessionCoordinator coordinator = new ConversationSessionCoordinator(3, 256, 1000);
        coordinator.begin("hello", "profile-a", 10);
        check(coordinator.expire(1009) == ConversationSessionCoordinator.Code.REQUEST_READY, "before deadline");
        check(coordinator.expire(1010) == ConversationSessionCoordinator.Code.TIMEOUT_CLEARED, "at deadline");
        check(!coordinator.status(1010).active, "timeout inactive");
    }

    private static void modelSwitchClearsBeforeRequest() {
        ConversationSessionCoordinator coordinator = new ConversationSessionCoordinator();
        coordinator.begin("private", "profile-a", 0);
        ConversationSessionCoordinator.BeginResult changed = coordinator.begin("new model", "profile-b", 1);
        check(changed.code == ConversationSessionCoordinator.Code.PROFILE_CHANGED_CLEARED, "switch detected");
        check(changed.messages.isEmpty() && !coordinator.status(1).active, "old context released");
        check(coordinator.begin("new model", "profile-b", 2).code == ConversationSessionCoordinator.Code.REQUEST_READY,
                "explicit second send starts fresh profile");
    }

    private static void lockAndBackgroundClear() {
        ConversationSessionCoordinator locked = new ConversationSessionCoordinator();
        locked.begin("private", "profile-a", 0);
        check(locked.onLocked() == ConversationSessionCoordinator.Code.LOCKED_CLEARED, "lock reason");
        check(!locked.status(0).active, "lock cleared");

        ConversationSessionCoordinator backgrounded = new ConversationSessionCoordinator();
        backgrounded.begin("private", "profile-a", 0);
        check(backgrounded.onBackgrounded() == ConversationSessionCoordinator.Code.BACKGROUNDED_CLEARED,
                "background reason");
        check(!backgrounded.status(0).active, "background cleared");
    }

    private static void enforcesHalfDuplex() {
        ConversationSessionCoordinator coordinator = new ConversationSessionCoordinator();
        coordinator.begin("one", "profile-a", 0);
        check(coordinator.begin("two", "profile-a", 1).code == ConversationSessionCoordinator.Code.BUSY,
                "second input denied while thinking");
    }

    private static void abortAllowsChangedRetry() {
        ConversationSessionCoordinator coordinator = new ConversationSessionCoordinator();
        coordinator.begin("failed", "profile-a", 0);
        check(coordinator.abort(1) == ConversationSessionCoordinator.Code.REQUEST_ABORTED, "request rolled back");
        ConversationSessionCoordinator.BeginResult retry = coordinator.begin("changed retry", "profile-a", 2);
        check(retry.code == ConversationSessionCoordinator.Code.REQUEST_READY && retry.messages.size() == 1,
                "retry has no failed phantom turn");
    }

    private static void clearsAtConfiguredTurnLimit() {
        ConversationSessionCoordinator coordinator = new ConversationSessionCoordinator(3, 512, 5000);
        for (int turn = 0; turn < 2; turn++) {
            coordinator.begin("u" + turn, "profile-a", turn * 2L);
            check(coordinator.complete("a" + turn, turn * 2L + 1) == ConversationSessionCoordinator.Code.REPLY_ACCEPTED,
                    "non-final reply");
        }
        coordinator.begin("u2", "profile-a", 4);
        check(coordinator.complete("a2", 5) == ConversationSessionCoordinator.Code.TURN_LIMIT_CLEARED,
                "third turn ends configured window");
        check(!coordinator.status(5).active, "limit releases session");
    }

    private static void invalidReplyClears() {
        ConversationSessionCoordinator coordinator = new ConversationSessionCoordinator();
        coordinator.begin("hello", "profile-a", 0);
        check(coordinator.complete("   ", 1) == ConversationSessionCoordinator.Code.INVALID_REPLY_CLEARED,
                "invalid reply has explicit terminal reason");
        check(!coordinator.status(1).active, "invalid reply releases session");
    }

    private static void statusContainsNoTranscript() {
        ConversationSessionCoordinator coordinator = new ConversationSessionCoordinator();
        coordinator.begin("secret phrase", "profile-a", 0);
        ConversationSessionCoordinator.SafeStatus status = coordinator.status(1);
        check(status.active && status.awaitingReply && status.completedTurns == 0, "safe counters");
        for (java.lang.reflect.Field field : status.getClass().getDeclaredFields()) {
            check(field.getType() != String.class, "status has no String transcript field");
        }
    }

    private static void rejectsWindowOutsideThreeToEight() {
        expectIllegal(() -> new ConversationSessionCoordinator(2, 512, 5000));
        expectIllegal(() -> new ConversationSessionCoordinator(9, 512, 5000));
    }

    private static void expectIllegal(Runnable runnable) {
        boolean rejected = false;
        try { runnable.run(); } catch (IllegalArgumentException expected) { rejected = true; }
        check(rejected, "invalid window rejected");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
