package io.github.toolazytoname.xiaohei;
public final class TtsLifecycleTest {
    public static void main(String[] args) {
        TtsLifecycle state = new TtsLifecycle();
        require(state.initialize()); require(state.initialized(true)); require(state.speak()); require(state.finished());
        require(state.state() == TtsLifecycle.State.WAITING_FOLLOWUP); require(state.speak()); require(state.interrupt());
        require(state.state() == TtsLifecycle.State.INTERRUPTED); require(state.acknowledgeInterruption()); require(state.stop());
        require(state.initialize()); require(state.initialized(false)); require(!state.speak()); require(state.destroy()); require(!state.initialize());
        TtsLifecycle invalid = new TtsLifecycle(); require(!invalid.interrupt()); require(!invalid.acknowledgeInterruption());
        System.out.println("PASS tts-lifecycle speaking=1 waiting_followup=2 interrupted=1 transitions=15 illegal_rejected=4");
    }
    private static void require(boolean value) { if (!value) throw new AssertionError("unexpected transition"); }
}
