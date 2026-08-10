package io.github.toolazytoname.xiaohei;
public final class TtsLifecycleTest {
    public static void main(String[] args) {
        TtsLifecycle state = new TtsLifecycle();
        require(state.initialize()); require(state.initialized(true)); require(state.speak()); require(state.finished());
        require(state.speak()); require(state.stop()); require(state.initialize()); require(state.initialized(false));
        require(!state.speak()); require(state.destroy()); require(!state.initialize());
        System.out.println("PASS tts-lifecycle transitions=10 illegal_rejected=2");
    }
    private static void require(boolean value) { if (!value) throw new AssertionError("unexpected transition"); }
}
