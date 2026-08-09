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
                    "cvv", "otp", "verification code", "passcode", "payment", "transfer")
                || containsAny(surface, "[redacted]", "输入验证码", "输入密码", "确认支付",
                    "付款金额", "转账金额", "信用卡", "银行卡", "enter password",
                    "verification code", "payment", "transfer"))
            return Decision.DENY;
        if (containsAny(target, "发送", "删除", "卸载", "安装", "授权", "允许",
                "拨打", "确认", "确定", "下一步", "继续", "call", "send", "delete",
                "install", "allow", "confirm", "continue", "next"))
            return Decision.REQUIRE_CONFIRMATION;
        // Generic approval controls are ambiguous across Android permission dialogs,
        // account flows, and destructive flows. Never click them from an Agent task.
        if (containsAny(target, "ok", "accept", "agree", "同意", "接受")
                || containsAny(surface, "登录", "sign in"))
            return Decision.REQUIRE_CONFIRMATION;
        return Decision.ALLOW;
    }

    static boolean packageAllowed(String packageName) {
        String pkg = lower(packageName);
        return pkg.equals("com.android.settings")
            || pkg.equals("com.android.calculator2")
            || pkg.equals("com.android.contacts")
            || pkg.equals("com.android.deskclock")
            || pkg.equals("com.android.dialer")
            || pkg.equals("com.android.documentsui")
            || pkg.equals("com.android.chrome")
            || pkg.equals("com.android.calendar")
            || pkg.equals("com.android.camera2")
            || pkg.equals("com.android.gallery3d")
            || pkg.equals("com.android.messaging")
            || pkg.equals("org.chromium.webview_shell")
            || pkg.equals("org.mozilla.fennec_fdroid")
            || pkg.equals("com.google.android.apps.googlecamera.fishfood")
            || pkg.equals("org.lineageos.aperture")
            || pkg.equals("org.lineageos.glimpse")
            || pkg.equals("org.lineageos.jelly")
            || pkg.equals("org.lineageos.etar")
            || pkg.equals("org.lineageos.recorder")
            || pkg.equals("org.lineageos.eleven")
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
