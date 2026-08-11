package io.github.toolazytoname.xiaohei;

import java.util.Collections;

/** Creates one local, user-confirmed, read-only test-collection call; it never executes an adapter. */
final class LocalAndroidToolFlow {
    interface Clock { long elapsedRealtime(); }
    interface IdSource { String next(); }

    static final class Request {
        final ToolGateway.Call call;
        final FreshConfirmationGate.Scope scope;

        Request(ToolGateway.Call call, FreshConfirmationGate.Scope scope) {
            this.call = call;
            this.scope = scope;
        }
    }

    private static final String TOOL = "android.media_test_collection";
    private static final String TARGET = "Pictures/XiaoheiTest/";
    private static final String CONTENT = "查询文件数量；不读取内容、不修改文件";
    private final ToolGateway gateway;
    private final FreshConfirmationGate confirmation;
    private final Clock clock;
    private final IdSource ids;

    LocalAndroidToolFlow(ToolGateway gateway, FreshConfirmationGate confirmation, Clock clock,
            IdSource ids) {
        if (gateway == null || confirmation == null || clock == null || ids == null)
            throw new IllegalArgumentException("runtime required");
        this.gateway = gateway;
        this.confirmation = confirmation;
        this.clock = clock;
        this.ids = ids;
    }

    Request prepareQuery() {
        long now = clock.elapsedRealtime();
        String task = id("task");
        String request = id("request");
        String plan = id("plan");
        ToolGateway.Call call = new ToolGateway.Call(task, request, plan, id("call"), TOOL,
                ToolCatalog.TOOL_VERSION, ToolCatalog.Risk.REVERSIBLE,
                ToolCatalog.Audience.ANDROID_GATEWAY,
                Collections.singletonMap("operation", "query"), id("idempotency"), now, 10000,
                false);
        return new Request(call, new FreshConfirmationGate.Scope(task, request, plan, TARGET, CONTENT));
    }

    ToolGateway.Result confirmAndAuthorize(Request request,
            FreshConfirmationGate.DeviceState device, int uid) {
        if (request == null || uid < 0) return null;
        long now = clock.elapsedRealtime();
        FreshConfirmationGate.Result issued = confirmation.issue(
                FreshConfirmationGate.Source.LOCAL_USER_GESTURE, id("confirmation"), request.scope,
                now, 30000L, device);
        if (issued.code != FreshConfirmationGate.Code.ISSUED) return null;
        FreshConfirmationGate.Result allowed = confirmation.authorizeAndConsume(
                request.scope, clock.elapsedRealtime(), device);
        ToolGateway.Result token = gateway.issue(new ToolGateway.Peer("127.0.0.1", "127.0.0.1", uid, uid),
                allowed, request.call, clock.elapsedRealtime(), 10000L);
        if (token.decision != ToolGateway.Decision.ISSUED) return token;
        return gateway.authorizeAndConsume(new ToolGateway.Peer("127.0.0.1", "127.0.0.1", uid, uid),
                request.call, token.token, clock.elapsedRealtime());
    }

    void cancel() { confirmation.cancel(); gateway.revokeAll(); }

    private String id(String prefix) { return prefix + "-" + ids.next(); }
}
