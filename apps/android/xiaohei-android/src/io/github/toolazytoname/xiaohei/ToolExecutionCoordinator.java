package io.github.toolazytoname.xiaohei;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Executes one already-authorized adapter call with bounded cancellation and structured output. */
final class ToolExecutionCoordinator {
    static final int SCHEMA_VERSION = 1;
    static final int MAX_OUTPUT_FIELDS = 32;
    static final int MAX_OUTPUT_VALUE_LENGTH = 1024;
    static final long WORKER_STOP_GRACE_MS = 100L;

    enum Status { SUCCESS, DENIED, CANCELLED, TIMEOUT, FAILED, ROLLBACK_REQUIRED }

    enum ErrorCode {
        NONE,
        AUTHORIZATION_DENIED,
        AUTHORIZATION_REPLAY,
        AUTHORIZATION_EXPIRED,
        SCOPE_CHANGED,
        ADAPTER_MISSING,
        INVALID_OUTPUT,
        USER_CANCELLED,
        GLOBAL_STOP,
        CLIENT_DISCONNECTED,
        CALLER_INTERRUPTED,
        DEADLINE_EXCEEDED,
        NETWORK_UNAVAILABLE,
        PROCESS_EXIT_NONZERO,
        ADAPTER_FAILURE,
        ROLLBACK_REQUIRED
    }

    enum AdapterStatus { SUCCESS, ROLLBACK_REQUIRED }

    interface Adapter {
        AdapterResponse execute(ToolGateway.Call call, CancellationSignal cancellation)
                throws AdapterFailure;
    }

    interface Clock {
        long elapsedRealtimeMs();
    }

    interface ExecutorFactory {
        ExecutorService create();
    }

    static final class AdapterResponse {
        final AdapterStatus status;
        final Map<String, String> output;

        AdapterResponse(AdapterStatus status, Map<String, String> output) {
            this.status = status;
            this.output = output == null ? null
                    : Collections.unmodifiableMap(new HashMap<>(output));
        }
    }

    static final class AdapterFailure extends Exception {
        final ErrorCode code;

        AdapterFailure(ErrorCode code) {
            super(String.valueOf(code));
            this.code = code;
        }
    }

    static final class CancellationSignal {
        enum Reason { USER, GLOBAL_STOP, CLIENT_DISCONNECTED }

        private boolean cancelled;
        private boolean terminal;
        private Reason reason;
        private Future<?> attached;

        synchronized boolean cancel(Reason reason) {
            if (cancelled || terminal || reason == null) return false;
            cancelled = true;
            this.reason = reason;
            if (attached != null) attached.cancel(true);
            return true;
        }

        synchronized boolean isCancelled() {
            return cancelled;
        }

        synchronized Reason reason() {
            return reason;
        }

        synchronized boolean isTerminal() {
            return terminal;
        }

        synchronized void attach(Future<?> future) {
            attached = future;
            if (cancelled && future != null) future.cancel(true);
        }

        synchronized Reason finish(Future<?> future) {
            if (attached == future) attached = null;
            terminal = true;
            return reason;
        }
    }

    static final class Result {
        final int schemaVersion;
        final String taskId;
        final String callId;
        final String tool;
        final Status status;
        final ErrorCode errorCode;
        final Map<String, String> output;
        final long startedAtElapsedMs;
        final long finishedAtElapsedMs;
        final long durationMs;
        final int adapterCalls;
        final boolean publicLogSafe;

        private Result(ToolGateway.Call call, Status status, ErrorCode errorCode,
                Map<String, String> output, long startedAtElapsedMs,
                long finishedAtElapsedMs, int adapterCalls) {
            this.schemaVersion = SCHEMA_VERSION;
            this.taskId = call.taskId;
            this.callId = call.callId;
            this.tool = call.tool;
            this.status = status;
            this.errorCode = errorCode;
            this.output = output == null ? Collections.<String, String>emptyMap()
                    : Collections.unmodifiableMap(new HashMap<>(output));
            this.startedAtElapsedMs = startedAtElapsedMs;
            this.finishedAtElapsedMs = Math.max(startedAtElapsedMs, finishedAtElapsedMs);
            this.durationMs = this.finishedAtElapsedMs - startedAtElapsedMs;
            this.adapterCalls = adapterCalls;
            this.publicLogSafe = false;
        }
    }

    private static final class MonotonicClock implements Clock {
        @Override public long elapsedRealtimeMs() {
            return System.nanoTime() / 1000000L;
        }
    }

    private static final class DaemonExecutorFactory implements ExecutorFactory {
        @Override public ExecutorService create() {
            return Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override public Thread newThread(Runnable runnable) {
                    Thread thread = new Thread(runnable, "xiaohei-tool-adapter");
                    thread.setDaemon(true);
                    return thread;
                }
            });
        }
    }

    private final Clock clock;
    private final ExecutorFactory executors;

    ToolExecutionCoordinator() {
        this(new MonotonicClock(), new DaemonExecutorFactory());
    }

    ToolExecutionCoordinator(Clock clock, ExecutorFactory executors) {
        if (clock == null || executors == null) throw new IllegalArgumentException("runtime required");
        this.clock = clock;
        this.executors = executors;
    }

    Result execute(ToolGateway.Result authorization, ToolGateway.Call call,
            Adapter adapter, CancellationSignal cancellation) {
        long started = clock.elapsedRealtimeMs();
        if (call == null) throw new IllegalArgumentException("validated call required");
        CancellationSignal signal = cancellation == null ? new CancellationSignal() : cancellation;
        ToolGateway.ExecutionPermit permit = authorization == null
                ? null : authorization.takeExecutionPermit();
        if (permit == null) {
            signal.finish(null);
            return result(call, Status.DENIED,
                    authorizationError(authorization), null, started, 0);
        }
        if (!permit.matches(call)) {
            signal.finish(null);
            return result(call, Status.DENIED, ErrorCode.SCOPE_CHANGED, null, started, 0);
        }
        if (adapter == null) {
            signal.finish(null);
            return result(call, Status.FAILED, ErrorCode.ADAPTER_MISSING, null, started, 0);
        }
        if (signal.isCancelled() || signal.isTerminal()) {
            signal.finish(null);
            return result(call, Status.CANCELLED,
                    cancellationError(signal.reason()), null, started, 0);
        }

        ExecutorService executor = executors.create();
        Future<AdapterResponse> future = executor.submit(
                () -> adapter.execute(call, signal));
        signal.attach(future);
        try {
            AdapterResponse response = future.get(call.timeoutMs, TimeUnit.MILLISECONDS);
            CancellationSignal.Reason completedAfterCancellation = signal.finish(future);
            if (completedAfterCancellation != null) return result(call, Status.CANCELLED,
                    cancellationError(completedAfterCancellation), null, started, 1);
            if (!validResponse(response)) return result(call, Status.FAILED,
                    ErrorCode.INVALID_OUTPUT, null, started, 1);
            if (response.status == AdapterStatus.ROLLBACK_REQUIRED)
                return result(call, Status.ROLLBACK_REQUIRED, ErrorCode.ROLLBACK_REQUIRED,
                        response.output, started, 1);
            return result(call, Status.SUCCESS, ErrorCode.NONE, response.output, started, 1);
        } catch (CancellationException cancelled) {
            signal.finish(future);
            return result(call, Status.CANCELLED, cancellationError(signal.reason()),
                    null, started, 1);
        } catch (TimeoutException timeout) {
            CancellationSignal.Reason timedOutAfterCancellation = signal.finish(future);
            future.cancel(true);
            if (timedOutAfterCancellation != null) return result(call, Status.CANCELLED,
                    cancellationError(timedOutAfterCancellation), null, started, 1);
            return result(call, Status.TIMEOUT, ErrorCode.DEADLINE_EXCEEDED,
                    null, started, 1);
        } catch (InterruptedException interrupted) {
            signal.finish(future);
            future.cancel(true);
            Thread.currentThread().interrupt();
            return result(call, Status.CANCELLED, ErrorCode.CALLER_INTERRUPTED,
                    null, started, 1);
        } catch (ExecutionException failed) {
            CancellationSignal.Reason failedAfterCancellation = signal.finish(future);
            if (failedAfterCancellation != null) return result(call, Status.CANCELLED,
                    cancellationError(failedAfterCancellation), null, started, 1);
            Throwable cause = failed.getCause();
            ErrorCode code = cause instanceof AdapterFailure
                    ? safeAdapterError(((AdapterFailure) cause).code) : ErrorCode.ADAPTER_FAILURE;
            return result(call, Status.FAILED, code, null, started, 1);
        } finally {
            signal.finish(future);
            executor.shutdownNow();
            try {
                executor.awaitTermination(WORKER_STOP_GRACE_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private Result result(ToolGateway.Call call, Status status, ErrorCode errorCode,
            Map<String, String> output, long started, int adapterCalls) {
        return new Result(call, status, errorCode, output, started,
                clock.elapsedRealtimeMs(), adapterCalls);
    }

    private static ErrorCode authorizationError(ToolGateway.Result authorization) {
        if (authorization == null) return ErrorCode.AUTHORIZATION_DENIED;
        if (authorization.decision == ToolGateway.Decision.ALLOW
                || authorization.decision == ToolGateway.Decision.TOKEN_REPLAY
                || authorization.decision == ToolGateway.Decision.IDEMPOTENCY_REPLAY
                || authorization.decision == ToolGateway.Decision.CONFIRMATION_REPLAY)
            return ErrorCode.AUTHORIZATION_REPLAY;
        if (authorization.decision == ToolGateway.Decision.TOKEN_EXPIRED
                || authorization.decision == ToolGateway.Decision.CLOCK_ROLLBACK)
            return ErrorCode.AUTHORIZATION_EXPIRED;
        return ErrorCode.AUTHORIZATION_DENIED;
    }

    private static ErrorCode cancellationError(CancellationSignal.Reason reason) {
        if (reason == CancellationSignal.Reason.GLOBAL_STOP) return ErrorCode.GLOBAL_STOP;
        if (reason == CancellationSignal.Reason.CLIENT_DISCONNECTED)
            return ErrorCode.CLIENT_DISCONNECTED;
        return ErrorCode.USER_CANCELLED;
    }

    private static ErrorCode safeAdapterError(ErrorCode code) {
        if (code == ErrorCode.NETWORK_UNAVAILABLE || code == ErrorCode.PROCESS_EXIT_NONZERO)
            return code;
        return ErrorCode.ADAPTER_FAILURE;
    }

    private static boolean validResponse(AdapterResponse response) {
        if (response == null || response.status == null || response.output == null
                || response.output.size() > MAX_OUTPUT_FIELDS) return false;
        for (Map.Entry<String, String> entry : response.output.entrySet()) {
            if (entry.getKey() == null || !entry.getKey().matches("[a-z][a-z0-9_]{0,63}")
                    || entry.getValue() == null
                    || entry.getValue().length() > MAX_OUTPUT_VALUE_LENGTH) return false;
        }
        return true;
    }
}
