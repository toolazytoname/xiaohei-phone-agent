package io.github.toolazytoname.xiaohei;

import java.util.Locale;

/** Permanent fail-closed corpus for money, authentication secrets, and security-control evasion. */
final class SensitiveActionDenialPolicy {
    enum Decision { DENY_SENSITIVE, NOT_MATCHED }
    private static final String[] TERMS = {
        "支付", "付款", "转账", "提现吗", "提现", "银行卡", "信用卡", "付款码", "收款码", "红包",
        "payment", "pay now", "transfer", "wire transfer", "bank account", "credit card", "cash out", "bank", "wallet",
        "验证码", "动态码", "短信码", "一次性密码", "密码", "口令", "交易密码", "cvv", "otp", "2fa", "passcode", "verification code", "one-time code", "password",
        "绕过", "跳过验证", "规避风控", "关闭安全", "关闭保护", "破解", "伪造验证码", "绕过安全检测", "bypass", "skip verification", "evade risk", "disable security", "disable protection", "forge otp", "avoid detection"
    };
    static Decision assess(String packageName, String visibleText, String requestedLabel) {
        return contains(packageName) || contains(visibleText) || contains(requestedLabel) ? Decision.DENY_SENSITIVE : Decision.NOT_MATCHED;
    }
    private static boolean contains(String value) {
        String normalized = value == null ? "" : value.toLowerCase(Locale.ROOT);
        for (String term : TERMS) if (normalized.contains(term)) return true;
        return false;
    }
}
