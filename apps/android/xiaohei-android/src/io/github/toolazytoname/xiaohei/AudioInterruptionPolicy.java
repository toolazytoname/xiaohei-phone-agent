package io.github.toolazytoname.xiaohei;

/** Maps non-content interruption signals to one safe release action; it never resumes audio. */
final class AudioInterruptionPolicy {
    enum Source { CALL, ALARM, MEDIA, ACTIVITY }
    enum Decision { STOP_AND_RELEASE, IGNORE }

    static final class Result {
        final Decision decision;
        final boolean stopInput;
        final boolean stopOutput;
        final boolean releaseOwnership;
        final boolean autoResume;
        final String safeReason;

        private Result(Decision decision, String safeReason) {
            this.decision = decision;
            this.stopInput = decision == Decision.STOP_AND_RELEASE;
            this.stopOutput = decision == Decision.STOP_AND_RELEASE;
            this.releaseOwnership = decision == Decision.STOP_AND_RELEASE;
            this.autoResume = false;
            this.safeReason = safeReason;
        }
    }

    private AudioInterruptionPolicy() { }

    static Result decide(Source source, boolean inputActive, boolean outputActive) {
        if (source == null || (!inputActive && !outputActive))
            return new Result(Decision.IGNORE, "没有活跃音频；未启动或恢复任何资源");
        switch (source) {
            case CALL: return stop("来电或通话音频中断；已停止听取和播报");
            case ALARM: return stop("闹钟音频中断；已停止听取和播报");
            case MEDIA: return stop("媒体音频中断；已停止听取和播报");
            case ACTIVITY: return stop("界面离开或系统切换；已停止听取和播报");
            default: return new Result(Decision.IGNORE, "未知中断来源；未改变音频状态");
        }
    }

    private static Result stop(String reason) { return new Result(Decision.STOP_AND_RELEASE, reason); }
}
