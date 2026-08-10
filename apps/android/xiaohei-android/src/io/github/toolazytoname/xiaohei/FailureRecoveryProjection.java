package io.github.toolazytoname.xiaohei;

/** Public, typed failure explanation. Never accepts exception text, paths, prompts, or credentials. */
final class FailureRecoveryProjection {
    enum Kind { POLICY_DENIED, TIMEOUT, CANCELLED, ADAPTER_UNAVAILABLE, UNKNOWN }
    static String visibleText(Kind kind) {
        if (kind == null) kind = Kind.UNKNOWN;
        switch (kind) {
            case POLICY_DENIED: return "失败原因 / Cause：本地安全策略拒绝\n影响 / Impact：没有执行动作\n恢复 / Recovery：人工缩小为一个低风险目标后重新确认";
            case TIMEOUT: return "失败原因 / Cause：已达到受审核时间预算\n影响 / Impact：任务已停止\n恢复 / Recovery：人工检查状态后创建新的审核任务";
            case CANCELLED: return "失败原因 / Cause：任务被停止\n影响 / Impact：没有继续或自动重试\n恢复 / Recovery：人工接管，必要时创建新的审核任务";
            case ADAPTER_UNAVAILABLE: return "失败原因 / Cause：所需适配器未连接\n影响 / Impact：没有执行动作\n恢复 / Recovery：在权限中心检查状态后人工处理";
            default: return "失败原因 / Cause：未分类的安全失败\n影响 / Impact：没有执行或重试\n恢复 / Recovery：查看任务卡后人工接管";
        }
    }
}
