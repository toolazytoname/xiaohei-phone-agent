package io.github.toolazytoname.xiaohei;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class ToolExecutionCoordinatorTest {
    private static final long NOW = 500000L;
    private static final ToolGateway.Peer LOCAL = new ToolGateway.Peer(
            "127.0.0.1", "::1", 10234, 10234);

    public static void main(String[] args) throws Exception {
        fiveCatalogToolsSucceedOnce();
        fiveTimeoutsInterruptWorkers();
        fiveCancellationPathsInterruptOrSkip();
        fiveStructuredAdapterFailures();
        fiveAuthorizationAndReplayDenials();
        malformedPostAuthorizationScopeFailsClosed();
        resultMetadataIsPrivateAndImmutable();
        System.out.println("PASS ToolExecutionCoordinatorTest success=5 timeout=5 cancel=5 structured_failure=5 denied_replay=5 malformed_scope=reject adapter_calls_bounded=true worker_interrupts=9 output_private=true execution_paths=test_adapters_only");
    }

    private static void fiveCatalogToolsSucceedOnce() {
        String[] tools = {
            "android.open_settings", "android.open_gallery", "android.open_dialer",
            "android.adjust_volume", "android.observe"
        };
        for (int index = 0; index < tools.length; index++) {
            ToolGateway.Call call = call(index, tools[index], 500);
            ToolExecutionCoordinator.CancellationSignal signal =
                    new ToolExecutionCoordinator.CancellationSignal();
            ToolExecutionCoordinator.Result result = new ToolExecutionCoordinator().execute(
                    authorize(newGateway(index), call), call,
                    (ignored, ignoredSignal) -> new ToolExecutionCoordinator.AdapterResponse(
                            ToolExecutionCoordinator.AdapterStatus.SUCCESS,
                            Collections.singletonMap("fixture", "synthetic")),
                    signal);
            expect(ToolExecutionCoordinator.Status.SUCCESS,
                    ToolExecutionCoordinator.ErrorCode.NONE, 1, result);
            check("synthetic".equals(result.output.get("fixture")), "success output");
            check(!signal.cancel(ToolExecutionCoordinator.CancellationSignal.Reason.USER),
                    "completed call accepted late cancellation");
        }
    }

    private static void fiveTimeoutsInterruptWorkers() throws Exception {
        for (int index = 0; index < 5; index++) {
            BlockingAdapter adapter = new BlockingAdapter();
            ToolGateway.Call call = call(10 + index, "android.observe", 100);
            ToolExecutionCoordinator.Result result = new ToolExecutionCoordinator().execute(
                    authorize(newGateway(10 + index), call), call, adapter,
                    new ToolExecutionCoordinator.CancellationSignal());
            expect(ToolExecutionCoordinator.Status.TIMEOUT,
                    ToolExecutionCoordinator.ErrorCode.DEADLINE_EXCEEDED, 1, result);
            check(adapter.started.await(1, TimeUnit.SECONDS), "timeout adapter did not start");
            check(adapter.interrupted.await(1, TimeUnit.SECONDS), "timeout worker survived interrupt");
        }
    }

    private static void fiveCancellationPathsInterruptOrSkip() throws Exception {
        ToolGateway.Call preCancelledCall = call(20, "android.observe", 500);
        ToolExecutionCoordinator.CancellationSignal preCancelled =
                new ToolExecutionCoordinator.CancellationSignal();
        check(preCancelled.cancel(ToolExecutionCoordinator.CancellationSignal.Reason.USER),
                "pre-cancel failed");
        ToolExecutionCoordinator.Result pre = new ToolExecutionCoordinator().execute(
                authorize(newGateway(20), preCancelledCall), preCancelledCall,
                (ignored, signal) -> { throw new AssertionError("pre-cancel invoked adapter"); },
                preCancelled);
        expect(ToolExecutionCoordinator.Status.CANCELLED,
                ToolExecutionCoordinator.ErrorCode.USER_CANCELLED, 0, pre);

        cancelWhileRunning(21, ToolExecutionCoordinator.CancellationSignal.Reason.USER,
                ToolExecutionCoordinator.ErrorCode.USER_CANCELLED);
        cancelWhileRunning(22, ToolExecutionCoordinator.CancellationSignal.Reason.GLOBAL_STOP,
                ToolExecutionCoordinator.ErrorCode.GLOBAL_STOP);
        cancelWhileRunning(23, ToolExecutionCoordinator.CancellationSignal.Reason.CLIENT_DISCONNECTED,
                ToolExecutionCoordinator.ErrorCode.CLIENT_DISCONNECTED);

        BlockingAdapter adapter = new BlockingAdapter();
        ToolGateway.Call call = call(24, "android.observe", 1000);
        AtomicReference<ToolExecutionCoordinator.Result> result = new AtomicReference<>();
        Thread caller = new Thread(() -> result.set(new ToolExecutionCoordinator().execute(
                authorize(newGateway(24), call), call, adapter,
                new ToolExecutionCoordinator.CancellationSignal())), "caller-interrupt-test");
        caller.start();
        check(adapter.started.await(1, TimeUnit.SECONDS), "caller-interrupt adapter did not start");
        caller.interrupt();
        caller.join(2000);
        check(!caller.isAlive(), "caller-interrupt coordinator stuck");
        expect(ToolExecutionCoordinator.Status.CANCELLED,
                ToolExecutionCoordinator.ErrorCode.CALLER_INTERRUPTED, 1, result.get());
        check(adapter.interrupted.await(1, TimeUnit.SECONDS), "caller interrupt left worker alive");
    }

    private static void cancelWhileRunning(int index,
            ToolExecutionCoordinator.CancellationSignal.Reason reason,
            ToolExecutionCoordinator.ErrorCode expected) throws Exception {
        BlockingAdapter adapter = new BlockingAdapter();
        ToolGateway.Call call = call(index, "android.observe", 1000);
        ToolExecutionCoordinator.CancellationSignal signal =
                new ToolExecutionCoordinator.CancellationSignal();
        AtomicReference<ToolExecutionCoordinator.Result> result = new AtomicReference<>();
        Thread caller = new Thread(() -> result.set(new ToolExecutionCoordinator().execute(
                authorize(newGateway(index), call), call, adapter, signal)), "cancel-test-" + index);
        caller.start();
        check(adapter.started.await(1, TimeUnit.SECONDS), "cancel adapter did not start");
        check(signal.cancel(reason), "cancel not accepted");
        caller.join(2000);
        check(!caller.isAlive(), "cancel coordinator stuck");
        expect(ToolExecutionCoordinator.Status.CANCELLED, expected, 1, result.get());
        check(adapter.interrupted.await(1, TimeUnit.SECONDS), "cancel left worker alive");
        check(!signal.cancel(reason), "cancel was not idempotent");
    }

    private static void fiveStructuredAdapterFailures() {
        ToolExecutionCoordinator.Adapter[] adapters = {
            (call, signal) -> { throw new ToolExecutionCoordinator.AdapterFailure(
                    ToolExecutionCoordinator.ErrorCode.NETWORK_UNAVAILABLE); },
            (call, signal) -> { throw new ToolExecutionCoordinator.AdapterFailure(
                    ToolExecutionCoordinator.ErrorCode.PROCESS_EXIT_NONZERO); },
            (call, signal) -> { throw new IllegalStateException("private failure detail"); },
            (call, signal) -> new ToolExecutionCoordinator.AdapterResponse(
                    ToolExecutionCoordinator.AdapterStatus.ROLLBACK_REQUIRED,
                    Collections.singletonMap("rollback", "required")),
            (call, signal) -> new ToolExecutionCoordinator.AdapterResponse(
                    ToolExecutionCoordinator.AdapterStatus.SUCCESS, null)
        };
        ToolExecutionCoordinator.Status[] statuses = {
            ToolExecutionCoordinator.Status.FAILED, ToolExecutionCoordinator.Status.FAILED,
            ToolExecutionCoordinator.Status.FAILED, ToolExecutionCoordinator.Status.ROLLBACK_REQUIRED,
            ToolExecutionCoordinator.Status.FAILED
        };
        ToolExecutionCoordinator.ErrorCode[] errors = {
            ToolExecutionCoordinator.ErrorCode.NETWORK_UNAVAILABLE,
            ToolExecutionCoordinator.ErrorCode.PROCESS_EXIT_NONZERO,
            ToolExecutionCoordinator.ErrorCode.ADAPTER_FAILURE,
            ToolExecutionCoordinator.ErrorCode.ROLLBACK_REQUIRED,
            ToolExecutionCoordinator.ErrorCode.INVALID_OUTPUT
        };
        for (int index = 0; index < adapters.length; index++) {
            ToolGateway.Call call = call(30 + index, "android.observe", 500);
            ToolExecutionCoordinator.Result result = new ToolExecutionCoordinator().execute(
                    authorize(newGateway(30 + index), call), call, adapters[index],
                    new ToolExecutionCoordinator.CancellationSignal());
            expect(statuses[index], errors[index], 1, result);
        }
    }

    private static void fiveAuthorizationAndReplayDenials() {
        ToolGateway.Call call = call(40, "android.open_gallery", 500);
        ToolExecutionCoordinator coordinator = new ToolExecutionCoordinator();
        ToolExecutionCoordinator.CancellationSignal deniedSignal =
                new ToolExecutionCoordinator.CancellationSignal();
        expect(ToolExecutionCoordinator.Status.DENIED,
                ToolExecutionCoordinator.ErrorCode.AUTHORIZATION_DENIED, 0,
                coordinator.execute(null, call, successAdapter(), deniedSignal));
        check(!deniedSignal.cancel(ToolExecutionCoordinator.CancellationSignal.Reason.USER),
                "denied call accepted late cancellation");

        ToolGateway gateway = newGateway(41);
        ToolGateway.Result issuedOnly = issue(gateway, call(41, "android.open_gallery", 500));
        expect(ToolExecutionCoordinator.Status.DENIED,
                ToolExecutionCoordinator.ErrorCode.AUTHORIZATION_DENIED, 0,
                coordinator.execute(issuedOnly, call(41, "android.open_gallery", 500),
                        successAdapter(), null));

        ToolGateway.Call replayCall = call(42, "android.open_gallery", 500);
        ToolGateway.Result allowed = authorize(newGateway(42), replayCall);
        expect(ToolExecutionCoordinator.Status.SUCCESS, ToolExecutionCoordinator.ErrorCode.NONE, 1,
                coordinator.execute(allowed, replayCall, successAdapter(), null));
        expect(ToolExecutionCoordinator.Status.DENIED,
                ToolExecutionCoordinator.ErrorCode.AUTHORIZATION_REPLAY, 0,
                coordinator.execute(allowed, replayCall, successAdapter(), null));

        ToolGateway.Call original = call(43, "android.open_gallery", 500);
        ToolGateway.Result scoped = authorize(newGateway(43), original);
        Map<String, String> changedArguments = new HashMap<>();
        changedArguments.put("fixture", "changed");
        ToolGateway.Call changed = copy(original, original.requestId, original.planId,
                original.callId, changedArguments, original.idempotencyKey);
        expect(ToolExecutionCoordinator.Status.DENIED,
                ToolExecutionCoordinator.ErrorCode.SCOPE_CHANGED, 0,
                coordinator.execute(scoped, changed, successAdapter(), null));

        ToolGateway shared = newGateway(44);
        ToolGateway.Call first = call(44, "android.open_gallery", 500);
        authorize(shared, first);
        ToolGateway.Call duplicate = copy(first, "request-execution-other",
                "plan-execution-other", "call-execution-other",
                first.arguments, first.idempotencyKey);
        ToolGateway.Result duplicateIssued = issue(shared, duplicate);
        ToolGateway.Result duplicateDenied = shared.authorizeAndConsume(
                LOCAL, duplicate, duplicateIssued.token, NOW + 2);
        expect(ToolExecutionCoordinator.Status.DENIED,
                ToolExecutionCoordinator.ErrorCode.AUTHORIZATION_REPLAY, 0,
                coordinator.execute(duplicateDenied, duplicate, successAdapter(), null));
    }

    private static void resultMetadataIsPrivateAndImmutable() {
        ToolGateway.Call call = call(50, "android.observe", 500);
        ToolExecutionCoordinator.Result result = new ToolExecutionCoordinator().execute(
                authorize(newGateway(50), call), call, successAdapter(), null);
        boolean immutable = false;
        try { result.output.put("private", "bad"); }
        catch (UnsupportedOperationException expected) { immutable = true; }
        check(immutable && !result.publicLogSafe, "result privacy/immutability");
        for (java.lang.reflect.Field field : ToolExecutionCoordinator.Result.class.getDeclaredFields()) {
            String name = field.getName().toLowerCase();
            check(!name.contains("exception") && !name.contains("message")
                    && !name.contains("stack"), "result exposes private failure text");
        }
    }

    private static void malformedPostAuthorizationScopeFailsClosed() {
        ToolGateway.Call original = call(51, "android.observe", 500);
        ToolGateway.Call malformed = new ToolGateway.Call(original.taskId, original.requestId,
                original.planId, original.callId, original.tool, original.toolVersion,
                original.risk, original.audience, original.arguments, original.idempotencyKey,
                original.requestedAtElapsedMs, 99, original.publicLogSafe);
        expect(ToolExecutionCoordinator.Status.DENIED,
                ToolExecutionCoordinator.ErrorCode.SCOPE_CHANGED, 0,
                new ToolExecutionCoordinator().execute(authorize(newGateway(51), original),
                        malformed, successAdapter(), null));
    }

    private static ToolExecutionCoordinator.Adapter successAdapter() {
        return (call, signal) -> new ToolExecutionCoordinator.AdapterResponse(
                ToolExecutionCoordinator.AdapterStatus.SUCCESS,
                Collections.singletonMap("fixture", "ok"));
    }

    private static ToolGateway newGateway(final int seed) {
        return new ToolGateway(new ToolGateway.TokenIdSource() {
            int next = seed;
            @Override public String nextId() { return String.format("cap-%032x", next++); }
        });
    }

    private static ToolGateway.Result issue(ToolGateway gateway, ToolGateway.Call call) {
        FreshConfirmationGate gate = new FreshConfirmationGate();
        FreshConfirmationGate.Scope scope = new FreshConfirmationGate.Scope(
                call.taskId, call.requestId, call.planId, "target-execution", "content-execution");
        check(gate.issue(FreshConfirmationGate.Source.LOCAL_USER_GESTURE,
                "confirmation-execution-0001", scope, NOW, 30000L, ready()).code
                == FreshConfirmationGate.Code.ISSUED, "confirmation issue");
        FreshConfirmationGate.Result confirmation = gate.authorizeAndConsume(
                scope, NOW + 1, ready());
        ToolGateway.Result issued = gateway.issue(LOCAL, confirmation, call, NOW + 1, 10000L);
        check(issued.decision == ToolGateway.Decision.ISSUED, "token issue " + issued.decision);
        return issued;
    }

    private static ToolGateway.Result authorize(ToolGateway gateway, ToolGateway.Call call) {
        ToolGateway.Result issued = issue(gateway, call);
        ToolGateway.Result allowed = gateway.authorizeAndConsume(LOCAL, call, issued.token, NOW + 2);
        check(allowed.decision == ToolGateway.Decision.ALLOW, "token authorize " + allowed.decision);
        return allowed;
    }

    private static FreshConfirmationGate.DeviceState ready() {
        return new FreshConfirmationGate.DeviceState(true, true, true);
    }

    private static ToolGateway.Call call(int index, String tool, int timeoutMs) {
        ToolCatalog.Descriptor descriptor = ToolCatalog.lookup(tool, 1);
        return new ToolGateway.Call(String.format("job-execution-%04d", index),
                String.format("request-execution-%04d", index),
                String.format("plan-execution-%04d", index),
                String.format("call-execution-%04d", index), tool, 1, descriptor.risk,
                descriptor.audience, Collections.<String, String>emptyMap(),
                String.format("idempotency-execution-%04d", index), NOW - 1,
                timeoutMs, false);
    }

    private static ToolGateway.Call copy(ToolGateway.Call original, String requestId,
            String planId, String callId, Map<String, String> arguments, String idempotencyKey) {
        return new ToolGateway.Call(original.taskId, requestId, planId, callId,
                original.tool, original.toolVersion, original.risk, original.audience,
                arguments, idempotencyKey, original.requestedAtElapsedMs,
                original.timeoutMs, original.publicLogSafe);
    }

    private static void expect(ToolExecutionCoordinator.Status status,
            ToolExecutionCoordinator.ErrorCode error, int adapterCalls,
            ToolExecutionCoordinator.Result result) {
        check(result != null && result.status == status && result.errorCode == error,
                "expected=" + status + "/" + error + " actual="
                        + (result == null ? "null" : result.status + "/" + result.errorCode));
        check(result.adapterCalls == adapterCalls, "adapter call count");
    }

    private static final class BlockingAdapter implements ToolExecutionCoordinator.Adapter {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch interrupted = new CountDownLatch(1);

        @Override public ToolExecutionCoordinator.AdapterResponse execute(ToolGateway.Call call,
                ToolExecutionCoordinator.CancellationSignal cancellation)
                throws ToolExecutionCoordinator.AdapterFailure {
            started.countDown();
            try {
                new CountDownLatch(1).await();
                throw new AssertionError("blocking adapter escaped without interrupt");
            } catch (InterruptedException expected) {
                interrupted.countDown();
                throw new ToolExecutionCoordinator.AdapterFailure(
                        ToolExecutionCoordinator.ErrorCode.ADAPTER_FAILURE);
            }
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
