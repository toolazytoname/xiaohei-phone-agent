package io.github.toolazytoname.xiaohei;

public final class ConversationPrivacyPolicyTest {
    public static void main(String[] args) {
        deny("微信有没有未读消息", ConversationPrivacyPolicy.Category.NOTIFICATIONS);
        deny("读取我的联系人", ConversationPrivacyPolicy.Category.CONTACTS);
        deny("我现在在哪里", ConversationPrivacyPolicy.Category.LOCATION);
        deny("把相册内容发给模型", ConversationPrivacyPolicy.Category.PRIVATE_MEDIA);
        deny("告诉我验证码", ConversationPrivacyPolicy.Category.CREDENTIALS);

        String[] aliases = {"read my unread messages", "read my contacts", "where am i right now",
            "read my photos", "show me my verification code"};
        for (String alias : aliases) check(ConversationPrivacyPolicy.evaluate(alias).denied, "alias not denied");

        String[] allowed = {"如何保护隐私", "什么是联系人", "位置权限是什么", "如何整理相册", "验证码是什么",
            "微信有没有未读消息然后打开微信", "读取我的联系人并发送", ""};
        for (String value : allowed)
            check(!ConversationPrivacyPolicy.evaluate(value).denied, "non-exact text denied: " + value);
        System.out.println("PASS ConversationPrivacyPolicyTest categories=5 canonical=5 aliases=5 non_exact=8 model_calls=0 action_calls=0");
    }

    private static void deny(String input, ConversationPrivacyPolicy.Category category) {
        ConversationPrivacyPolicy.Result result = ConversationPrivacyPolicy.evaluate(input);
        check(result.denied && result.category == category, "wrong privacy category");
        check(result.text.contains("本地隐私拒绝") && result.text.contains("ZERO MODEL CALLS"), "missing label");
        check(result.modelCalls == 0 && result.actionCalls == 0, "privacy denial escaped authority");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
