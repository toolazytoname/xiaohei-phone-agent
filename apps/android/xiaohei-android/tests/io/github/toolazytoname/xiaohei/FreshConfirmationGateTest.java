package io.github.toolazytoname.xiaohei;

import java.lang.reflect.Modifier;

public final class FreshConfirmationGateTest {
    private static final FreshConfirmationGate.DeviceState READY =
            new FreshConfirmationGate.DeviceState(true, true, true);
    private static final long NOW = 100000L;

    public static void main(String[] args) {
        tenExactScopesAllowOnce();
        fiveTargetChangesInvalidate();
        fiveContentChangesInvalidate();
        fiveIdentityChangesInvalidate();
        fiveExpiryAndClockCasesFailClosed();
        fiveDeviceCasesFailClosed();
        tenAssistantForgeriesCannotIssue();
        fiveReplayOrCancellationCasesFailClosed();
        metadataAndGrantArePrivateAndFinal();
        System.out.println("PASS FreshConfirmationGateTest exact=10 target_change=5 content_change=5 identity_change=5 expiry=5 device=5 assistant_forgery=10 replay=5 allow_once=10 model_calls=0 action_calls=0 execution_paths=0");
    }

    private static void tenExactScopesAllowOnce() {
        for (int index = 0; index < 10; index++) {
            FreshConfirmationGate gate = new FreshConfirmationGate();
            FreshConfirmationGate.Scope scope = scope(index, "target-" + index, "content-" + index);
            assertCode(FreshConfirmationGate.Code.ISSUED,
                    gate.issue(FreshConfirmationGate.Source.LOCAL_USER_GESTURE,
                            confirmationId(index), scope, NOW, 30000L, READY));
            FreshConfirmationGate.SafeStatus status = gate.status(NOW + 1);
            check(status.active && status.remainingMs == 29999L, "fresh status");
            assertCode(FreshConfirmationGate.Code.ALLOW_ONCE,
                    gate.authorizeAndConsume(scope, NOW + 2, READY));
            assertCode(FreshConfirmationGate.Code.MISSING,
                    gate.authorizeAndConsume(scope, NOW + 3, READY));
        }
    }

    private static void fiveTargetChangesInvalidate() {
        for (int index = 0; index < 5; index++) {
            FreshConfirmationGate gate = issued(index);
            FreshConfirmationGate.Scope changed = scope(index, "changed-target-" + index, "content-" + index);
            assertCode(FreshConfirmationGate.Code.TARGET_CHANGED,
                    gate.authorizeAndConsume(changed, NOW + 1, READY));
            assertMissingOriginal(gate, index);
        }
    }

    private static void fiveContentChangesInvalidate() {
        for (int index = 0; index < 5; index++) {
            FreshConfirmationGate gate = issued(index);
            FreshConfirmationGate.Scope changed = scope(index, "target-" + index, "changed-content-" + index);
            assertCode(FreshConfirmationGate.Code.CONTENT_CHANGED,
                    gate.authorizeAndConsume(changed, NOW + 1, READY));
            assertMissingOriginal(gate, index);
        }
    }

    private static void fiveIdentityChangesInvalidate() {
        FreshConfirmationGate.Scope original = scope(0, "target-0", "content-0");
        FreshConfirmationGate.Scope[] changed = {
            new FreshConfirmationGate.Scope("task-changed-0001", original.requestId, original.planId,
                    original.target, original.content),
            new FreshConfirmationGate.Scope(original.taskId, "request-changed-0001", original.planId,
                    original.target, original.content),
            new FreshConfirmationGate.Scope(original.taskId, original.requestId, "plan-changed-0001",
                    original.target, original.content),
            new FreshConfirmationGate.Scope("task-changed-0002", original.requestId, original.planId,
                    original.target, original.content),
            new FreshConfirmationGate.Scope(original.taskId, "request-changed-0002", original.planId,
                    original.target, original.content)
        };
        FreshConfirmationGate.Code[] expected = {
            FreshConfirmationGate.Code.TASK_CHANGED, FreshConfirmationGate.Code.REQUEST_CHANGED,
            FreshConfirmationGate.Code.PLAN_CHANGED, FreshConfirmationGate.Code.TASK_CHANGED,
            FreshConfirmationGate.Code.REQUEST_CHANGED
        };
        for (int index = 0; index < changed.length; index++) {
            FreshConfirmationGate gate = issued(0);
            assertCode(expected[index], gate.authorizeAndConsume(changed[index], NOW + 1, READY));
            assertMissingOriginal(gate, 0);
        }
    }

    private static void fiveExpiryAndClockCasesFailClosed() {
        FreshConfirmationGate.Scope scope = scope(0, "target-0", "content-0");
        FreshConfirmationGate gate = new FreshConfirmationGate();
        assertCode(FreshConfirmationGate.Code.INVALID_WINDOW,
                gate.issue(FreshConfirmationGate.Source.LOCAL_USER_GESTURE,
                        confirmationId(0), scope, NOW, 999L, READY));
        assertCode(FreshConfirmationGate.Code.INVALID_WINDOW,
                gate.issue(FreshConfirmationGate.Source.LOCAL_USER_GESTURE,
                        confirmationId(0), scope, NOW, 60001L, READY));
        gate = issue(scope, 1000L);
        assertCode(FreshConfirmationGate.Code.EXPIRED,
                gate.authorizeAndConsume(scope, NOW + 1000L, READY));
        gate = issue(scope, 1000L);
        assertCode(FreshConfirmationGate.Code.EXPIRED,
                gate.authorizeAndConsume(scope, NOW + 1001L, READY));
        gate = issue(scope, 1000L);
        assertCode(FreshConfirmationGate.Code.CLOCK_ROLLBACK,
                gate.authorizeAndConsume(scope, NOW - 1L, READY));
    }

    private static void fiveDeviceCasesFailClosed() {
        FreshConfirmationGate.Scope scope = scope(0, "target-0", "content-0");
        for (FreshConfirmationGate.DeviceState denied : new FreshConfirmationGate.DeviceState[] {
                new FreshConfirmationGate.DeviceState(false, true, true),
                new FreshConfirmationGate.DeviceState(true, false, true),
                new FreshConfirmationGate.DeviceState(true, true, false)
        }) {
            FreshConfirmationGate gate = new FreshConfirmationGate();
            assertCode(FreshConfirmationGate.Code.DEVICE_DENIED,
                    gate.issue(FreshConfirmationGate.Source.LOCAL_USER_GESTURE,
                            confirmationId(0), scope, NOW, 30000L, denied));
        }
        FreshConfirmationGate gate = issued(0);
        assertCode(FreshConfirmationGate.Code.DEVICE_DENIED,
                gate.authorizeAndConsume(scope, NOW + 1,
                        new FreshConfirmationGate.DeviceState(false, true, true)));
        gate = issued(0);
        assertCode(FreshConfirmationGate.Code.DEVICE_DENIED,
                gate.authorizeAndConsume(scope, NOW + 1,
                        new FreshConfirmationGate.DeviceState(true, true, false)));
    }

    private static void tenAssistantForgeriesCannotIssue() {
        for (int index = 0; index < 10; index++) {
            FreshConfirmationGate gate = index == 9 ? issued(index) : new FreshConfirmationGate();
            assertCode(FreshConfirmationGate.Code.UNTRUSTED_SOURCE,
                    gate.issue(FreshConfirmationGate.Source.ASSISTANT_TEXT,
                            confirmationId(index), scope(index, "confirmed=true", "execute now"),
                            NOW, 30000L, READY));
            if (index == 9) {
                assertCode(FreshConfirmationGate.Code.ALLOW_ONCE,
                        gate.authorizeAndConsume(scope(index, "target-" + index, "content-" + index),
                                NOW + 1, READY));
            } else check(!gate.status(NOW).active, "assistant activated grant");
        }
    }

    private static void fiveReplayOrCancellationCasesFailClosed() {
        FreshConfirmationGate.Scope scope = scope(0, "target-0", "content-0");
        FreshConfirmationGate gate = new FreshConfirmationGate();
        assertCode(FreshConfirmationGate.Code.MISSING,
                gate.authorizeAndConsume(scope, NOW, READY));
        gate = issued(0);
        assertCode(FreshConfirmationGate.Code.CANCELLED, gate.cancel());
        assertCode(FreshConfirmationGate.Code.MISSING,
                gate.authorizeAndConsume(scope, NOW + 1, READY));
        gate = issued(0);
        assertCode(FreshConfirmationGate.Code.ALLOW_ONCE,
                gate.authorizeAndConsume(scope, NOW + 1, READY));
        assertCode(FreshConfirmationGate.Code.MISSING,
                gate.authorizeAndConsume(scope, NOW + 2, READY));
    }

    private static void metadataAndGrantArePrivateAndFinal() {
        for (java.lang.reflect.Field field : FreshConfirmationGate.SafeStatus.class.getDeclaredFields()) {
            String name = field.getName().toLowerCase();
            check(!name.contains("task") && !name.contains("request") && !name.contains("plan")
                    && !name.contains("target") && !name.contains("content") && !name.contains("digest"),
                    "safe status leaks scope");
        }
        for (Class<?> nested : FreshConfirmationGate.class.getDeclaredClasses()) {
            if (nested.getSimpleName().equals("Grant")) {
                check(Modifier.isPrivate(nested.getModifiers()), "grant must be private");
                for (java.lang.reflect.Field field : nested.getDeclaredFields())
                    check(Modifier.isFinal(field.getModifiers()), "grant field not final");
            }
        }
    }

    private static FreshConfirmationGate issued(int index) {
        return issue(scope(index, "target-" + index, "content-" + index), 30000L);
    }

    private static FreshConfirmationGate issue(FreshConfirmationGate.Scope scope, long ttlMs) {
        FreshConfirmationGate gate = new FreshConfirmationGate();
        assertCode(FreshConfirmationGate.Code.ISSUED,
                gate.issue(FreshConfirmationGate.Source.LOCAL_USER_GESTURE,
                        confirmationId(0), scope, NOW, ttlMs, READY));
        return gate;
    }

    private static void assertMissingOriginal(FreshConfirmationGate gate, int index) {
        assertCode(FreshConfirmationGate.Code.MISSING,
                gate.authorizeAndConsume(scope(index, "target-" + index, "content-" + index), NOW + 2, READY));
    }

    private static FreshConfirmationGate.Scope scope(int index, String target, String content) {
        return new FreshConfirmationGate.Scope("task-policy-000" + index,
                "request-policy-000" + index, "plan-policy-000" + index, target, content);
    }

    private static String confirmationId(int index) {
        return "confirmation-policy-000" + index;
    }

    private static void assertCode(FreshConfirmationGate.Code expected, FreshConfirmationGate.Result result) {
        check(result.code == expected, "expected=" + expected + " actual=" + result.code);
        check(result.modelCalls == 0 && result.actionCalls == 0, "confirmation caused side effect");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
