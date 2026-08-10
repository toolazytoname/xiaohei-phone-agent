package io.github.toolazytoname.xiaohei;

/** Pure, fail-closed lifecycle for a single half-duplex system TTS utterance. */
final class TtsLifecycle {
    enum State { NEW, INITIALIZING, READY, SPEAKING, WAITING_FOLLOWUP, INTERRUPTED, STOPPED, FAILED, DESTROYED }
    private State state = State.NEW;
    State state() { return state; }
    boolean initialize() { return transition(State.NEW, State.INITIALIZING) || transition(State.STOPPED, State.INITIALIZING); }
    boolean initialized(boolean success) { return transition(State.INITIALIZING, success ? State.READY : State.FAILED); }
    boolean speak() { return transition(State.READY, State.SPEAKING) || transition(State.WAITING_FOLLOWUP, State.SPEAKING); }
    /** Completion releases output ownership; a future user turn must explicitly start separately. */
    boolean finished() { return transition(State.SPEAKING, State.WAITING_FOLLOWUP); }
    /** A user/system interruption releases the output path without treating it as successful speech. */
    boolean interrupt() { return transition(State.SPEAKING, State.INTERRUPTED); }
    boolean acknowledgeInterruption() { return transition(State.INTERRUPTED, State.WAITING_FOLLOWUP); }
    boolean stop() { return transition(State.SPEAKING, State.STOPPED) || transition(State.WAITING_FOLLOWUP, State.STOPPED)
        || transition(State.INTERRUPTED, State.STOPPED) || transition(State.INITIALIZING, State.STOPPED) || transition(State.READY, State.STOPPED); }
    boolean fail() { return state == State.DESTROYED ? false : set(State.FAILED); }
    boolean destroy() { return state == State.DESTROYED ? false : set(State.DESTROYED); }
    private boolean transition(State from, State to) { return state == from && set(to); }
    private boolean set(State next) { state = next; return true; }
}
