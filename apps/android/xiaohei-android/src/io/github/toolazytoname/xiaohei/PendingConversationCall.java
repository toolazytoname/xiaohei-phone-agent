package io.github.toolazytoname.xiaohei;

/** Race-safe binding between a UI generation and a cancellable request. */
final class PendingConversationCall {
    interface Cancellable { void cancel(); }

    private enum State { OPEN, CANCEL_REQUESTED, FINISHED }

    private final long generation;
    private State state = State.OPEN;
    private Cancellable cancellable;

    PendingConversationCall(long generation) { this.generation = generation; }

    long generation() { return generation; }

    synchronized void bind(Cancellable next) {
        if (next == null) return;
        if (state == State.OPEN) {
            cancellable = next;
        } else {
            next.cancel();
        }
    }

    synchronized boolean requestCancel() {
        if (state != State.OPEN) return false;
        state = State.CANCEL_REQUESTED;
        if (cancellable != null) cancellable.cancel();
        cancellable = null;
        return true;
    }

    synchronized boolean finish() {
        if (state == State.FINISHED) return false;
        state = State.FINISHED;
        cancellable = null;
        return true;
    }
}
