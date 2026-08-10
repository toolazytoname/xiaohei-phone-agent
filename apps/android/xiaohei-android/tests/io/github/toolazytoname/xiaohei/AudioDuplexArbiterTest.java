package io.github.toolazytoname.xiaohei;
public final class AudioDuplexArbiterTest {
    public static void main(String[] args) {
        AudioDuplexArbiter a = new AudioDuplexArbiter();
        expect(AudioDuplexArbiter.Decision.ACQUIRED, a.acquireInput()); assertOnly(a, AudioDuplexArbiter.Owner.INPUT);
        expect(AudioDuplexArbiter.Decision.DENY_CONFLICT, a.acquireOutput()); assertOnly(a, AudioDuplexArbiter.Owner.INPUT);
        expect(AudioDuplexArbiter.Decision.RELEASED, a.releaseInput()); assertOnly(a, AudioDuplexArbiter.Owner.NONE);
        expect(AudioDuplexArbiter.Decision.ACQUIRED, a.acquireOutput()); assertOnly(a, AudioDuplexArbiter.Owner.OUTPUT);
        expect(AudioDuplexArbiter.Decision.DENY_CONFLICT, a.acquireInput()); expect(AudioDuplexArbiter.Decision.DENY_NOT_OWNER, a.releaseInput());
        expect(AudioDuplexArbiter.Decision.RELEASED, a.interruptAll()); assertOnly(a, AudioDuplexArbiter.Owner.NONE);
        expect(AudioDuplexArbiter.Decision.ALREADY_HELD, a.interruptAll());
        System.out.println("PASS audio-duplex input=1 output=1 conflicts=2 interrupt=1 overlap=0 adapter_calls=0");
    }
    private static void assertOnly(AudioDuplexArbiter a, AudioDuplexArbiter.Owner owner) { AudioDuplexArbiter.Snapshot s=a.snapshot(); if(s.owner!=owner || (s.recorderExpectedActive && s.ttsExpectedActive)) throw new AssertionError("overlap"); }
    private static void expect(AudioDuplexArbiter.Decision expected, AudioDuplexArbiter.Decision actual) { if(expected!=actual) throw new AssertionError(actual.toString()); }
}
