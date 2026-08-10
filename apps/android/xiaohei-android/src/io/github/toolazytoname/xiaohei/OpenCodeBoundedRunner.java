package io.github.toolazytoname.xiaohei;

import java.util.Collections;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Runs one task through an injected, reviewed OpenCode adapter under fixed budgets.
 * This class has no command line, process launcher, network client, file-content API, UI, or root path.
 */
final class OpenCodeBoundedRunner {
    static final int SCHEMA_VERSION = 1;
    static final int MIN_TIMEOUT_MS = 100;
    static final int MAX_TIMEOUT_MS = 60000;
    static final int MAX_TOKEN_BUDGET = 4096;
    static final int MAX_STEP_BUDGET = 32;
    static final int MAX_OUTPUT_CODE_POINTS = 4096;

    enum Code { SUCCESS, DENIED, CANCELLED, TIMEOUT, BUDGET_EXCEEDED, ADAPTER_FAILURE, INVALID_OUTPUT }
    enum CancelReason { USER, GLOBAL_STOP, CLIENT_DISCONNECTED }
    enum Profile { RELAY_OPENAI, RELAY_ANTHROPIC, LOCAL_SMALL }
    enum Agent { ANALYZE, DIAGNOSE, ORGANIZE }

    interface Adapter {
        void run(OpenCodeTaskProtocol.Task task, OpenCodeWorkspaceBoundary.Lease lease,
                Budget budget, Cancellation cancellation) throws Exception;
    }

    static final class Budget {
        final Profile profile;
        final Agent agent;
        final int timeoutMs;
        final int tokenLimit;
        final int stepLimit;
        final int outputLimit;
        private int tokens;
        private int steps;
        private final StringBuilder output = new StringBuilder();
        private boolean exceeded;

        Budget(Profile profile, Agent agent, int timeoutMs, int tokenLimit, int stepLimit,
                int outputLimit) {
            this.profile = profile;
            this.agent = agent;
            this.timeoutMs = timeoutMs;
            this.tokenLimit = tokenLimit;
            this.stepLimit = stepLimit;
            this.outputLimit = outputLimit;
        }

        synchronized boolean consumeTokens(int count) {
            if (count < 0 || tokens + count > tokenLimit) return exceeded = true;
            tokens += count;
            return true;
        }

        synchronized boolean recordStep() {
            if (steps >= stepLimit) return exceeded = true;
            steps++;
            return true;
        }

        synchronized boolean appendRedactedOutput(String value) {
            if (value == null || value.codePointCount(0, value.length()) > outputLimit - outputCodePoints())
                return exceeded = true;
            output.append(value);
            return true;
        }

        synchronized int tokensUsed() { return tokens; }
        synchronized int stepsUsed() { return steps; }
        synchronized boolean exceeded() { return exceeded; }
        synchronized int outputCodePoints() { return output.codePointCount(0, output.length()); }
    }

    static final class Cancellation {
        private boolean cancelled;
        private CancelReason reason;
        private Future<?> future;

        synchronized boolean cancel(CancelReason value) {
            if (cancelled || value == null) return false;
            cancelled = true;
            reason = value;
            if (future != null) future.cancel(true);
            return true;
        }
        synchronized boolean cancelled() { return cancelled; }
        synchronized CancelReason reason() { return reason; }
        synchronized void attach(Future<?> value) { future = value; if (cancelled && value != null) value.cancel(true); }
    }

    static final class Result {
        final int schemaVersion = SCHEMA_VERSION;
        final Code code;
        final int tokensUsed;
        final int stepsUsed;
        final int outputCodePoints;
        final boolean publicLogSafe = false;
        private Result(Code code, Budget budget) {
            this.code = code;
            this.tokensUsed = budget == null ? 0 : budget.tokensUsed();
            this.stepsUsed = budget == null ? 0 : budget.stepsUsed();
            this.outputCodePoints = budget == null ? 0 : budget.outputCodePoints();
        }
    }

    Result run(OpenCodeTaskProtocol.Task task, OpenCodeWorkspaceBoundary.Lease lease,
            Budget budget, Adapter adapter, Cancellation cancellation) {
        if (!valid(task, lease, budget) || adapter == null) return new Result(Code.DENIED, budget);
        Cancellation signal = cancellation == null ? new Cancellation() : cancellation;
        if (signal.cancelled()) return new Result(Code.CANCELLED, budget);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> { adapter.run(task, lease, budget, signal); return null; });
        signal.attach(future);
        try {
            future.get(budget.timeoutMs, TimeUnit.MILLISECONDS);
            if (signal.cancelled()) return new Result(Code.CANCELLED, budget);
            return new Result(budget.exceeded() ? Code.BUDGET_EXCEEDED : Code.SUCCESS, budget);
        } catch (TimeoutException timeout) {
            future.cancel(true);
            return new Result(Code.TIMEOUT, budget);
        } catch (CancellationException cancelled) {
            return new Result(Code.CANCELLED, budget);
        } catch (InterruptedException interrupted) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            return new Result(Code.CANCELLED, budget);
        } catch (ExecutionException failed) {
            return new Result(budget.exceeded() ? Code.BUDGET_EXCEEDED : Code.ADAPTER_FAILURE, budget);
        } finally {
            executor.shutdownNow();
        }
    }

    private static boolean valid(OpenCodeTaskProtocol.Task task, OpenCodeWorkspaceBoundary.Lease lease,
            Budget budget) {
        return task != null && lease != null && task.taskId.equals(lease.taskId)
                && task.dryRun && task.requiresConfirmation
                && OpenCodeTaskProtocol.EXECUTION_STATE.equals(task.executionState)
                && budget != null && budget.profile != null && budget.agent != null
                && budget.timeoutMs >= MIN_TIMEOUT_MS && budget.timeoutMs <= MAX_TIMEOUT_MS
                && budget.tokenLimit > 0 && budget.tokenLimit <= MAX_TOKEN_BUDGET
                && budget.stepLimit > 0 && budget.stepLimit <= MAX_STEP_BUDGET
                && budget.outputLimit > 0 && budget.outputLimit <= MAX_OUTPUT_CODE_POINTS;
    }
}
