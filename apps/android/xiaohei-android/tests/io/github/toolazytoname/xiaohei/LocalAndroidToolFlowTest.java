package io.github.toolazytoname.xiaohei;

/** Ensures the visible query entry can mint only one local, confirmed query authority. */
public final class LocalAndroidToolFlowTest {
    private static final class Clock implements LocalAndroidToolFlow.Clock { long now = 100000L; public long elapsedRealtime(){ return now++; } }
    private static final class Ids implements LocalAndroidToolFlow.IdSource { int n; public String next(){ return "00000000-0000-0000-0000-" + String.format("%012d", ++n); } }
    private static final FreshConfirmationGate.DeviceState READY = new FreshConfirmationGate.DeviceState(true, true, true);
    public static void main(String[] args) {
        Clock clock = new Clock();
        LocalAndroidToolFlow flow = new LocalAndroidToolFlow(new ToolGateway(), new FreshConfirmationGate(), clock, new Ids());
        LocalAndroidToolFlow.Request request = flow.prepareQuery();
        check("android.media_test_collection".equals(request.call.tool), "closed tool");
        check(request.call.arguments.size() == 1 && "query".equals(request.call.arguments.get("operation")), "query only");
        ToolGateway.Result allowed = flow.confirmAndAuthorize(request, READY, 12345);
        check(allowed != null && allowed.decision == ToolGateway.Decision.ALLOW, "allow once");
        check(allowed.takeExecutionPermit() != null && allowed.takeExecutionPermit() == null, "permit one use");
        check(flow.confirmAndAuthorize(null, READY, 12345) == null, "missing request");
        flow.cancel();
        System.out.println("PASS LocalAndroidToolFlowTest local_gesture=1 query_only=1 permit_once=1 cancel=1 execution=0");
    }
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
