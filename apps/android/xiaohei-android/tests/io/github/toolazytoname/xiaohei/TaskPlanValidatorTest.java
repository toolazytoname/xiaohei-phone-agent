package io.github.toolazytoname.xiaohei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TaskPlanValidatorTest {
    private static final String CREATED_AT = "2026-08-10T09:15:00Z";

    public static void main(String[] args) {
        acceptsTenBoundedDags();
        rejectsFiveUnknownTools();
        rejectsZeroAndOverBudgetSteps();
        rejectsFiveRealCycles();
        rejectsTwelveMalformedPlans();
        System.out.println("PASS TaskPlanValidatorTest valid=10 unknown_tool=5 step_count=2 cycle=5 malformed=12 model_calls=0 action_calls=0 execution_paths=0");
    }

    private static void acceptsTenBoundedDags() {
        List<TaskPlanValidator.Plan> plans = Arrays.asList(
                plan(1, step("s1", "android.open_gallery", ToolCatalog.Risk.LOW)),
                plan(1, step("s1", "android.open_settings", ToolCatalog.Risk.LOW)),
                plan(1, step("s1", "android.open_dialer", ToolCatalog.Risk.LOW)),
                plan(1, step("s1", "android.adjust_volume", ToolCatalog.Risk.REVERSIBLE)),
                plan(1, step("s1", "android.observe", ToolCatalog.Risk.OBSERVE)),
                plan(2, step("s1", "android.open_settings", ToolCatalog.Risk.LOW),
                        step("s2", "android.observe", ToolCatalog.Risk.OBSERVE, "s1")),
                plan(3, step("s1", "android.open_gallery", ToolCatalog.Risk.LOW),
                        step("s2", "android.observe", ToolCatalog.Risk.OBSERVE, "s1"),
                        step("s3", "android.open_dialer", ToolCatalog.Risk.LOW, "s1")),
                plan(3, step("s3", "android.observe", ToolCatalog.Risk.OBSERVE, "s1", "s2"),
                        step("s1", "android.open_settings", ToolCatalog.Risk.LOW),
                        step("s2", "android.open_gallery", ToolCatalog.Risk.LOW)),
                plan(4, step("s1", "android.open_settings", ToolCatalog.Risk.LOW),
                        step("s2", "android.observe", ToolCatalog.Risk.OBSERVE, "s1"),
                        step("s3", "android.open_gallery", ToolCatalog.Risk.LOW, "s2"),
                        step("s4", "android.observe", ToolCatalog.Risk.OBSERVE, "s3")),
                plan(8, linear(8))
        );
        for (TaskPlanValidator.Plan plan : plans) assertCode(TaskPlanValidator.Code.OK, plan);
        boolean immutable = false;
        try { plans.get(0).steps.clear(); } catch (UnsupportedOperationException expected) { immutable = true; }
        if (!immutable) throw new AssertionError("plan steps must be immutable");
    }

    private static void rejectsFiveUnknownTools() {
        for (String tool : new String[] {
                "root.shell", "android.tap", "android.send_message", "opencode.run", "unknown.tool"
        }) assertCode(TaskPlanValidator.Code.UNKNOWN_TOOL,
                plan(1, step("s1", tool, ToolCatalog.Risk.HIGH)));
    }

    private static void rejectsZeroAndOverBudgetSteps() {
        assertCode(TaskPlanValidator.Code.STEP_COUNT, plan(1));
        assertCode(TaskPlanValidator.Code.STEP_COUNT, plan(8, linear(9)));
    }

    private static void rejectsFiveRealCycles() {
        assertCode(TaskPlanValidator.Code.DEPENDENCY_CYCLE,
                plan(1, step("s1", "android.observe", ToolCatalog.Risk.OBSERVE, "s1")));
        assertCode(TaskPlanValidator.Code.DEPENDENCY_CYCLE,
                plan(2, step("s1", "android.observe", ToolCatalog.Risk.OBSERVE, "s2"),
                        step("s2", "android.observe", ToolCatalog.Risk.OBSERVE, "s1")));
        assertCode(TaskPlanValidator.Code.DEPENDENCY_CYCLE,
                plan(3, step("s1", "android.observe", ToolCatalog.Risk.OBSERVE, "s3"),
                        step("s2", "android.observe", ToolCatalog.Risk.OBSERVE, "s1"),
                        step("s3", "android.observe", ToolCatalog.Risk.OBSERVE, "s2")));
        assertCode(TaskPlanValidator.Code.DEPENDENCY_CYCLE,
                plan(4, step("s1", "android.observe", ToolCatalog.Risk.OBSERVE, "s4"),
                        step("s2", "android.observe", ToolCatalog.Risk.OBSERVE, "s1"),
                        step("s3", "android.observe", ToolCatalog.Risk.OBSERVE, "s2"),
                        step("s4", "android.observe", ToolCatalog.Risk.OBSERVE, "s3")));
        assertCode(TaskPlanValidator.Code.DEPENDENCY_CYCLE,
                plan(3, step("s1", "android.observe", ToolCatalog.Risk.OBSERVE),
                        step("s2", "android.observe", ToolCatalog.Risk.OBSERVE, "s3"),
                        step("s3", "android.observe", ToolCatalog.Risk.OBSERVE, "s2")));
    }

    private static void rejectsTwelveMalformedPlans() {
        assertCode(TaskPlanValidator.Code.INVALID_PLAN, null);
        assertCode(TaskPlanValidator.Code.INVALID_PLAN, new TaskPlanValidator.Plan(
                2, "plan-route-0001", "request-route-0001", true, 1, 60000,
                CREATED_AT, false, Collections.singletonList(step("s1", "android.observe", ToolCatalog.Risk.OBSERVE))));
        assertCode(TaskPlanValidator.Code.INVALID_PLAN, new TaskPlanValidator.Plan(
                1, "short", "request-route-0001", true, 1, 60000,
                CREATED_AT, false, Collections.singletonList(step("s1", "android.observe", ToolCatalog.Risk.OBSERVE))));
        assertCode(TaskPlanValidator.Code.INVALID_PLAN, new TaskPlanValidator.Plan(
                1, "plan-route-0001", "request-route-0001", false, 1, 60000,
                CREATED_AT, false, Collections.singletonList(step("s1", "android.observe", ToolCatalog.Risk.OBSERVE))));
        assertCode(TaskPlanValidator.Code.INVALID_PLAN, new TaskPlanValidator.Plan(
                1, "plan-route-0001", "request-route-0001", true, 1, 999,
                CREATED_AT, false, Collections.singletonList(step("s1", "android.observe", ToolCatalog.Risk.OBSERVE))));
        assertCode(TaskPlanValidator.Code.INVALID_PLAN, new TaskPlanValidator.Plan(
                1, "plan-route-0001", "request-route-0001", true, 1, 60000,
                "2026-99-40T25:61:61Z", false, Collections.singletonList(step("s1", "android.observe", ToolCatalog.Risk.OBSERVE))));
        assertCode(TaskPlanValidator.Code.DUPLICATE_STEP_ID,
                plan(2, step("s1", "android.observe", ToolCatalog.Risk.OBSERVE),
                        step("s1", "android.open_gallery", ToolCatalog.Risk.LOW)));
        TaskPlanValidator.Step first = step("s1", "android.observe", ToolCatalog.Risk.OBSERVE);
        TaskPlanValidator.Step duplicateKey = new TaskPlanValidator.Step("s2", "android.open_gallery", 1,
                ToolCatalog.Risk.LOW, Collections.emptyList(), Collections.emptyMap(), first.idempotencyKey);
        assertCode(TaskPlanValidator.Code.DUPLICATE_IDEMPOTENCY_KEY, plan(2, first, duplicateKey));
        assertCode(TaskPlanValidator.Code.RISK_MISMATCH,
                plan(1, step("s1", "android.open_gallery", ToolCatalog.Risk.HIGH)));
        TaskPlanValidator.Step badVersion = new TaskPlanValidator.Step("s1", "android.observe", 2,
                ToolCatalog.Risk.OBSERVE, Collections.emptyList(), Collections.emptyMap(), "idem-plan-step-s1");
        assertCode(TaskPlanValidator.Code.INVALID_TOOL_VERSION, plan(1, badVersion));
        assertCode(TaskPlanValidator.Code.INVALID_DEPENDENCY,
                plan(1, step("s1", "android.observe", ToolCatalog.Risk.OBSERVE, "missing")));
        Map<String, String> badArguments = new HashMap<>();
        badArguments.put("Not-Allowed", "value");
        TaskPlanValidator.Step badArgumentStep = new TaskPlanValidator.Step("s1", "android.observe", 1,
                ToolCatalog.Risk.OBSERVE, Collections.emptyList(), badArguments, "idem-plan-step-s1");
        assertCode(TaskPlanValidator.Code.INVALID_ARGUMENTS, plan(1, badArgumentStep));
    }

    private static TaskPlanValidator.Plan plan(int budget, TaskPlanValidator.Step... steps) {
        return new TaskPlanValidator.Plan(1, "plan-route-0001", "request-route-0001",
                true, budget, 60000, CREATED_AT, false, Arrays.asList(steps));
    }

    private static TaskPlanValidator.Step step(String id, String tool, ToolCatalog.Risk risk,
            String... dependencies) {
        Map<String, String> arguments = new HashMap<>();
        arguments.put("fixture", "synthetic");
        return new TaskPlanValidator.Step(id, tool, 1, risk, Arrays.asList(dependencies),
                arguments, "idem-plan-step-" + id);
    }

    private static TaskPlanValidator.Step[] linear(int count) {
        List<TaskPlanValidator.Step> steps = new ArrayList<>();
        for (int index = 1; index <= count; index++) {
            String id = "s" + index;
            steps.add(step(id, index % 2 == 0 ? "android.observe" : "android.open_gallery",
                    index % 2 == 0 ? ToolCatalog.Risk.OBSERVE : ToolCatalog.Risk.LOW,
                    index == 1 ? new String[0] : new String[] {"s" + (index - 1)}));
        }
        return steps.toArray(new TaskPlanValidator.Step[0]);
    }

    private static void assertCode(TaskPlanValidator.Code expected, TaskPlanValidator.Plan plan) {
        TaskPlanValidator.Result result = TaskPlanValidator.validate(plan);
        if (result.code != expected) throw new AssertionError("expected=" + expected + " actual=" + result.code);
        if (result.modelCalls != 0 || result.actionCalls != 0) throw new AssertionError("side effect");
    }
}
