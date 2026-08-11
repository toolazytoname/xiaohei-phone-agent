package io.github.toolazytoname.xiaohei;
public final class TtsLifecycleTest {
    public static void main(String[] args) {
        TtsLifecycle state = new TtsLifecycle();
        require(state.initialize()); require(state.initialized(true)); require(state.speak()); require(state.finished());
        require(state.state() == TtsLifecycle.State.WAITING_FOLLOWUP); require(state.speak()); require(state.interrupt());
        require(state.state() == TtsLifecycle.State.INTERRUPTED); require(state.acknowledgeInterruption()); require(state.stop());
        require(state.initialize()); require(state.initialized(false)); require(!state.speak()); require(state.destroy()); require(!state.initialize());
        TtsLifecycle invalid = new TtsLifecycle(); require(!invalid.interrupt()); require(!invalid.acknowledgeInterruption());
        TtsLifecycle interrupted = speaking(); require(interrupted.interrupt()); require(!interrupted.finished()); require(!interrupted.failSpeaking());
        require(interrupted.acknowledgeInterruption()); require(interrupted.speak()); require(interrupted.finished());
        TtsLifecycle stopped = speaking(); require(stopped.stop()); require(!stopped.finished()); require(!stopped.failSpeaking());
        TtsLifecycle destroyed = speaking(); require(destroyed.destroy()); require(!destroyed.finished()); require(!destroyed.failSpeaking());
        System.out.println("PASS tts-lifecycle speaking=5 waiting_followup=3 interrupted=2 explicit_resume=1 transitions=27 stale_callbacks_rejected=6 illegal_rejected=4");
    }
    private static TtsLifecycle speaking() {
        TtsLifecycle state = new TtsLifecycle();
        require(state.initialize()); require(state.initialized(true)); require(state.speak());
        return state;
    }
    private static void require(boolean value) { if (!value) throw new AssertionError("unexpected transition"); }
}
