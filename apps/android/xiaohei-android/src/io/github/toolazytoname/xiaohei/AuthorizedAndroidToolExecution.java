package io.github.toolazytoname.xiaohei;

import android.content.Context;

/** Single authorized path from a consumed gateway permit to a closed Android adapter registry. */
final class AuthorizedAndroidToolExecution {
    interface Resolver { ToolExecutionCoordinator.Adapter resolve(ToolGateway.Call call); }
    private final ToolExecutionCoordinator coordinator;
    private final Resolver resolver;

    AuthorizedAndroidToolExecution(Context context) {
        this(new ToolExecutionCoordinator(), new AndroidToolAdapterRegistry(context)::resolve);
    }

    AuthorizedAndroidToolExecution(ToolExecutionCoordinator coordinator, Resolver resolver) {
        if (coordinator == null || resolver == null) throw new IllegalArgumentException("runtime required");
        this.coordinator = coordinator; this.resolver = resolver;
    }

    ToolExecutionCoordinator.Result execute(ToolGateway.Result authorization, ToolGateway.Call call,
            ToolExecutionCoordinator.CancellationSignal cancellation) {
        return coordinator.execute(authorization, call, resolver.resolve(call), cancellation);
    }
}
