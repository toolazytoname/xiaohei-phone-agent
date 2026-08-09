package io.github.toolazytoname.xiaohei;

import java.util.Locale;

/** Fail-closed policy applied before every semantic UI action. */
final class AgentPolicy {
    enum Decision { ALLOW, REQUIRE_CONFIRMATION, DENY }

    static Decision assess(String packageName, String visibleText, String requestedLabel) {
        String pkg = lower(packageName);
        String surface = lower(visibleText);
        String target = lower(requestedLabel);
        if (containsAny(pkg, "alipay", "bank", "wallet", "finance", "pay")
                || containsAny(target, "付款", "支付", "转账", "银行卡", "验证码", "密码",
                    "cvv", "otp", "verification code", "passcode")
                || containsAny(surface, "[redacted]", "输入验证码", "输入密码", "确认支付",
                    "付款金额", "转账金额", "enter password", "verification code"))
            return Decision.DENY;
        if (containsAny(target, "发送", "删除", "卸载", "安装", "授权", "允许",
                "拨打", "call", "send", "delete", "install", "allow"))
            return Decision.REQUIRE_CONFIRMATION;
        return Decision.ALLOW;
    }

    static boolean packageAllowed(String packageName) {
        String pkg = lower(packageName);
        return pkg.startsWith("com.android.settings")
            || pkg.startsWith("com.android.launcher")
            || pkg.startsWith("org.lineageos")
            || pkg.startsWith("com.google.android")
            || pkg.startsWith("com.android.chrome")
            || pkg.startsWith("org.mozilla")
            || pkg.equals("io.github.toolazytoname.xiaohei");
    }

    private static String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private static boolean containsAny(String value, String... needles) {
        for (String needle : needles) if (value.contains(needle)) return true;
        return false;
    }
}
