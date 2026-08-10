package io.github.toolazytoname.xiaohei;

import java.util.List;

public final class MemoryConversationSessionTest {
    public static void main(String[] args) {
        rejectsInvalidBudgets();
        keepsBoundedContextInMemory();
        clearsAtTurnLimit();
        clearsAtInputTokenLimit();
        clearsAtOutputTokenLimit();
        clearsAtTimeoutAndClockRollback();
        cancelClearsTranscript();
        abortRollsBackPendingInput();
        rejectsConcurrentTurn();
        newInstanceRestoresNoTranscript();
        requestViewIsImmutable();
        estimatesChineseConservatively();
        System.out.println("PASS MemoryConversationSessionTest cases=12 turns/time/tokens=bounded transcript_persistence=0");
    }

    private static void rejectsInvalidBudgets() {
        expectIllegal(() -> new MemoryConversationSession(0, 64, 1000, 0));
        expectIllegal(() -> new MemoryConversationSession(9, 64, 1000, 0));
        expectIllegal(() -> new MemoryConversationSession(1, 63, 1000, 0));
        expectIllegal(() -> new MemoryConversationSession(1, 8193, 1000, 0));
        expectIllegal(() -> new MemoryConversationSession(1, 64, 999, 0));
        expectIllegal(() -> new MemoryConversationSession(1, 64, 900001, 0));
        expectIllegal(() -> new MemoryConversationSession(1, 64, 1000, -1));
    }

    private static void keepsBoundedContextInMemory() {
        MemoryConversationSession session = new MemoryConversationSession(3, 128, 5000, 100);
        check(session.beginTurn(" first ", 100) == MemoryConversationSession.Code.ACCEPTED, "first input");
        List<MemoryConversationSession.Message> first = session.requestMessages(101);
        check(first.size() == 1 && first.get(0).role == MemoryConversationSession.Role.USER, "first request");
        check("first".equals(first.get(0).text), "trimmed input");
        check(session.completeTurn("one", 102) == MemoryConversationSession.Code.ACCEPTED, "first reply");
        check(session.beginTurn("second", 103) == MemoryConversationSession.Code.ACCEPTED, "second input");
        List<MemoryConversationSession.Message> second = session.requestMessages(104);
        check(second.size() == 3, "bounded history included");
        check(second.get(1).role == MemoryConversationSession.Role.ASSISTANT, "role order");
        MemoryConversationSession.Status status = session.status(104);
        check(status.completedTurns == 1 && status.awaitingAssistant && !status.closed, "safe metadata");
    }

    private static void clearsAtTurnLimit() {
        MemoryConversationSession session = new MemoryConversationSession(1, 128, 5000, 0);
        check(session.beginTurn("hello", 0) == MemoryConversationSession.Code.ACCEPTED, "turn input");
        check(session.completeTurn("world", 1) == MemoryConversationSession.Code.TURN_LIMIT_CLEARED, "turn cap");
        MemoryConversationSession.Status status = session.status(1);
        check(status.lastCode == MemoryConversationSession.Code.TURN_LIMIT_CLEARED, "turn reason retained");
        assertCleared(status, "turn limit cleared");
        check(session.requestMessages(1).isEmpty(), "turn limit no text");
    }

    private static void clearsAtInputTokenLimit() {
        MemoryConversationSession session = new MemoryConversationSession(2, 64, 5000, 0);
        String tooLarge = repeat("中", 65);
        check(session.beginTurn(tooLarge, 0) == MemoryConversationSession.Code.TOKEN_BUDGET_CLEARED, "input token cap");
        MemoryConversationSession.Status status = session.status(0);
        check(status.lastCode == MemoryConversationSession.Code.TOKEN_BUDGET_CLEARED, "input reason retained");
        assertCleared(status, "input cap cleared");
    }

    private static void clearsAtOutputTokenLimit() {
        MemoryConversationSession session = new MemoryConversationSession(2, 64, 5000, 0);
        check(session.beginTurn(repeat("a", 32), 0) == MemoryConversationSession.Code.ACCEPTED, "input fits");
        check(session.completeTurn(repeat("b", 33), 1) == MemoryConversationSession.Code.TOKEN_BUDGET_CLEARED, "output cap");
        MemoryConversationSession.Status status = session.status(1);
        check(status.lastCode == MemoryConversationSession.Code.TOKEN_BUDGET_CLEARED, "output reason retained");
        assertCleared(status, "output cap cleared");
    }

    private static void clearsAtTimeoutAndClockRollback() {
        MemoryConversationSession timeout = new MemoryConversationSession(2, 128, 1000, 50);
        check(timeout.beginTurn("hello", 50) == MemoryConversationSession.Code.ACCEPTED, "before timeout");
        check(timeout.status(1050).lastCode == MemoryConversationSession.Code.TIMEOUT_CLEARED, "deadline inclusive");
        assertCleared(timeout.status(1050), "timeout cleared");

        MemoryConversationSession rollback = new MemoryConversationSession(2, 128, 1000, 50);
        check(rollback.beginTurn("hello", 49) == MemoryConversationSession.Code.TIMEOUT_CLEARED, "clock rollback fails closed");
        assertCleared(rollback.status(50), "rollback cleared");
    }

    private static void cancelClearsTranscript() {
        MemoryConversationSession session = new MemoryConversationSession(2, 128, 5000, 0);
        session.beginTurn("private text", 0);
        check(session.cancel() == MemoryConversationSession.Code.CANCELLED_CLEARED, "cancel outcome");
        MemoryConversationSession.Status status = session.status(0);
        check(status.lastCode == MemoryConversationSession.Code.CANCELLED_CLEARED, "cancel reason retained");
        assertCleared(status, "cancel cleared");
        check(session.cancel() == MemoryConversationSession.Code.CLOSED, "repeat cancel bounded");
        check(session.status(0).lastCode == MemoryConversationSession.Code.CANCELLED_CLEARED,
                "repeat cancel does not replace terminal reason");
    }

    private static void abortRollsBackPendingInput() {
        MemoryConversationSession session = new MemoryConversationSession(2, 128, 5000, 0);
        session.beginTurn("failed request", 0);
        check(session.abortTurn(1) == MemoryConversationSession.Code.ABORTED, "abort pending");
        MemoryConversationSession.Status status = session.status(1);
        check(status.usedTokens == 0 && !status.awaitingAssistant && !status.closed, "abort rollback");
        check(session.beginTurn("retry", 2) == MemoryConversationSession.Code.ACCEPTED, "retry once changed state");
    }

    private static void rejectsConcurrentTurn() {
        MemoryConversationSession session = new MemoryConversationSession(2, 128, 5000, 0);
        session.beginTurn("one", 0);
        check(session.beginTurn("two", 1) == MemoryConversationSession.Code.BUSY, "one in-flight turn");
        check(session.requestMessages(1).size() == 1, "busy input not appended");
    }

    private static void newInstanceRestoresNoTranscript() {
        MemoryConversationSession oldSession = new MemoryConversationSession(2, 128, 5000, 0);
        oldSession.beginTurn("must not restore", 0);
        MemoryConversationSession afterRestart = new MemoryConversationSession(2, 128, 5000, 0);
        MemoryConversationSession.Status status = afterRestart.status(0);
        check(status.completedTurns == 0 && status.usedTokens == 0, "fresh process state");
        check(afterRestart.requestMessages(0).isEmpty(), "no transcript restoration");
    }

    private static void requestViewIsImmutable() {
        MemoryConversationSession session = new MemoryConversationSession(2, 128, 5000, 0);
        session.beginTurn("hello", 0);
        List<MemoryConversationSession.Message> view = session.requestMessages(0);
        boolean rejected = false;
        try { view.clear(); } catch (UnsupportedOperationException expected) { rejected = true; }
        check(rejected, "request view immutable");
        check(session.requestMessages(0).size() == 1, "internal list intact");
    }

    private static void estimatesChineseConservatively() {
        check(MemoryConversationSession.estimateTokens("四个汉字") >= 4, "CJK estimate");
        check(MemoryConversationSession.estimateTokens("🙂") >= 1, "code point estimate");
    }

    private static void assertCleared(MemoryConversationSession.Status status, String message) {
        check(status.closed && !status.awaitingAssistant && status.usedTokens == 0, message);
    }

    private static String repeat(String value, int count) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < count; i++) out.append(value);
        return out.toString();
    }

    private static void expectIllegal(Runnable runnable) {
        boolean rejected = false;
        try { runnable.run(); } catch (IllegalArgumentException expected) { rejected = true; }
        check(rejected, "invalid budget rejected");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
