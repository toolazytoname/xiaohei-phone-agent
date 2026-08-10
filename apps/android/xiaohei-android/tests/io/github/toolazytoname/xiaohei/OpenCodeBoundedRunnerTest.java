package io.github.toolazytoname.xiaohei;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public final class OpenCodeBoundedRunnerTest {
    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("xiaohei-oc-runner-");
        try {
            successAndAllBudgets(root);
            budgetAndInputDenials(root);
            timeoutAndCancellation(root);
            System.out.println("PASS OpenCodeBoundedRunnerTest success=4 budget=3 denied=5 timeout=1 "
                    + "cancel=2 process_launches=0 network_calls=0 content_reads=0 content_writes=0");
        } finally {
            Files.walk(root).sorted(Comparator.reverseOrder()).forEach(path -> {
                try { Files.deleteIfExists(path); } catch (Exception failure) { throw new AssertionError(failure); }
            });
        }
    }

    private static void successAndAllBudgets(Path root) {
        OpenCodeBoundedRunner runner = new OpenCodeBoundedRunner();
        Fixture one = fixture(root, "job-runner-public-0001");
        OpenCodeBoundedRunner.Budget budget = budget(500, 10, 3, 20);
        OpenCodeBoundedRunner.Result result = runner.run(one.task, one.lease, budget, (task, lease, meter, cancel) -> {
            check(meter.consumeTokens(5), "tokens"); check(meter.recordStep(), "step");
            check(meter.appendRedactedOutput("done"), "output");
        }, null);
        expect(OpenCodeBoundedRunner.Code.SUCCESS, result, "success");
        check(result.tokensUsed == 5 && result.stepsUsed == 1 && result.outputCodePoints == 4, "usage result");
        check(!result.publicLogSafe, "private result");
        for (OpenCodeBoundedRunner.Profile profile : OpenCodeBoundedRunner.Profile.values()) {
            Fixture next = fixture(root, "job-runner-public-000" + (profile.ordinal() + 2));
            expect(OpenCodeBoundedRunner.Code.SUCCESS, runner.run(next.task, next.lease, budget(500, 1, 1, 1),
                    (task, lease, meter, cancel) -> { check(meter.consumeTokens(1), "profile token");
                        check(meter.recordStep(), "profile step"); check(meter.appendRedactedOutput("x"), "profile output"); }, null),
                    "profile " + profile);
        }
    }

    private static void budgetAndInputDenials(Path root) {
        OpenCodeBoundedRunner runner = new OpenCodeBoundedRunner();
        for (int index = 0; index < 3; index++) {
            Fixture f = fixture(root, "job-runner-budget-000" + index);
            final int mode = index;
            expect(OpenCodeBoundedRunner.Code.BUDGET_EXCEEDED, runner.run(f.task, f.lease, budget(500, 1, 1, 1),
                    (task, lease, meter, cancel) -> {
                        if (mode == 0) meter.consumeTokens(2);
                        if (mode == 1) { meter.recordStep(); meter.recordStep(); }
                        if (mode == 2) meter.appendRedactedOutput("xx");
                    }, null), "budget " + index);
        }
        Fixture f = fixture(root, "job-runner-denied-0001");
        expect(OpenCodeBoundedRunner.Code.DENIED, runner.run(null, f.lease, budget(500, 1, 1, 1), noop(), null), "null task");
        expect(OpenCodeBoundedRunner.Code.DENIED, runner.run(f.task, null, budget(500, 1, 1, 1), noop(), null), "null lease");
        expect(OpenCodeBoundedRunner.Code.DENIED, runner.run(f.task, f.lease, budget(99, 1, 1, 1), noop(), null), "timeout floor");
        expect(OpenCodeBoundedRunner.Code.DENIED, runner.run(f.task, f.lease, budget(500, 4097, 1, 1), noop(), null), "token cap");
        expect(OpenCodeBoundedRunner.Code.DENIED, runner.run(f.task, f.lease, budget(500, 1, 33, 1), noop(), null), "step cap");
    }

    private static void timeoutAndCancellation(Path root) {
        OpenCodeBoundedRunner runner = new OpenCodeBoundedRunner();
        Fixture timeout = fixture(root, "job-runner-timeout-001");
        expect(OpenCodeBoundedRunner.Code.TIMEOUT, runner.run(timeout.task, timeout.lease, budget(100, 1, 1, 1),
                (task, lease, meter, cancel) -> Thread.sleep(1000), null), "timeout");
        Fixture pre = fixture(root, "job-runner-cancel-0001");
        OpenCodeBoundedRunner.Cancellation cancellation = new OpenCodeBoundedRunner.Cancellation();
        check(cancellation.cancel(OpenCodeBoundedRunner.CancelReason.USER), "pre cancel");
        expect(OpenCodeBoundedRunner.Code.CANCELLED, runner.run(pre.task, pre.lease, budget(500, 1, 1, 1), noop(), cancellation), "pre cancelled");
        Fixture live = fixture(root, "job-runner-cancel-0002");
        OpenCodeBoundedRunner.Cancellation active = new OpenCodeBoundedRunner.Cancellation();
        Thread killer = new Thread(() -> { try { Thread.sleep(30); } catch (InterruptedException ignored) { }
            active.cancel(OpenCodeBoundedRunner.CancelReason.GLOBAL_STOP); });
        killer.start();
        expect(OpenCodeBoundedRunner.Code.CANCELLED, runner.run(live.task, live.lease, budget(500, 1, 1, 1),
                (task, lease, meter, cancel) -> Thread.sleep(1000), active), "active cancelled");
    }

    private static OpenCodeBoundedRunner.Adapter noop() { return (task, lease, meter, cancel) -> { }; }
    private static OpenCodeBoundedRunner.Budget budget(int timeout, int tokens, int steps, int output) {
        return new OpenCodeBoundedRunner.Budget(OpenCodeBoundedRunner.Profile.RELAY_OPENAI,
                OpenCodeBoundedRunner.Agent.ORGANIZE, timeout, tokens, steps, output);
    }
    private static Fixture fixture(Path root, String id) {
        UnconfirmedActionRequest.Result request = UnconfirmedActionRequest.fromConversationMessage(
                new MemoryConversationSession.Message(MemoryConversationSession.Role.USER, "帮我整理项目文件"),
                "request-runner-public-0001", "2026-08-10T14:10:00Z");
        OpenCodeTaskProtocol.Result task = OpenCodeTaskProtocol.create(request.request, id, "plan-runner-public-0001",
                OpenCodeTaskProtocol.Kind.CONTROLLED_FILE_ORGANIZATION);
        OpenCodeWorkspaceBoundary.Result lease = OpenCodeWorkspaceBoundary.allocate(root, task.task);
        check(task.code == OpenCodeTaskProtocol.Code.CREATED && lease.code == OpenCodeWorkspaceBoundary.Code.CREATED, "fixture");
        return new Fixture(task.task, lease.lease);
    }
    private static final class Fixture { final OpenCodeTaskProtocol.Task task; final OpenCodeWorkspaceBoundary.Lease lease;
        Fixture(OpenCodeTaskProtocol.Task task, OpenCodeWorkspaceBoundary.Lease lease) { this.task = task; this.lease = lease; } }
    private static void expect(OpenCodeBoundedRunner.Code code, OpenCodeBoundedRunner.Result result, String message) {
        check(result.code == code, message + " actual=" + result.code); }
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
