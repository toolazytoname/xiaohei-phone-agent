package io.github.toolazytoname.xiaohei;

/**
 * Owns the product state boundary. DSP is deliberately not implemented here:
 * a verified device adapter may call dispatchDspHit only after it has armed a
 * supported hardware profile.
 */
final class WakewordBroker {
    enum State { OFF, ARMING, ARMED, TRIGGERED, LISTENING, THINKING, ACTING, ERROR }

    interface Listener {
        void onStateChanged(State state, String detail);
        void onWakewordHit(WakewordEvent event);
    }

    private final Listener listener;
    private final WakewordProfile profile;
    private final ManualWakewordBackend manualBackend;
    private State state = State.OFF;

    WakewordBroker(Listener listener) {
        this(listener, WakewordProfile.baseManualGallery(), new ManualWakewordBackend());
    }

    WakewordBroker(Listener listener, WakewordProfile profile, ManualWakewordBackend manualBackend) {
        this.listener = listener;
        this.profile = profile;
        this.manualBackend = manualBackend;
    }

    State state() { return state; }

    void armManualMode() {
        transition(State.ARMING, "基础模式：等待手动测试事件");
        manualBackend.arm(profile, new WakewordBackend.Callback() {
            @Override public void onHit(String keywordId, int confidence, boolean captureAvailable) {
                dispatch(new WakewordEvent(WakewordEvent.Source.APP_BUTTON, keywordId, confidence, captureAvailable));
            }
            @Override public void onError(String safeDetail) { transition(State.ERROR, safeDetail); }
        });
        transition(State.ARMED, "基础模式已就绪；未开启常驻麦克风");
    }

    void armDspMode() {
        transition(State.ARMED, "DSP 模式已唤起；准备接收一条短命令");
    }

    void disarm() {
        manualBackend.disarm();
        transition(State.OFF, "已关闭");
    }

    void dispatchManualHit() {
        if (state != State.ARMED) {
            transition(State.ERROR, "请先启用基础模式");
            return;
        }
        manualBackend.emitTestHit();
    }

    void dispatchDspHit(String keywordId, int confidence, boolean captureAvailable) {
        if (state != State.ARMED) return;
        dispatch(new WakewordEvent(WakewordEvent.Source.DSP, keywordId, confidence, captureAvailable));
    }

    void dispatchCpuHit(String keywordId) {
        dispatch(new WakewordEvent(WakewordEvent.Source.CPU_KWS, keywordId, -1, false));
    }

    boolean beginVoiceCommand() {
        if (state != State.TRIGGERED && state != State.ARMED) return false;
        transition(State.LISTENING, "正在听取一条短命令；结束后立即释放麦克风");
        return true;
    }

    void beginThinking() {
        if (state == State.LISTENING) transition(State.THINKING, "正在解析命令");
    }

    void beginAction(String detail) {
        transition(State.ACTING, detail);
    }

    void finishCommand(String detail) {
        if (state != State.OFF) transition(State.ARMED, detail);
    }

    void failCommand(String safeDetail) {
        transition(State.ERROR, safeDetail);
    }

    private void dispatch(WakewordEvent event) {
        transition(State.TRIGGERED, "已收到 " + event.source + " 唤醒事件");
        listener.onWakewordHit(event);
    }

    private void transition(State next, String detail) {
        state = next;
        listener.onStateChanged(next, detail);
    }
}
