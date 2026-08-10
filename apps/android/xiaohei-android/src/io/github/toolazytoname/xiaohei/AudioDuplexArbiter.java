package io.github.toolazytoname.xiaohei;

/** Single-process audio ownership rule: recognizer input and TTS output are mutually exclusive. */
final class AudioDuplexArbiter {
    enum Owner { NONE, INPUT, OUTPUT }
    enum Decision { ACQUIRED, ALREADY_HELD, RELEASED, DENY_CONFLICT, DENY_NOT_OWNER }
    static final class Snapshot {
        final Owner owner; final boolean recorderExpectedActive; final boolean ttsExpectedActive;
        Snapshot(Owner owner) { this.owner = owner; recorderExpectedActive = owner == Owner.INPUT; ttsExpectedActive = owner == Owner.OUTPUT; }
    }
    private Owner owner = Owner.NONE;
    synchronized Decision acquireInput() { return acquire(Owner.INPUT); }
    synchronized Decision acquireOutput() { return acquire(Owner.OUTPUT); }
    synchronized Decision releaseInput() { return release(Owner.INPUT); }
    synchronized Decision releaseOutput() { return release(Owner.OUTPUT); }
    synchronized Decision interruptAll() { if (owner == Owner.NONE) return Decision.ALREADY_HELD; owner = Owner.NONE; return Decision.RELEASED; }
    synchronized Snapshot snapshot() { return new Snapshot(owner); }
    private Decision acquire(Owner requested) { if (owner == requested) return Decision.ALREADY_HELD; if (owner != Owner.NONE) return Decision.DENY_CONFLICT; owner = requested; return Decision.ACQUIRED; }
    private Decision release(Owner requested) { if (owner != requested) return Decision.DENY_NOT_OWNER; owner = Owner.NONE; return Decision.RELEASED; }
}
