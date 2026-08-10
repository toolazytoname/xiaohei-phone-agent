package io.github.toolazytoname.xiaohei;

/** Maps typed runner lifecycle events to a visible, redacted task-card projection. */
final class OpenCodeProgressProjection {
    enum Stage { NOT_CONNECTED, QUEUED, RUNNING, SUCCEEDED, FAILED, CANCELLED }
    enum Event { ACCEPTED, STARTED, STEP_COMPLETED, SUCCEEDED, FAILED, CANCELLED }

    static final class Card {
        final Stage stage;
        final OpenCodeTaskProtocol.Kind kind;
        final int completedSteps;
        final int stepLimit;
        final boolean publicLogSafe;

        private Card(Stage stage, OpenCodeTaskProtocol.Kind kind, int completedSteps, int stepLimit) {
            this.stage = stage;
            this.kind = kind;
            this.completedSteps = completedSteps;
            this.stepLimit = stepLimit;
            this.publicLogSafe = true;
        }

        String visibleText() {
            if (stage == Stage.NOT_CONNECTED) return "OpenCode：未连接；未执行任务";
            String label = kind == OpenCodeTaskProtocol.Kind.PROJECT_SUMMARY ? "项目摘要"
                    : kind == OpenCodeTaskProtocol.Kind.TEST_DIAGNOSIS ? "测试诊断" : "受控文件整理";
            String status = stage == Stage.QUEUED ? "等待确认" : stage == Stage.RUNNING ? "执行中"
                    : stage == Stage.SUCCEEDED ? "已完成" : stage == Stage.FAILED ? "失败"
                    : "已停止";
            return "OpenCode：" + label + " · " + status + " · 步骤 " + completedSteps + "/" + stepLimit
                    + "\n不显示任务正文、路径、计费量或终端输出";
        }
    }

    private OpenCodeProgressProjection() {}

    static Card disconnected() { return new Card(Stage.NOT_CONNECTED, null, 0, 0); }

    static Card initial(OpenCodeTaskProtocol.Task task, OpenCodeBoundedRunner.Budget budget) {
        if (task == null || budget == null || task.kind == null || budget.stepLimit < 1) return disconnected();
        return new Card(Stage.QUEUED, task.kind, 0, budget.stepLimit);
    }

    static Card apply(Card previous, Event event) {
        if (previous == null || event == null || previous.stage == Stage.NOT_CONNECTED) return disconnected();
        if (previous.stage == Stage.SUCCEEDED || previous.stage == Stage.FAILED || previous.stage == Stage.CANCELLED)
            return previous;
        if (event == Event.ACCEPTED) return previous;
        if (event == Event.STARTED) return new Card(Stage.RUNNING, previous.kind, previous.completedSteps, previous.stepLimit);
        if (event == Event.STEP_COMPLETED && previous.stage == Stage.RUNNING)
            return new Card(Stage.RUNNING, previous.kind, Math.min(previous.stepLimit, previous.completedSteps + 1), previous.stepLimit);
        if (event == Event.SUCCEEDED) return new Card(Stage.SUCCEEDED, previous.kind, previous.completedSteps, previous.stepLimit);
        if (event == Event.FAILED) return new Card(Stage.FAILED, previous.kind, previous.completedSteps, previous.stepLimit);
        if (event == Event.CANCELLED) return new Card(Stage.CANCELLED, previous.kind, previous.completedSteps, previous.stepLimit);
        return previous;
    }
}
