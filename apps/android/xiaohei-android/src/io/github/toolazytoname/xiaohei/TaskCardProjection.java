package io.github.toolazytoname.xiaohei;

/** Public, read-only projection of a reviewed task; never a task payload or execution grant. */
final class TaskCardProjection {
    enum Stage { UNAVAILABLE, PREVIEW, RUNNING, SUCCEEDED, FAILED, TAKEN_OVER }
    enum Result { NONE, VERIFIED, STOPPED, DENIED, FAILED }

    static final class Card {
        final Stage stage; final String target; final int completedSteps; final int stepBudget;
        final int timeoutMs; final Result result; final boolean publicLogSafe;
        private Card(Stage stage, String target, int completedSteps, int stepBudget, int timeoutMs, Result result) {
            this.stage = stage; this.target = target; this.completedSteps = completedSteps;
            this.stepBudget = stepBudget; this.timeoutMs = timeoutMs; this.result = result;
            this.publicLogSafe = true;
        }
        String visibleText() {
            if (stage == Stage.UNAVAILABLE) return "任务卡 / Task card：尚无已审核任务；不会启动或猜测任务";
            String status = stage == Stage.PREVIEW ? "等待用户确认 / awaiting user confirmation"
                : stage == Stage.RUNNING ? "执行中 / running" : stage == Stage.SUCCEEDED ? "已验证 / verified"
                : stage == Stage.FAILED ? "失败 / failed" : "已人工接管 / taken over";
            return "任务卡 / Task card\n目标 / Target: " + target
                + "\n计划 / Plan: 已审核步骤 " + stepBudget + "；当前 " + completedSteps + "/" + stepBudget
                + "\n预算 / Budget: 最多 " + timeoutMs + " ms；步骤 " + stepBudget
                + "\n结果 / Result: " + result + " · " + status
                + "\n人工接管 / Takeover: " + (stage == Stage.TAKEN_OVER ? "已接管；不自动重试" : "真实 runner 未接线；可停止并人工接管")
                + (stage == Stage.FAILED ? "\n" + FailureRecoveryProjection.visibleText(FailureRecoveryProjection.Kind.UNKNOWN) : "")
                + "\n不显示任务正文、路径、Token、模型回复或推理过程";
        }
    }
    private TaskCardProjection() {}
    static Card unavailable() { return new Card(Stage.UNAVAILABLE, "", 0, 0, 0, Result.NONE); }
    static Card preview(String target, int stepBudget, int timeoutMs) {
        if (!valid(target) || stepBudget < 1 || stepBudget > 8 || timeoutMs < 1000 || timeoutMs > 60000) return unavailable();
        return new Card(Stage.PREVIEW, clean(target), 0, stepBudget, timeoutMs, Result.NONE);
    }
    static Card apply(Card prior, Stage stage, int completed, Result result) {
        if (prior == null || prior.stage == Stage.UNAVAILABLE || stage == null || result == null) return unavailable();
        if (prior.stage == Stage.SUCCEEDED || prior.stage == Stage.FAILED || prior.stage == Stage.TAKEN_OVER) return prior;
        if (completed < prior.completedSteps || completed > prior.stepBudget) return prior;
        if (stage == Stage.PREVIEW) return prior;
        return new Card(stage, prior.target, completed, prior.stepBudget, prior.timeoutMs, result);
    }
    private static boolean valid(String value) { return value != null && !value.trim().isEmpty() && value.length() <= 80 && value.indexOf('\n') < 0 && value.indexOf('\r') < 0; }
    private static String clean(String value) { return value.replaceAll("[\\p{Cntrl}]", " ").trim(); }
}
