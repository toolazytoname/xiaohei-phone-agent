package io.github.toolazytoname.xiaohei;

public final class ProcessAudioDuplexTest {
    public static void main(String[] args) {
        ProcessAudioDuplex duplex = new ProcessAudioDuplex();
        ProcessAudioDuplex.Lease input = duplex.acquireInput();
        require(input != null); require(duplex.acquireInput() == null); require(duplex.acquireOutput() == null);
        require(duplex.snapshot().owner == AudioDuplexArbiter.Owner.INPUT);
        require(duplex.release(input)); require(!duplex.release(input));

        ProcessAudioDuplex.Lease output = duplex.acquireOutput();
        require(output != null); require(duplex.acquireInput() == null); require(duplex.acquireOutput() == null);
        require(duplex.snapshot().owner == AudioDuplexArbiter.Owner.OUTPUT);
        require(!duplex.release(input)); require(duplex.release(output));
        require(duplex.snapshot().owner == AudioDuplexArbiter.Owner.NONE);
        System.out.println("PASS process-audio-duplex leases=2 conflicts=4 stale_release=2 overlap=0");
    }

    private static void require(boolean value) { if (!value) throw new AssertionError("unexpected ownership result"); }
}
