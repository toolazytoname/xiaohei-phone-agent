package io.github.toolazytoname.xiaohei;

/**
 * Fixed, non-sensitive remote-planner envelope. It deliberately has no transport or execution
 * capability, and cannot accept task text, UI data, file paths, images, or credentials.
 */
final class MinimalPlannerRequest {
    static final int CATALOG_VERSION = 1;

    final String action;
    final boolean dryRun;
    final int stepBudget;
    final int timeoutMs;
    final int catalogVersion;

    private MinimalPlannerRequest(int steps, int timeout) {
        action = "plan_complex_task";
        dryRun = true;
        stepBudget = steps;
        timeoutMs = timeout;
        catalogVersion = CATALOG_VERSION;
    }

    static MinimalPlannerRequest create(int steps, int timeout) {
        if (steps < 1 || steps > TaskPlanValidator.MAX_STEPS
                || timeout < TaskPlanValidator.MIN_TIMEOUT_MS
                || timeout > TaskPlanValidator.MAX_TIMEOUT_MS) return null;
        return new MinimalPlannerRequest(steps, timeout);
    }
}
