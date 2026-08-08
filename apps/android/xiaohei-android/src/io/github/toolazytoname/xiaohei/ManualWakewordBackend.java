package io.github.toolazytoname.xiaohei;

/** Base-tier backend. It exists for transparent product testing, never DSP emulation. */
final class ManualWakewordBackend implements WakewordBackend {
    private Callback callback;

    @Override public String id() { return "app_button"; }

    @Override public boolean supports(WakewordProfile profile) {
        return profile != null && id().equals(profile.backend);
    }

    @Override public void arm(WakewordProfile profile, Callback callback) {
        this.callback = callback;
    }

    @Override public void disarm() { callback = null; }

    void emitTestHit() {
        if (callback != null) callback.onHit("manual-test", 100, false);
    }
}
