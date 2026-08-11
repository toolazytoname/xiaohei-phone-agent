package io.github.toolazytoname.xiaohei;

/**
 * Pure state machine for one deliberately initiated Conversation voice turn.
 * It owns neither audio nor network resources: callers must release those on
 * every non-active transition.  In particular, it has no command/action path.
 */
final class ConversationVoiceTurnCoordinator {
    enum State { IDLE, LISTENING, REVIEWING, THINKING, SPEAKING, WAITING_FOLLOWUP, STOPPED, FAILED }
    private State state = State.IDLE;

    State state() { return state; }
    boolean beginListening() { return transition(State.IDLE, State.LISTENING)
            || transition(State.WAITING_FOLLOWUP, State.LISTENING); }
    boolean partial() { return state == State.LISTENING; }
    boolean finalTranscript() { return transition(State.LISTENING, State.REVIEWING); }
    boolean beginThinking() { return transition(State.REVIEWING, State.THINKING); }
    boolean beginSpeaking() { return transition(State.THINKING, State.SPEAKING); }
    boolean speechFinished() { return transition(State.SPEAKING, State.WAITING_FOLLOWUP); }
    boolean stop() { return setTerminal(State.STOPPED); }
    boolean fail() { return setTerminal(State.FAILED); }
    boolean reset() { state = State.IDLE; return true; }

    private boolean setTerminal(State next) {
        if (state == State.STOPPED || state == State.FAILED) return false;
        state = next;
        return true;
    }
    private boolean transition(State from, State to) {
        if (state != from) return false;
        state = to;
        return true;
    }
}
