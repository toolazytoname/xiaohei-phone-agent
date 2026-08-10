package io.github.toolazytoname.xiaohei;

import java.util.Locale;

/** Exact local parsing and idempotent state for zero-model-call conversation controls. */
final class ConversationControlPolicy {
    enum Action { NONE, STOP, REPEAT, CLEAR, CONTINUE, END }
    enum Mode { ACTIVE, PAUSED, CLEARED }

    static final class Outcome {
        final Action action;
        final boolean changed;
        final boolean cancelRequest;
        final boolean clearContext;
        final boolean repeatLastReply;
        final int modelCalls;

        Outcome(Action action, boolean changed, boolean cancelRequest,
                boolean clearContext, boolean repeatLastReply) {
            this.action = action;
            this.changed = changed;
            this.cancelRequest = cancelRequest;
            this.clearContext = clearContext;
            this.repeatLastReply = repeatLastReply;
            this.modelCalls = 0;
        }
    }

    static final class State {
        private Mode mode = Mode.ACTIVE;
        private boolean requestInFlight;
        private boolean hasLastReply;

        synchronized boolean markRequestStarted() {
            if (mode != Mode.ACTIVE || requestInFlight) return false;
            requestInFlight = true;
            return true;
        }

        synchronized void markRequestFinished(boolean replyStored) {
            requestInFlight = false;
            if (replyStored) hasLastReply = true;
        }

        synchronized Outcome apply(Action action) {
            if (action == null || action == Action.NONE) {
                return new Outcome(Action.NONE, false, false, false, false);
            }
            switch (action) {
                case STOP: {
                    boolean cancel = requestInFlight;
                    boolean changed = mode != Mode.PAUSED || cancel;
                    requestInFlight = false;
                    mode = Mode.PAUSED;
                    return new Outcome(action, changed, cancel, false, false);
                }
                case REPEAT:
                    return new Outcome(action, false, false, false, hasLastReply);
                case CLEAR:
                case END: {
                    boolean cancel = requestInFlight;
                    boolean changed = mode != Mode.CLEARED || requestInFlight || hasLastReply;
                    requestInFlight = false;
                    hasLastReply = false;
                    mode = Mode.CLEARED;
                    return new Outcome(action, changed, cancel, true, false);
                }
                case CONTINUE: {
                    boolean changed = mode != Mode.ACTIVE;
                    mode = Mode.ACTIVE;
                    return new Outcome(action, changed, false, false, false);
                }
                default:
                    return new Outcome(Action.NONE, false, false, false, false);
            }
        }

        synchronized boolean canSend() { return mode == Mode.ACTIVE && !requestInFlight; }
        synchronized boolean canRepeat() { return hasLastReply; }
        synchronized Mode mode() { return mode; }
        synchronized boolean requestInFlight() { return requestInFlight; }
    }

    private ConversationControlPolicy() {}

    static Action parse(String input) {
        if (input == null) return Action.NONE;
        String value = input.trim().toLowerCase(Locale.ROOT)
                .replace("。", "").replace("！", "").replace("？", "")
                .replace("!", "").replace("?", "").replace(".", "");
        if (equalsAny(value, "停止", "停一下", "暂停", "stop", "pause")) return Action.STOP;
        if (equalsAny(value, "重说", "再说一遍", "repeat", "say that again")) return Action.REPEAT;
        if (equalsAny(value, "清空", "清空聊天", "清除上下文", "clear", "clear chat")) return Action.CLEAR;
        if (equalsAny(value, "继续", "继续聊", "continue", "continue chat")) return Action.CONTINUE;
        if (equalsAny(value, "结束聊天", "退出聊天", "不用聊了", "end chat", "goodbye")) return Action.END;
        return Action.NONE;
    }

    private static boolean equalsAny(String value, String... candidates) {
        for (String candidate : candidates) if (candidate.equals(value)) return true;
        return false;
    }
}
