package io.github.toolazytoname.xiaohei;

public final class AgentPolicyTest {
    public static void main(String[] args) {
        expect(AgentPolicy.Decision.ALLOW, "com.android.settings", "网络和互联网", "网络和互联网");
        expect(AgentPolicy.Decision.ALLOW, "com.android.settings",
            "Privacy Permissions, account activity, personal data", "Network & internet");
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
        String[] aospMatrix = { "com.android.calendar", "com.android.camera2",
            "com.android.gallery3d", "com.android.messaging", "org.chromium.webview_shell" };
        for (String pkg : aospMatrix) if (!AgentPolicy.packageAllowed(pkg))
            throw new AssertionError("reviewed AOSP matrix package must be allowlisted: " + pkg);
        if (AgentPolicy.packageAllowed("com.google.android.unreviewed"))
            throw new AssertionError("package prefixes must not grant access");
        if (AgentPolicy.packageAllowed("org.lineageos.unreviewed"))
            throw new AssertionError("package prefixes must not grant access");
        System.out.println("PASS agent-policy allow=2 deny=3 confirm=3 package-allow=6 package-deny=3");
    }

    private static void expect(AgentPolicy.Decision expected, String pkg, String text, String label) {
        AgentPolicy.Decision actual = AgentPolicy.assess(pkg, text, label);
        if (actual != expected) throw new AssertionError("expected=" + expected + " actual=" + actual);
    }
}
