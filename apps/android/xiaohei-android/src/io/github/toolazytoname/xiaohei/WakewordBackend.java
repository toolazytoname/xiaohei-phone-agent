package io.github.toolazytoname.xiaohei;

/**
 * The only boundary a hardware backend receives. A DSP implementation must
 * never start an Android action itself; it may only report a validated hit.
 */
interface WakewordBackend {
    String id();
    boolean supports(WakewordProfile profile);
    void arm(WakewordProfile profile, Callback callback) throws Exception;
    void disarm();

    interface Callback {
        void onHit(String keywordId, int confidence, boolean captureAvailable);
        void onError(String safeDetail);
    }
}
