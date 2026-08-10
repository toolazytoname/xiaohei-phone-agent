package io.github.toolazytoname.xiaohei;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bounded, transcript-only-in-memory conversation context.
 *
 * <p>The caller supplies monotonic milliseconds. Limit, timeout, cancellation, and privacy
 * failures close the session and erase all message text. This class has no persistence API.
 */
final class MemoryConversationSession {
    static final int MIN_TURNS = 1;
    static final int MAX_TURNS = 8;
    static final int MIN_TOKEN_BUDGET = 64;
    static final int MAX_TOKEN_BUDGET = 8192;
    static final long MIN_TIMEOUT_MS = 1000L;
    static final long MAX_TIMEOUT_MS = 900000L;

    enum Role { USER, ASSISTANT }

    enum Code {
        ACCEPTED,
        BUSY,
        CLOSED,
        INVALID_STATE,
        INVALID_TEXT,
        TURN_LIMIT_CLEARED,
        TOKEN_BUDGET_CLEARED,
        TIMEOUT_CLEARED,
        CANCELLED_CLEARED,
        ABORTED
    }

    static final class Message {
        final Role role;
        final String text;

        Message(Role role, String text) {
            this.role = role;
            this.text = text;
        }
    }

    /** Public-log-safe metadata. It intentionally has no transcript field. */
    static final class Status {
        final int completedTurns;
        final int maxTurns;
        final int usedTokens;
        final int tokenBudget;
        final long timeoutMs;
        final boolean awaitingAssistant;
        final boolean closed;
        final Code lastCode;

        Status(int completedTurns, int maxTurns, int usedTokens, int tokenBudget,
               long timeoutMs, boolean awaitingAssistant, boolean closed, Code lastCode) {
            this.completedTurns = completedTurns;
            this.maxTurns = maxTurns;
            this.usedTokens = usedTokens;
            this.tokenBudget = tokenBudget;
            this.timeoutMs = timeoutMs;
            this.awaitingAssistant = awaitingAssistant;
            this.closed = closed;
            this.lastCode = lastCode;
        }
    }

    private final int maxTurns;
    private final int tokenBudget;
    private final long timeoutMs;
    private final long startedAtMs;
    private final List<Message> messages = new ArrayList<>();
    private int completedTurns;
    private int usedTokens;
    private boolean awaitingAssistant;
    private boolean closed;
    private Code lastCode = Code.ACCEPTED;

    MemoryConversationSession(int maxTurns, int tokenBudget, long timeoutMs, long startedAtMs) {
        if (maxTurns < MIN_TURNS || maxTurns > MAX_TURNS) {
            throw new IllegalArgumentException("maxTurns outside conversation-session.v1");
        }
        if (tokenBudget < MIN_TOKEN_BUDGET || tokenBudget > MAX_TOKEN_BUDGET) {
            throw new IllegalArgumentException("tokenBudget outside conversation-session.v1");
        }
        if (timeoutMs < MIN_TIMEOUT_MS || timeoutMs > MAX_TIMEOUT_MS) {
            throw new IllegalArgumentException("timeoutMs outside conversation-session.v1");
        }
        if (startedAtMs < 0) throw new IllegalArgumentException("startedAtMs must be monotonic");
        this.maxTurns = maxTurns;
        this.tokenBudget = tokenBudget;
        this.timeoutMs = timeoutMs;
        this.startedAtMs = startedAtMs;
    }

    synchronized Code beginTurn(String userText, long nowMs) {
        Code terminal = checkUsable(nowMs);
        if (terminal != null) return terminal;
        if (awaitingAssistant) return set(Code.BUSY);
        if (completedTurns >= maxTurns) return clearAndClose(Code.TURN_LIMIT_CLEARED);
        String normalized = normalize(userText);
        if (normalized == null) return set(Code.INVALID_TEXT);
        int nextTokens = estimateTokens(normalized);
        if (wouldExceedBudget(nextTokens)) return clearAndClose(Code.TOKEN_BUDGET_CLEARED);
        messages.add(new Message(Role.USER, normalized));
        usedTokens += nextTokens;
        awaitingAssistant = true;
        return set(Code.ACCEPTED);
    }

    synchronized Code completeTurn(String assistantText, long nowMs) {
        Code terminal = checkUsable(nowMs);
        if (terminal != null) return terminal;
        if (!awaitingAssistant) return set(Code.INVALID_STATE);
        String normalized = normalize(assistantText);
        if (normalized == null) return clearAndClose(Code.INVALID_TEXT);
        int nextTokens = estimateTokens(normalized);
        if (wouldExceedBudget(nextTokens)) return clearAndClose(Code.TOKEN_BUDGET_CLEARED);
        messages.add(new Message(Role.ASSISTANT, normalized));
        usedTokens += nextTokens;
        awaitingAssistant = false;
        completedTurns++;
        if (completedTurns >= maxTurns) return clearAndClose(Code.TURN_LIMIT_CLEARED);
        return set(Code.ACCEPTED);
    }

    synchronized Code abortTurn(long nowMs) {
        Code terminal = checkUsable(nowMs);
        if (terminal != null) return terminal;
        if (!awaitingAssistant || messages.isEmpty()) return set(Code.INVALID_STATE);
        Message pending = messages.remove(messages.size() - 1);
        usedTokens -= estimateTokens(pending.text);
        awaitingAssistant = false;
        return set(Code.ABORTED);
    }

    synchronized Code cancel() {
        if (closed) return Code.CLOSED;
        return clearAndClose(Code.CANCELLED_CLEARED);
    }

    synchronized List<Message> requestMessages(long nowMs) {
        Code terminal = checkUsable(nowMs);
        if (terminal != null || closed || !awaitingAssistant) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<>(messages));
    }

    synchronized Status status(long nowMs) {
        checkUsable(nowMs);
        return new Status(
                completedTurns, maxTurns, usedTokens, tokenBudget, timeoutMs,
                awaitingAssistant, closed, lastCode
        );
    }

    private Code checkUsable(long nowMs) {
        if (closed) return Code.CLOSED;
        if (nowMs < startedAtMs || nowMs - startedAtMs >= timeoutMs) {
            return clearAndClose(Code.TIMEOUT_CLEARED);
        }
        return null;
    }

    private boolean wouldExceedBudget(int additionalTokens) {
        return additionalTokens > tokenBudget - usedTokens;
    }

    private Code clearAndClose(Code code) {
        messages.clear();
        usedTokens = 0;
        awaitingAssistant = false;
        closed = true;
        return set(code);
    }

    private Code set(Code code) {
        lastCode = code;
        return code;
    }

    private static String normalize(String text) {
        if (text == null) return null;
        String normalized = text.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    /** Conservative provider-independent estimate: never below code points or UTF-8 bytes / 4. */
    static int estimateTokens(String text) {
        int codePoints = text.codePointCount(0, text.length());
        int utf8Quarter = (text.getBytes(StandardCharsets.UTF_8).length + 3) / 4;
        return Math.max(1, Math.max(codePoints, utf8Quarter));
    }
}
