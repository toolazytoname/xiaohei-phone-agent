package io.github.toolazytoname.xiaohei;

/** Process-wide leases that connect real Android audio endpoints to the half-duplex rule. */
final class ProcessAudioDuplex {
    static final class Lease {
        private final AudioDuplexArbiter.Owner owner;
        private final long generation;
        private Lease(AudioDuplexArbiter.Owner owner, long generation) {
            this.owner = owner;
            this.generation = generation;
        }
    }

    private static final ProcessAudioDuplex SHARED = new ProcessAudioDuplex();
    private final AudioDuplexArbiter arbiter = new AudioDuplexArbiter();
    private Lease active;
    private long generation;

    static ProcessAudioDuplex shared() { return SHARED; }

    synchronized Lease acquireInput() { return acquire(AudioDuplexArbiter.Owner.INPUT); }
    synchronized Lease acquireOutput() { return acquire(AudioDuplexArbiter.Owner.OUTPUT); }

    synchronized boolean release(Lease lease) {
        if (lease == null || lease != active) return false;
        AudioDuplexArbiter.Decision decision = lease.owner == AudioDuplexArbiter.Owner.INPUT
            ? arbiter.releaseInput() : arbiter.releaseOutput();
        if (decision != AudioDuplexArbiter.Decision.RELEASED) return false;
        active = null;
        return true;
    }

    synchronized AudioDuplexArbiter.Snapshot snapshot() { return arbiter.snapshot(); }

    private Lease acquire(AudioDuplexArbiter.Owner owner) {
        AudioDuplexArbiter.Decision decision = owner == AudioDuplexArbiter.Owner.INPUT
            ? arbiter.acquireInput() : arbiter.acquireOutput();
        if (decision != AudioDuplexArbiter.Decision.ACQUIRED) return null;
        active = new Lease(owner, ++generation);
        return active;
    }
}
