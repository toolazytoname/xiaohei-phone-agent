package io.github.toolazytoname.xiaohei;

import java.util.concurrent.atomic.AtomicInteger;

public final class PendingConversationCallTest {
    public static void main(String[] args) {
        bindThenCancel();
        cancelBeforeBind();
        finishBeforeBind();
        finishAfterCancel();
        normalFinishDoesNotCancel();
        System.out.println("PASS PendingConversationCallTest cases=5 sync_callback=bounded stale_cancel=bounded");
    }

    private static void bindThenCancel() {
        AtomicInteger calls = new AtomicInteger();
        PendingConversationCall pending = new PendingConversationCall(1);
        pending.bind(calls::incrementAndGet);
        check(pending.requestCancel(), "first cancel accepted");
        check(!pending.requestCancel(), "second cancel rejected");
        check(calls.get() == 1, "bound request cancelled once");
    }

    private static void cancelBeforeBind() {
        AtomicInteger calls = new AtomicInteger();
        PendingConversationCall pending = new PendingConversationCall(2);
        check(pending.requestCancel(), "early cancel accepted");
        pending.bind(calls::incrementAndGet);
        check(calls.get() == 1, "late binding cancelled immediately");
    }

    private static void finishBeforeBind() {
        AtomicInteger calls = new AtomicInteger();
        PendingConversationCall pending = new PendingConversationCall(3);
        check(pending.finish(), "sync completion accepted");
        pending.bind(calls::incrementAndGet);
        check(calls.get() == 1, "request returned after sync callback is closed");
        check(!pending.finish(), "duplicate callback rejected");
    }

    private static void finishAfterCancel() {
        PendingConversationCall pending = new PendingConversationCall(4);
        check(pending.requestCancel(), "cancel accepted");
        check(pending.finish(), "cancel result completes generation");
        check(!pending.finish(), "second result rejected");
    }

    private static void normalFinishDoesNotCancel() {
        AtomicInteger calls = new AtomicInteger();
        PendingConversationCall pending = new PendingConversationCall(5);
        pending.bind(calls::incrementAndGet);
        check(pending.generation() == 5, "stable generation");
        check(pending.finish(), "normal finish accepted");
        check(calls.get() == 0, "successful request not cancelled");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
