package io.github.toolazytoname.xiaohei;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Pure rules-first validation for schema-v1 dry-run plans. It never calls or executes a tool. */
final class TaskPlanValidator {
    static final int SCHEMA_VERSION = 1;
    static final int MAX_STEPS = 8;
    static final int MIN_TIMEOUT_MS = 1000;
    static final int MAX_TIMEOUT_MS = 60000;
    static final int TOOL_VERSION = 1;

    enum Code {
        OK,
        INVALID_PLAN,
        STEP_COUNT,
        DUPLICATE_STEP_ID,
        DUPLICATE_IDEMPOTENCY_KEY,
        UNKNOWN_TOOL,
        RISK_MISMATCH,
        INVALID_TOOL_VERSION,
        INVALID_DEPENDENCY,
        DEPENDENCY_CYCLE,
        INVALID_ARGUMENTS
    }

    static final class Step {
        final String id;
        final String tool;
        final int toolVersion;
        final ToolCatalog.Risk risk;
        final List<String> dependsOn;
        final Map<String, String> arguments;
        final String idempotencyKey;

        Step(String id, String tool, int toolVersion, ToolCatalog.Risk risk,
                List<String> dependsOn, Map<String, String> arguments, String idempotencyKey) {
            this.id = id;
            this.tool = tool;
            this.toolVersion = toolVersion;
            this.risk = risk;
            this.dependsOn = dependsOn == null ? null
                    : Collections.unmodifiableList(new ArrayList<>(dependsOn));
            this.arguments = arguments == null ? null
                    : Collections.unmodifiableMap(new HashMap<>(arguments));
            this.idempotencyKey = idempotencyKey;
        }
    }

    static final class Plan {
        final int schemaVersion;
        final String planId;
        final String requestId;
        final boolean dryRun;
        final int stepBudget;
        final int timeoutMs;
        final String createdAt;
        final boolean publicLogSafe;
        final List<Step> steps;

        Plan(int schemaVersion, String planId, String requestId, boolean dryRun,
                int stepBudget, int timeoutMs, String createdAt, boolean publicLogSafe,
                List<Step> steps) {
            this.schemaVersion = schemaVersion;
            this.planId = planId;
            this.requestId = requestId;
            this.dryRun = dryRun;
            this.stepBudget = stepBudget;
            this.timeoutMs = timeoutMs;
            this.createdAt = createdAt;
            this.publicLogSafe = publicLogSafe;
            this.steps = steps == null ? null : Collections.unmodifiableList(new ArrayList<>(steps));
        }
    }

    static final class Result {
        final Code code;
        final int stepCount;
        final int modelCalls;
        final int actionCalls;

        private Result(Code code, int stepCount) {
            this.code = code;
            this.stepCount = stepCount;
            this.modelCalls = 0;
            this.actionCalls = 0;
        }
    }

    private TaskPlanValidator() {}

    static Result validate(Plan plan) {
        if (!validPlanEnvelope(plan)) return result(Code.INVALID_PLAN, plan);
        if (plan.steps.isEmpty() || plan.steps.size() > MAX_STEPS
                || plan.steps.size() > plan.stepBudget) return result(Code.STEP_COUNT, plan);

        Map<String, Step> byId = new HashMap<>();
        Set<String> idempotencyKeys = new HashSet<>();
        for (Step step : plan.steps) {
            if (step == null || !validShortId(step.id)) return result(Code.INVALID_PLAN, plan);
            if (byId.put(step.id, step) != null) return result(Code.DUPLICATE_STEP_ID, plan);
            if (!validIdempotencyKey(step.idempotencyKey)) return result(Code.INVALID_PLAN, plan);
            if (!idempotencyKeys.add(step.idempotencyKey))
                return result(Code.DUPLICATE_IDEMPOTENCY_KEY, plan);
            if (step.toolVersion != TOOL_VERSION) return result(Code.INVALID_TOOL_VERSION, plan);
            ToolCatalog.Risk catalogRisk = ToolCatalog.risk(step.tool);
            if (catalogRisk == null) return result(Code.UNKNOWN_TOOL, plan);
            if (step.risk != catalogRisk) return result(Code.RISK_MISMATCH, plan);
            if (!validArguments(step.arguments)) return result(Code.INVALID_ARGUMENTS, plan);
            if (!validDependencies(step.dependsOn)) return result(Code.INVALID_DEPENDENCY, plan);
        }

        for (Step step : plan.steps)
            for (String dependency : step.dependsOn)
                if (!byId.containsKey(dependency)) return result(Code.INVALID_DEPENDENCY, plan);

        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        for (Step step : plan.steps)
            if (hasCycle(step.id, byId, visiting, visited))
                return result(Code.DEPENDENCY_CYCLE, plan);
        return result(Code.OK, plan);
    }

    private static boolean validPlanEnvelope(Plan plan) {
        return plan != null && plan.schemaVersion == SCHEMA_VERSION
                && validLongId(plan.planId) && validLongId(plan.requestId)
                && plan.dryRun && plan.stepBudget >= 1 && plan.stepBudget <= MAX_STEPS
                && plan.timeoutMs >= MIN_TIMEOUT_MS && plan.timeoutMs <= MAX_TIMEOUT_MS
                && validTimestamp(plan.createdAt) && !plan.publicLogSafe && plan.steps != null;
    }

    private static boolean validArguments(Map<String, String> arguments) {
        if (arguments == null || arguments.size() > 32) return false;
        for (Map.Entry<String, String> entry : arguments.entrySet())
            if (entry.getKey() == null || !entry.getKey().matches("[a-z][a-z0-9_]{0,63}")
                    || entry.getValue() == null || entry.getValue().length() > 1024) return false;
        return true;
    }

    private static boolean validDependencies(List<String> dependencies) {
        if (dependencies == null || dependencies.size() > MAX_STEPS - 1) return false;
        Set<String> unique = new HashSet<>();
        for (String dependency : dependencies)
            if (!validShortId(dependency) || !unique.add(dependency)) return false;
        return true;
    }

    private static boolean hasCycle(String id, Map<String, Step> byId,
            Set<String> visiting, Set<String> visited) {
        if (visited.contains(id)) return false;
        if (!visiting.add(id)) return true;
        for (String dependency : byId.get(id).dependsOn)
            if (hasCycle(dependency, byId, visiting, visited)) return true;
        visiting.remove(id);
        visited.add(id);
        return false;
    }

    private static boolean validLongId(String value) {
        return value != null && value.matches("[A-Za-z0-9][A-Za-z0-9._:-]{7,127}");
    }

    private static boolean validShortId(String value) {
        return value != null && value.matches("[A-Za-z0-9][A-Za-z0-9._:-]{0,63}");
    }

    private static boolean validIdempotencyKey(String value) {
        return value != null && value.matches("[A-Za-z0-9][A-Za-z0-9._:-]{15,127}");
    }

    private static boolean validTimestamp(String value) {
        if (value == null) return false;
        try {
            Instant.parse(value);
            return value.endsWith("Z");
        } catch (DateTimeParseException invalid) {
            return false;
        }
    }

    private static Result result(Code code, Plan plan) {
        return new Result(code, plan == null || plan.steps == null ? 0 : plan.steps.size());
    }
}
