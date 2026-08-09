package io.github.toolazytoname.xiaohei;

public final class AgentPolicyTest {
    public static void main(String[] args) {
        expect(AgentPolicy.Decision.ALLOW, "com.android.settings", "网络和互联网", "网络和互联网");
        expect(AgentPolicy.Decision.DENY, "com.eg.android.AlipayGphone", "首页", "扫一扫");
        expect(AgentPolicy.Decision.DENY, "com.android.settings", "输入验证码", "下一步");
        expect(AgentPolicy.Decision.REQUIRE_CONFIRMATION, "com.android.settings", "应用信息", "卸载");
        expect(AgentPolicy.Decision.REQUIRE_CONFIRMATION, "com.android.contacts",
            "要允许“通讯录”向您发送通知吗？", "允许");
        expect(AgentPolicy.Decision.REQUIRE_CONFIRMATION, "com.android.settings",
            "网络和互联网", "下一步");
        expect(AgentPolicy.Decision.DENY, "com.android.settings", "付款金额", "确定");
        if (AgentPolicy.packageAllowed("com.example.unknown"))
            throw new AssertionError("unknown package must not be allowlisted");
        if (!AgentPolicy.packageAllowed("com.android.calculator2"))
            throw new AssertionError("calculator must be explicitly allowlisted");
        System.out.println("PASS agent-policy allow=1 deny=3 confirm=3 package-allow=1 package-deny=1");
    }

    private static void expect(AgentPolicy.Decision expected, String pkg, String text, String label) {
        AgentPolicy.Decision actual = AgentPolicy.assess(pkg, text, label);
        if (actual != expected) throw new AssertionError("expected=" + expected + " actual=" + actual);
    }
}
