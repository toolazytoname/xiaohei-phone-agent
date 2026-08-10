package io.github.toolazytoname.xiaohei;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Pure-Java half-duplex coordinator over the bounded memory session. */
final class ConversationSessionCoordinator {
    static final int DEFAULT_MAX_TURNS = 6;
    static final int DEFAULT_TOKEN_BUDGET = 2048;
    static final long DEFAULT_TIMEOUT_MS = 300000L;

    enum Code {
        REQUEST_READY,
        REPLY_ACCEPTED,
        REQUEST_ABORTED,
        BUSY,
        INVALID_TEXT,
        END_COMMAND_CLEARED,
        PROFILE_CHANGED_CLEARED,
        LOCKED_CLEARED,
        BACKGROUNDED_CLEARED,
        USER_CLEARED,
        INVALID_REPLY_CLEARED,
        TURN_LIMIT_CLEARED,
        TOKEN_BUDGET_CLEARED,
        TIMEOUT_CLEARED,
        CLOSED
    }

    static final class BeginResult {
        final Code code;
        final List<MemoryConversationSession.Message> messages;

        BeginResult(Code code, List<MemoryConversationSession.Message> messages) {
            this.code = code;
            this.messages = messages;
        }
    }

    static final class SafeStatus {
        final int completedTurns;
        final int maxTurns;
        final int usedTokens;
        final int tokenBudget;
        final boolean active;
        final boolean awaitingReply;
        final long remainingMs;
        final Code lastCode;

        SafeStatus(int completedTurns, int maxTurns, int usedTokens, int tokenBudget,
                   boolean active, boolean awaitingReply, long remainingMs, Code lastCode) {
            this.completedTurns = completedTurns;
            this.maxTurns = maxTurns;
            this.usedTokens = usedTokens;
            this.tokenBudget = tokenBudget;
            this.active = active;
            this.awaitingReply = awaitingReply;
            this.remainingMs = remainingMs;
            this.lastCode = lastCode;
        }
    }

    private final int maxTurns;
    private final int tokenBudget;
    private final long timeoutMs;
    private MemoryConversationSession session;
    private String profileFingerprint;
    private long startedAtMs;
    private Code lastCode = Code.CLOSED;

    ConversationSessionCoordinator() {
        this(DEFAULT_MAX_TURNS, DEFAULT_TOKEN_BUDGET, DEFAULT_TIMEOUT_MS);
    }

    ConversationSessionCoordinator(int maxTurns, int tokenBudget, long timeoutMs) {
        // Reuse the versioned boundary validation instead of maintaining a second rule set.
        new MemoryConversationSession(maxTurns, tokenBudget, timeoutMs, 0);
        if (maxTurns < 3 || maxTurns > 8) {
            throw new IllegalArgumentException("follow-up window must be 3-8 turns");
        }
        this.maxTurns = maxTurns;
        this.tokenBudget = tokenBudget;
        this.timeoutMs = timeoutMs;
    }

    synchronized BeginResult begin(String input, String currentProfileFingerprint, long nowMs) {
        if (isEndCommand(input)) {
            clearInternal(Code.END_COMMAND_CLEARED);
            return result(lastCode);
        }
        if (currentProfileFingerprint == null || currentProfileFingerprint.isEmpty()) {
            return result(set(Code.INVALID_TEXT));
        }
        if (session != null && !currentProfileFingerprint.equals(profileFingerprint)) {
            clearInternal(Code.PROFILE_CHANGED_CLEARED);
            return result(lastCode);
        }
        if (session == null) {
            session = new MemoryConversationSession(maxTurns, tokenBudget, timeoutMs, nowMs);
            profileFingerprint = currentProfileFingerprint;
            startedAtMs = nowMs;
        }
        MemoryConversationSession.Code code = session.beginTurn(input, nowMs);
        switch (code) {
            case ACCEPTED:
                lastCode = Code.REQUEST_READY;
                return new BeginResult(lastCode, session.requestMessages(nowMs));
            case BUSY:
                return result(set(Code.BUSY));
            case INVALID_TEXT:
                return result(set(Code.INVALID_TEXT));
            case TURN_LIMIT_CLEARED:
                return terminal(Code.TURN_LIMIT_CLEARED);
            case TOKEN_BUDGET_CLEARED:
                return terminal(Code.TOKEN_BUDGET_CLEARED);
            case TIMEOUT_CLEARED:
                return terminal(Code.TIMEOUT_CLEARED);
            case CLOSED:
            default:
                return terminal(Code.CLOSED);
        }
    }

    synchronized Code complete(String assistantText, long nowMs) {
        if (session == null) return set(Code.CLOSED);
        MemoryConversationSession.Code code = session.completeTurn(assistantText, nowMs);
        switch (code) {
            case ACCEPTED:
                return set(Code.REPLY_ACCEPTED);
            case TURN_LIMIT_CLEARED:
                return terminalCode(Code.TURN_LIMIT_CLEARED);
            case TOKEN_BUDGET_CLEARED:
                return terminalCode(Code.TOKEN_BUDGET_CLEARED);
            case INVALID_TEXT:
                return terminalCode(Code.INVALID_REPLY_CLEARED);
            case TIMEOUT_CLEARED:
                return terminalCode(Code.TIMEOUT_CLEARED);
            default:
                return terminalCode(Code.CLOSED);
        }
    }

    synchronized Code abort(long nowMs) {
        if (session == null) return set(Code.CLOSED);
        MemoryConversationSession.Code code = session.abortTurn(nowMs);
        if (code == MemoryConversationSession.Code.ABORTED) return set(Code.REQUEST_ABORTED);
        if (code == MemoryConversationSession.Code.TIMEOUT_CLEARED)
            return terminalCode(Code.TIMEOUT_CLEARED);
        return set(Code.CLOSED);
    }

    synchronized Code checkProfile(String currentProfileFingerprint) {
        if (session == null) return lastCode;
        if (currentProfileFingerprint == null || !currentProfileFingerprint.equals(profileFingerprint)) {
            clearInternal(Code.PROFILE_CHANGED_CLEARED);
        }
        return lastCode;
    }

    synchronized Code onLocked() {
        clearInternal(Code.LOCKED_CLEARED);
        return lastCode;
    }

    synchronized Code onBackgrounded() {
        clearInternal(Code.BACKGROUNDED_CLEARED);
        return lastCode;
    }

    synchronized Code clearByUser() {
        clearInternal(Code.USER_CLEARED);
        return lastCode;
    }

    synchronized Code expire(long nowMs) {
        if (session == null) return lastCode;
        MemoryConversationSession.Status status = session.status(nowMs);
        if (status.closed) return terminalCode(Code.TIMEOUT_CLEARED);
        return lastCode;
    }

    synchronized SafeStatus status(long nowMs) {
        if (session == null) {
            return new SafeStatus(0, maxTurns, 0, tokenBudget, false, false, 0, lastCode);
        }
        MemoryConversationSession.Status value = session.status(nowMs);
        if (value.closed) {
            terminalCode(Code.TIMEOUT_CLEARED);
            return new SafeStatus(0, maxTurns, 0, tokenBudget, false, false, 0, lastCode);
        }
        long elapsed = nowMs < startedAtMs ? timeoutMs : nowMs - startedAtMs;
        long remaining = Math.max(0, timeoutMs - elapsed);
        return new SafeStatus(
                value.completedTurns, maxTurns, value.usedTokens, tokenBudget,
                true, value.awaitingAssistant, remaining, lastCode
        );
    }

    private BeginResult terminal(Code code) {
        return result(terminalCode(code));
    }

    private Code terminalCode(Code code) {
        session = null;
        profileFingerprint = null;
        startedAtMs = 0;
        return set(code);
    }

    private void clearInternal(Code code) {
        if (session != null) session.cancel();
        terminalCode(code);
    }

    private Code set(Code code) {
        lastCode = code;
        return code;
    }

    private BeginResult result(Code code) {
        return new BeginResult(code, Collections.emptyList());
    }

    static boolean isEndCommand(String input) {
        if (input == null) return false;
        String normalized = input.trim().toLowerCase(Locale.ROOT)
                .replace("。", "").replace("！", "").replace("!", "").replace(".", "");
        return normalized.equals("结束聊天") || normalized.equals("退出聊天")
                || normalized.equals("停止聊天") || normalized.equals("不用聊了")
                || normalized.equals("end chat") || normalized.equals("stop chatting")
                || normalized.equals("goodbye");
    }
}
