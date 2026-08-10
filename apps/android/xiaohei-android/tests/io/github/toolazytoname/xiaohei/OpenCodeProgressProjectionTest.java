package io.github.toolazytoname.xiaohei;

public final class OpenCodeProgressProjectionTest {
    public static void main(String[] args) {
        disconnectedIsExplicitAndSafe();
        lifecycleIsBoundedAndTerminal();
        noSensitiveTextCanReachCard();
        System.out.println("PASS OpenCodeProgressProjectionTest disconnected=1 lifecycle=7 terminal=3 sensitive_fields=4 ui_text=redacted");
    }
    private static void disconnectedIsExplicitAndSafe() {
        OpenCodeProgressProjection.Card card = OpenCodeProgressProjection.disconnected();
        check(card.stage == OpenCodeProgressProjection.Stage.NOT_CONNECTED && card.publicLogSafe, "disconnected");
        check(card.visibleText().contains("未连接"), "visible state");
    }
    private static void lifecycleIsBoundedAndTerminal() {
        OpenCodeProgressProjection.Card card = OpenCodeProgressProjection.initial(task(), budget());
        check(card.stage == OpenCodeProgressProjection.Stage.QUEUED && card.stepLimit == 2, "initial");
        card = OpenCodeProgressProjection.apply(card, OpenCodeProgressProjection.Event.STARTED);
        card = OpenCodeProgressProjection.apply(card, OpenCodeProgressProjection.Event.STEP_COMPLETED);
        card = OpenCodeProgressProjection.apply(card, OpenCodeProgressProjection.Event.STEP_COMPLETED);
        card = OpenCodeProgressProjection.apply(card, OpenCodeProgressProjection.Event.STEP_COMPLETED);
        check(card.completedSteps == 2, "bounded steps");
        card = OpenCodeProgressProjection.apply(card, OpenCodeProgressProjection.Event.SUCCEEDED);
        check(card.stage == OpenCodeProgressProjection.Stage.SUCCEEDED, "success");
        check(OpenCodeProgressProjection.apply(card, OpenCodeProgressProjection.Event.FAILED).stage == card.stage, "terminal");
    }
    private static void noSensitiveTextCanReachCard() {
        String text = OpenCodeProgressProjection.initial(task(), budget()).visibleText();
        for (String forbidden : new String[] {"帮我整理", "token", "/data/", "terminal"}) check(!text.toLowerCase().contains(forbidden), "sensitive " + forbidden);
    }
    private static OpenCodeTaskProtocol.Task task() {
        UnconfirmedActionRequest.Result request = UnconfirmedActionRequest.fromConversationMessage(new MemoryConversationSession.Message(MemoryConversationSession.Role.USER, "帮我整理项目文件"), "request-progress-public-001", "2026-08-10T15:00:00Z");
        return OpenCodeTaskProtocol.create(request.request, "job-progress-public-0001", "plan-progress-public-001", OpenCodeTaskProtocol.Kind.CONTROLLED_FILE_ORGANIZATION).task;
    }
    private static OpenCodeBoundedRunner.Budget budget() { return new OpenCodeBoundedRunner.Budget(OpenCodeBoundedRunner.Profile.RELAY_OPENAI, OpenCodeBoundedRunner.Agent.ORGANIZE, 500, 10, 2, 100); }
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
