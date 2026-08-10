package io.github.toolazytoname.xiaohei;

public final class GlobalStopRegistryTest {
    public static void main(String[] args) {
        stopsEveryRegisteredOwnerOnce();
        reportsFailureWithoutFalseAllReleased();
        rejectsLateRegistrationAndRepeatedStop();
        System.out.println("PASS global-stop registry=8 stop_once=8 failure_visible=1 repeat=deny discovery=0 execution=owners_only");
    }

    private static void stopsEveryRegisteredOwnerOnce() {
        GlobalStopRegistry registry = new GlobalStopRegistry();
        CountingOwner owner = new CountingOwner(true);
        for (GlobalStopRegistry.Resource resource : GlobalStopRegistry.Resource.values())
            require(registry.register(resource, owner), "registered " + resource);
        GlobalStopRegistry.Result result = registry.stopAll();
        require(result.code == GlobalStopRegistry.Code.STOPPED && result.requested == 8 && result.stopped == 8
                && result.failed == 0 && result.allResourcesReleased && owner.calls == 8, "all owners stopped");
    }

    private static void reportsFailureWithoutFalseAllReleased() {
        GlobalStopRegistry registry = new GlobalStopRegistry();
        require(registry.register(GlobalStopRegistry.Resource.VOICE, new CountingOwner(true)), "voice");
        require(registry.register(GlobalStopRegistry.Resource.PHONE_AGENT, new CountingOwner(false)), "agent");
        GlobalStopRegistry.Result result = registry.stopAll();
        require(result.code == GlobalStopRegistry.Code.STOP_FAILED && result.failed == 1
                && !result.allResourcesReleased && registry.state() == GlobalStopRegistry.State.FAILED,
                "failure remains visible");
    }

    private static void rejectsLateRegistrationAndRepeatedStop() {
        GlobalStopRegistry registry = new GlobalStopRegistry();
        require(registry.stopAll().code == GlobalStopRegistry.Code.NOTHING_ACTIVE, "empty stop");
        require(!registry.register(GlobalStopRegistry.Resource.DSP, new CountingOwner(true)), "late registration rejected");
        require(registry.stopAll().code == GlobalStopRegistry.Code.ALREADY_TERMINAL, "repeat denied");
    }

    private static final class CountingOwner implements GlobalStopRegistry.Owner {
        final boolean answer;
        int calls;
        CountingOwner(boolean answer) { this.answer = answer; }
        @Override public boolean stop() { calls++; return answer; }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
