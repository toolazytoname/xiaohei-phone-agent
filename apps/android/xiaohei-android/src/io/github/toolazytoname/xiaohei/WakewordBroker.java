package io.github.toolazytoname.xiaohei;

/**
 * Owns the product state boundary. DSP is deliberately not implemented here:
 * a verified device adapter may call dispatchDspHit only after it has armed a
 * supported hardware profile.
 */
final class WakewordBroker {
    enum State { OFF, ARMING, ARMED, TRIGGERED, ERROR }

    interface Listener {
        void onStateChanged(State state, String detail);
        void onWakewordHit(WakewordEvent event);
    }

    private final Listener listener;
    private State state = State.OFF;

    WakewordBroker(Listener listener) {
        this.listener = listener;
    }

    State state() { return state; }

    void armManualMode() {
        transition(State.ARMING, "基础模式：等待手动测试事件");
        transition(State.ARMED, "基础模式已就绪；未开启常驻麦克风");
    }

    void disarm() {
        transition(State.OFF, "已关闭");
    }

    void dispatchManualHit() {
        if (state != State.ARMED) {
            transition(State.ERROR, "请先启用基础模式");
            return;
        }
        dispatch(new WakewordEvent(WakewordEvent.Source.APP_BUTTON, "manual-test", 100, false));
    }

    void dispatchDspHit(String keywordId, int confidence, boolean captureAvailable) {
        if (state != State.ARMED) return;
        dispatch(new WakewordEvent(WakewordEvent.Source.DSP, keywordId, confidence, captureAvailable));
    }

    private void dispatch(WakewordEvent event) {
        transition(State.TRIGGERED, "已收到 " + event.source + " 唤醒事件");
        listener.onWakewordHit(event);
        transition(State.ARMED, "已重新就绪；未保留命令音频");
    }

    private void transition(State next, String detail) {
        state = next;
        listener.onStateChanged(next, detail);
    }
}
