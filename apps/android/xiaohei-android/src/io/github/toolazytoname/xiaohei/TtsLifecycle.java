package io.github.toolazytoname.xiaohei;

/** Pure, fail-closed lifecycle for a single half-duplex system TTS utterance. */
final class TtsLifecycle {
    enum State { NEW, INITIALIZING, READY, SPEAKING, STOPPED, FAILED, DESTROYED }
    private State state = State.NEW;
    State state() { return state; }
    boolean initialize() { return transition(State.NEW, State.INITIALIZING) || transition(State.STOPPED, State.INITIALIZING); }
    boolean initialized(boolean success) { return transition(State.INITIALIZING, success ? State.READY : State.FAILED); }
    boolean speak() { return transition(State.READY, State.SPEAKING); }
    boolean finished() { return transition(State.SPEAKING, State.READY); }
    boolean stop() { return transition(State.SPEAKING, State.STOPPED) || transition(State.INITIALIZING, State.STOPPED) || transition(State.READY, State.STOPPED); }
    boolean fail() { return state == State.DESTROYED ? false : set(State.FAILED); }
    boolean destroy() { return state == State.DESTROYED ? false : set(State.DESTROYED); }
    private boolean transition(State from, State to) { return state == from && set(to); }
    private boolean set(State next) { state = next; return true; }
}
