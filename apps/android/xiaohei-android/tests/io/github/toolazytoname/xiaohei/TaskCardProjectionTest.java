package io.github.toolazytoname.xiaohei;

public final class TaskCardProjectionTest {
    public static void main(String[] args) {
        TaskCardProjection.Card empty = TaskCardProjection.unavailable();
        check(empty.stage == TaskCardProjection.Stage.UNAVAILABLE, "empty");
        TaskCardProjection.Card card = TaskCardProjection.preview("整理受控项目摘要", 3, 60000);
        check(card.stage == TaskCardProjection.Stage.PREVIEW && card.publicLogSafe, "preview");
        card = TaskCardProjection.apply(card, TaskCardProjection.Stage.RUNNING, 1, TaskCardProjection.Result.NONE);
        check(card.completedSteps == 1, "running");
        String text = card.visibleText().toLowerCase();
        check(!text.contains("/data/"), "path leaked");
        TaskCardProjection.Card taken = TaskCardProjection.apply(card, TaskCardProjection.Stage.TAKEN_OVER, 1, TaskCardProjection.Result.STOPPED);
        check(taken.stage == TaskCardProjection.Stage.TAKEN_OVER, "takeover");
        check(TaskCardProjection.preview("bad\ninput", 1, 1000).stage == TaskCardProjection.Stage.UNAVAILABLE, "input");
        System.out.println("PASS task-card preview=1 running=1 takeover=1 sensitive=0 execution=0");
    }
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
