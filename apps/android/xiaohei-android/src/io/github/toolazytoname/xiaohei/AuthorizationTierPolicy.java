package io.github.toolazytoname.xiaohei;

/** Prevents Android/OpenCode/root authority tokens from crossing audience tiers. */
final class AuthorizationTierPolicy {
    enum Tier { ANDROID, OPENCODE, ROOT }
    enum Decision { ALLOW, DENY_MISSING, DENY_CROSS_TIER, DENY_ROOT_UNIMPLEMENTED }
    static final class Result { final Decision decision; final int modelCalls=0; final int executionCalls=0; Result(Decision d){decision=d;} }
    private AuthorizationTierPolicy() {}
    static Result authorize(Tier credential, ToolCatalog.Audience target) {
        if (credential==null || target==null) return new Result(Decision.DENY_MISSING);
        if (target==ToolCatalog.Audience.ROOT_BROKER) return new Result(Decision.DENY_ROOT_UNIMPLEMENTED);
        if ((credential==Tier.ANDROID && target==ToolCatalog.Audience.ANDROID_GATEWAY)
                || (credential==Tier.OPENCODE && target==ToolCatalog.Audience.OPENCODE_GATEWAY)) return new Result(Decision.ALLOW);
        return new Result(Decision.DENY_CROSS_TIER);
    }
}
