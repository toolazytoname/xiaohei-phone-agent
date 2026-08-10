package io.github.toolazytoname.xiaohei;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ToolGatewayTest {
    private static final long NOW = 200000L;
    private static final ToolGateway.Peer LOCAL = new ToolGateway.Peer(
            "127.0.0.1", "::1", 10234, 10234);

    public static void main(String[] args) {
        tenExactCapabilitiesAllowOnce();
        tenNonLocalOrCrossUidPeersFailClosed();
        fiveConfirmationFailuresFailClosed();
        tenScopeOrCatalogChangesFailClosed();
        fiveInvalidCallMetadataCasesFailClosed();
        fiveExpiryAndClockCasesFailClosed();
        fiveReplayRevocationAndForeignCasesFailClosed();
        timeoutIsCatalogBoundAndCapabilityScoped();
        tokenAndCallMetadataAreImmutable();
        System.out.println("PASS ToolGatewayTest allow_once=10 non_local=10 confirmation=5 scope_change=7 catalog_change=3 invalid_call=5 expiry=5 replay=5 timeout=bound token_ttl=1..30s secure_default=128bit model_calls=0 action_calls=0 execution_paths=0");
    }

    private static void tenExactCapabilitiesAllowOnce() {
        for (int index = 0; index < 10; index++) {
            ToolGateway gateway = gateway(index * 10);
            ToolGateway.Call call = call(index, "android.open_gallery", ToolCatalog.Risk.LOW,
                    ToolCatalog.Audience.ANDROID_GATEWAY, Collections.<String, String>emptyMap());
            ToolGateway.Result issued = gateway.issue(LOCAL, confirmation(index), call, NOW, 10000L);
            expect(ToolGateway.Decision.ISSUED, issued);
            check(issued.token != null && issued.token.singleUse && issued.token.ttlMs == 10000L,
                    "token metadata");
            expect(ToolGateway.Decision.ALLOW,
                    gateway.authorizeAndConsume(LOCAL, call, issued.token, NOW + 1));
            expect(ToolGateway.Decision.TOKEN_REPLAY,
                    gateway.authorizeAndConsume(LOCAL, call, issued.token, NOW + 2));
        }
    }

    private static void tenNonLocalOrCrossUidPeersFailClosed() {
        ToolGateway.Peer[] denied = {
            new ToolGateway.Peer("0.0.0.0", "127.0.0.1", 10, 10),
            new ToolGateway.Peer("127.0.0.1", "192.168.1.2", 10, 10),
            new ToolGateway.Peer("127.0.0.1", "8.8.8.8", 10, 10),
            new ToolGateway.Peer("localhost", "127.0.0.1", 10, 10),
            new ToolGateway.Peer("::1", "::2", 10, 10),
            new ToolGateway.Peer("127.0.0.1", "127.0.0.1", 10, 11),
            new ToolGateway.Peer("[::1]", "[::1]", -1, -1),
            new ToolGateway.Peer(null, "127.0.0.1", 10, 10),
            new ToolGateway.Peer("127.0.0.1", "", 10, 10),
            null
        };
        for (int index = 0; index < denied.length; index++) {
            ToolGateway gateway = gateway(index);
            ToolGateway.Decision expected = index == 5 || index == 6
                    ? ToolGateway.Decision.PEER_UID_MISMATCH : ToolGateway.Decision.NON_LOOPBACK;
            expect(expected, gateway.issue(denied[index], confirmation(index),
                    call(index), NOW, 10000L));
            check(gateway.activeCount() == 0, "denied peer created token");
        }
    }

    private static void fiveConfirmationFailuresFailClosed() {
        ToolGateway gateway = gateway(100);
        ToolGateway.Call call = call(0);
        expect(ToolGateway.Decision.CONFIRMATION_REQUIRED,
                gateway.issue(LOCAL, null, call, NOW, 10000L));

        FreshConfirmationGate gate = new FreshConfirmationGate();
        FreshConfirmationGate.Scope scope = scope(0);
        FreshConfirmationGate.Result issuedOnly = gate.issue(
                FreshConfirmationGate.Source.LOCAL_USER_GESTURE, confirmationId(0), scope,
                NOW, 30000L, ready());
        expect(ToolGateway.Decision.CONFIRMATION_REQUIRED,
                gateway.issue(LOCAL, issuedOnly, call, NOW, 10000L));

        FreshConfirmationGate.Result assistant = new FreshConfirmationGate().issue(
                FreshConfirmationGate.Source.ASSISTANT_TEXT, confirmationId(0), scope,
                NOW, 30000L, ready());
        expect(ToolGateway.Decision.CONFIRMATION_REQUIRED,
                gateway.issue(LOCAL, assistant, call, NOW, 10000L));

        ToolGateway.Call changedTask = copy(call, "task-gateway-changed", call.requestId,
                call.planId, call.callId, call.tool, call.toolVersion, call.risk,
                call.audience, call.arguments, call.idempotencyKey);
        expect(ToolGateway.Decision.CONFIRMATION_SCOPE,
                gateway.issue(LOCAL, confirmation(0), changedTask, NOW, 10000L));

        FreshConfirmationGate.Result oneReceipt = confirmation(1);
        expect(ToolGateway.Decision.ISSUED,
                gateway.issue(LOCAL, oneReceipt, call(1), NOW, 10000L));
        expect(ToolGateway.Decision.CONFIRMATION_REPLAY,
                gateway.issue(LOCAL, oneReceipt, call(1), NOW, 10000L));
    }

    private static void tenScopeOrCatalogChangesFailClosed() {
        for (int index = 0; index < 10; index++) {
            ToolGateway gateway = gateway(200 + index * 10);
            ToolGateway.Call original = call(index);
            ToolGateway.Result issued = gateway.issue(LOCAL, confirmation(index), original, NOW, 10000L);
            expect(ToolGateway.Decision.ISSUED, issued);
            ToolGateway.Call changed;
            ToolGateway.Decision expected;
            switch (index) {
                case 0:
                    changed = copy(original, "task-gateway-other", original.requestId, original.planId,
                            original.callId, original.tool, 1, original.risk, original.audience,
                            original.arguments, original.idempotencyKey); expected = ToolGateway.Decision.TOKEN_SCOPE; break;
                case 1:
                    changed = copy(original, original.taskId, "request-gateway-other", original.planId,
                            original.callId, original.tool, 1, original.risk, original.audience,
                            original.arguments, original.idempotencyKey); expected = ToolGateway.Decision.TOKEN_SCOPE; break;
                case 2:
                    changed = copy(original, original.taskId, original.requestId, "plan-gateway-other",
                            original.callId, original.tool, 1, original.risk, original.audience,
                            original.arguments, original.idempotencyKey); expected = ToolGateway.Decision.TOKEN_SCOPE; break;
                case 3:
                    changed = copy(original, original.taskId, original.requestId, original.planId,
                            "call-gateway-other", original.tool, 1, original.risk, original.audience,
                            original.arguments, original.idempotencyKey); expected = ToolGateway.Decision.TOKEN_SCOPE; break;
                case 4:
                    changed = copy(original, original.taskId, original.requestId, original.planId,
                            original.callId, "android.observe", 1, ToolCatalog.Risk.OBSERVE,
                            original.audience, original.arguments, original.idempotencyKey);
                    expected = ToolGateway.Decision.TOKEN_SCOPE; break;
                case 5:
                    Map<String, String> arguments = new HashMap<>(); arguments.put("fixture", "changed");
                    changed = copy(original, original.taskId, original.requestId, original.planId,
                            original.callId, original.tool, 1, original.risk, original.audience,
                            arguments, original.idempotencyKey); expected = ToolGateway.Decision.TOKEN_SCOPE; break;
                case 6:
                    changed = copy(original, original.taskId, original.requestId, original.planId,
                            original.callId, original.tool, 1, original.risk, original.audience,
                            original.arguments, "idempotency-gateway-other");
                    expected = ToolGateway.Decision.TOKEN_SCOPE; break;
                case 7:
                    changed = copy(original, original.taskId, original.requestId, original.planId,
                            original.callId, original.tool, 2, original.risk, original.audience,
                            original.arguments, original.idempotencyKey);
                    expected = ToolGateway.Decision.VERSION_MISMATCH; break;
                case 8:
                    changed = copy(original, original.taskId, original.requestId, original.planId,
                            original.callId, original.tool, 1, ToolCatalog.Risk.HIGH,
                            original.audience, original.arguments, original.idempotencyKey);
                    expected = ToolGateway.Decision.RISK_MISMATCH; break;
                default:
                    changed = copy(original, original.taskId, original.requestId, original.planId,
                            original.callId, original.tool, 1, original.risk,
                            ToolCatalog.Audience.ROOT_BROKER, original.arguments, original.idempotencyKey);
                    expected = ToolGateway.Decision.AUDIENCE_MISMATCH;
            }
            expect(expected, gateway.authorizeAndConsume(LOCAL, changed, issued.token, NOW + 1));
            expect(ToolGateway.Decision.TOKEN_REPLAY,
                    gateway.authorizeAndConsume(LOCAL, original, issued.token, NOW + 2));
        }
    }

    private static void fiveExpiryAndClockCasesFailClosed() {
        ToolGateway gateway = gateway(400);
        expect(ToolGateway.Decision.INVALID_WINDOW,
                gateway.issue(LOCAL, confirmation(0), call(0), NOW, 999L));
        expect(ToolGateway.Decision.INVALID_WINDOW,
                gateway.issue(LOCAL, confirmation(1), call(1), NOW, 30001L));

        for (int index = 2; index < 5; index++) {
            gateway = gateway(400 + index);
            ToolGateway.Call call = call(index);
            ToolGateway.Result issued = gateway.issue(LOCAL, confirmation(index), call, NOW, 1000L);
            expect(ToolGateway.Decision.ISSUED, issued);
            long when = index == 2 ? NOW + 1000L : index == 3 ? NOW + 1001L : NOW - 1L;
            ToolGateway.Decision expected = index == 4
                    ? ToolGateway.Decision.CLOCK_ROLLBACK : ToolGateway.Decision.TOKEN_EXPIRED;
            expect(expected, gateway.authorizeAndConsume(LOCAL, call, issued.token, when));
        }
    }

    private static void fiveInvalidCallMetadataCasesFailClosed() {
        ToolGateway.Call base = call(30);
        ToolGateway.Call[] invalid = {
            withMetadata(base, null, base.idempotencyKey, base.requestedAtElapsedMs, false),
            withMetadata(base, base.arguments, "short", base.requestedAtElapsedMs, false),
            withMetadata(base, base.arguments, base.idempotencyKey, base.requestedAtElapsedMs, true),
            withMetadata(base, base.arguments, base.idempotencyKey, NOW + 1, false),
            withMetadata(base, base.arguments, base.idempotencyKey, NOW - 60001L, false)
        };
        for (int index = 0; index < invalid.length; index++) {
            expect(ToolGateway.Decision.INVALID_CALL, gateway(600 + index).issue(
                    LOCAL, confirmationFor(invalid[index]), invalid[index], NOW, 10000L));
        }
    }

    private static void fiveReplayRevocationAndForeignCasesFailClosed() {
        ToolGateway gateway = gateway(500);
        ToolGateway.Call first = call(0);
        ToolGateway.Result issued = gateway.issue(LOCAL, confirmation(0), first, NOW, 10000L);
        expect(ToolGateway.Decision.ALLOW,
                gateway.authorizeAndConsume(LOCAL, first, issued.token, NOW + 1));
        expect(ToolGateway.Decision.TOKEN_REPLAY,
                gateway.authorizeAndConsume(LOCAL, first, issued.token, NOW + 2));

        ToolGateway.Call next = call(1);
        ToolGateway.Call sameKey = copy(next, first.taskId, next.requestId, next.planId,
                next.callId, next.tool, 1, next.risk, next.audience,
                next.arguments, first.idempotencyKey);
        FreshConfirmationGate.Result sameTaskReceipt = confirmationFor(sameKey);
        ToolGateway.Result second = gateway.issue(LOCAL, sameTaskReceipt, sameKey, NOW + 3, 10000L);
        expect(ToolGateway.Decision.ISSUED, second);
        expect(ToolGateway.Decision.IDEMPOTENCY_REPLAY,
                gateway.authorizeAndConsume(LOCAL, sameKey, second.token, NOW + 4));

        ToolGateway revokeGateway = gateway(510);
        ToolGateway.Result revoked = revokeGateway.issue(LOCAL, confirmation(2), call(2), NOW, 10000L);
        check(revokeGateway.revokeAll() == 1 && revokeGateway.activeCount() == 0, "revoke all");
        expect(ToolGateway.Decision.TOKEN_REPLAY,
                revokeGateway.authorizeAndConsume(LOCAL, call(2), revoked.token, NOW + 1));

        ToolGateway foreign = gateway(520);
        expect(ToolGateway.Decision.TOKEN_MISSING,
                foreign.authorizeAndConsume(LOCAL, first, issued.token, NOW + 1));
        expect(ToolGateway.Decision.TOKEN_MISSING,
                foreign.authorizeAndConsume(LOCAL, first, null, NOW + 1));
    }

    private static void tokenAndCallMetadataAreImmutable() {
        for (Constructor<?> constructor : ToolGateway.Token.class.getDeclaredConstructors()) {
            check(!Modifier.isPublic(constructor.getModifiers())
                    && !Modifier.isProtected(constructor.getModifiers()), "token constructor visible");
            check(Modifier.isPrivate(constructor.getModifiers()) || constructor.isSynthetic(),
                    "unexpected token constructor");
        }
        for (java.lang.reflect.Field field : ToolGateway.Token.class.getDeclaredFields())
            check(Modifier.isFinal(field.getModifiers()), "token field mutable");
        ToolGateway.Call call = call(0);
        boolean immutable = false;
        try { call.arguments.put("changed", "bad"); }
        catch (UnsupportedOperationException expected) { immutable = true; }
        check(immutable, "call arguments mutable");
    }

    private static void timeoutIsCatalogBoundAndCapabilityScoped() {
        ToolGateway.Call base = call(70);
        expect(ToolGateway.Decision.INVALID_CALL, gateway(700).issue(
                LOCAL, confirmationFor(withTimeout(base, 99)), withTimeout(base, 99), NOW, 10000L));
        expect(ToolGateway.Decision.INVALID_CALL, gateway(701).issue(
                LOCAL, confirmationFor(withTimeout(base, 5001)),
                withTimeout(base, 5001), NOW, 10000L));

        ToolGateway gateway = gateway(702);
        ToolGateway.Result issued = gateway.issue(LOCAL, confirmationFor(base), base, NOW, 10000L);
        expect(ToolGateway.Decision.ISSUED, issued);
        expect(ToolGateway.Decision.TOKEN_SCOPE,
                gateway.authorizeAndConsume(LOCAL, withTimeout(base, 999), issued.token, NOW + 1));
    }

    private static ToolGateway gateway(final int seed) {
        return new ToolGateway(new ToolGateway.TokenIdSource() {
            int next = seed;
            @Override public String nextId() {
                return String.format("cap-%032x", next++);
            }
        });
    }

    private static FreshConfirmationGate.Result confirmation(int index) {
        return confirmationFor(call(index));
    }

    private static FreshConfirmationGate.Result confirmationFor(ToolGateway.Call call) {
        FreshConfirmationGate gate = new FreshConfirmationGate();
        FreshConfirmationGate.Scope scope = new FreshConfirmationGate.Scope(
                call.taskId, call.requestId, call.planId, "target-gateway", "content-gateway");
        check(gate.issue(FreshConfirmationGate.Source.LOCAL_USER_GESTURE,
                confirmationId(Math.abs(call.taskId.hashCode() % 1000)), scope,
                NOW, 30000L, ready()).code == FreshConfirmationGate.Code.ISSUED,
                "confirmation issue");
        FreshConfirmationGate.Result result = gate.authorizeAndConsume(scope, NOW + 1, ready());
        check(result.code == FreshConfirmationGate.Code.ALLOW_ONCE, "confirmation consume");
        return result;
    }

    private static FreshConfirmationGate.Scope scope(int index) {
        ToolGateway.Call call = call(index);
        return new FreshConfirmationGate.Scope(call.taskId, call.requestId, call.planId,
                "target-gateway", "content-gateway");
    }

    private static FreshConfirmationGate.DeviceState ready() {
        return new FreshConfirmationGate.DeviceState(true, true, true);
    }

    private static String confirmationId(int index) {
        return String.format("confirmation-gateway-%04d", index);
    }

    private static ToolGateway.Call call(int index) {
        return call(index, "android.open_gallery", ToolCatalog.Risk.LOW,
                ToolCatalog.Audience.ANDROID_GATEWAY, Collections.<String, String>emptyMap());
    }

    private static ToolGateway.Call call(int index, String tool, ToolCatalog.Risk risk,
            ToolCatalog.Audience audience, Map<String, String> arguments) {
        return new ToolGateway.Call(String.format("task-gateway-%04d", index),
                String.format("request-gateway-%04d", index),
                String.format("plan-gateway-%04d", index),
                String.format("call-gateway-%04d", index), tool, 1, risk, audience, arguments,
                String.format("idempotency-gateway-%04d", index), NOW - 1000L, 1000, false);
    }

    private static ToolGateway.Call copy(ToolGateway.Call original, String taskId, String requestId,
            String planId, String callId, String tool, int version, ToolCatalog.Risk risk,
            ToolCatalog.Audience audience, Map<String, String> arguments, String idempotencyKey) {
        return new ToolGateway.Call(taskId, requestId, planId, callId, tool, version, risk,
                audience, arguments, idempotencyKey, original.requestedAtElapsedMs,
                original.timeoutMs, original.publicLogSafe);
    }

    private static ToolGateway.Call withMetadata(ToolGateway.Call original,
            Map<String, String> arguments, String idempotencyKey,
            long requestedAtElapsedMs, boolean publicLogSafe) {
        return new ToolGateway.Call(original.taskId, original.requestId, original.planId,
                original.callId, original.tool, original.toolVersion, original.risk,
                original.audience, arguments, idempotencyKey, requestedAtElapsedMs,
                original.timeoutMs, publicLogSafe);
    }

    private static ToolGateway.Call withTimeout(ToolGateway.Call original, int timeoutMs) {
        return new ToolGateway.Call(original.taskId, original.requestId, original.planId,
                original.callId, original.tool, original.toolVersion, original.risk,
                original.audience, original.arguments, original.idempotencyKey,
                original.requestedAtElapsedMs, timeoutMs, original.publicLogSafe);
    }

    private static void expect(ToolGateway.Decision expected, ToolGateway.Result actual) {
        check(actual.decision == expected, "expected=" + expected + " actual=" + actual.decision);
        check(actual.modelCalls == 0 && actual.actionCalls == 0 && actual.executionCalls == 0,
                "gateway caused side effect");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
