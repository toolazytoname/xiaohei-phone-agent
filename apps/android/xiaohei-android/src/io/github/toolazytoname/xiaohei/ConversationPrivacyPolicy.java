package io.github.toolazytoname.xiaohei;

import java.util.Locale;

/** Exact local denials for current private device data in zero-authority Conversation. */
final class ConversationPrivacyPolicy {
    enum Category { NONE, NOTIFICATIONS, CONTACTS, LOCATION, PRIVATE_MEDIA, CREDENTIALS }

    static final class Result {
        final Category category;
        final boolean denied;
        final String text;
        final int modelCalls;
        final int actionCalls;

        private Result(Category category, String text) {
            this.category = category;
            this.denied = category != Category.NONE;
            this.text = text;
            this.modelCalls = 0;
            this.actionCalls = 0;
        }
    }

    private ConversationPrivacyPolicy() {}

    static Result evaluate(String input) {
        String request = normalize(input);
        switch (request) {
            case "微信有没有未读消息":
            case "读取我的未读消息":
            case "read my unread messages":
                return deny(Category.NOTIFICATIONS,
                    "Conversation 不读取通知正文。请返回主页使用可撤销的“通知助手”，它只做受控摘要。\n"
                    + "Conversation cannot read notification content. Use the revocable Notification Assistant from the main screen.");
            case "读取我的联系人":
            case "把我的联系人发给模型":
            case "read my contacts":
                return deny(Category.CONTACTS,
                    "Conversation 不读取或上传联系人。需要联系人能力时必须走未来的显式授权工具。\n"
                    + "Conversation cannot read or upload contacts. A future scoped tool must request explicit permission.");
            case "我现在在哪里":
            case "读取我的实时位置":
            case "where am i right now":
                return deny(Category.LOCATION,
                    "Conversation 不读取实时位置。需要定位时必须使用单独、可见、按次授权的能力。\n"
                    + "Conversation cannot read live location. Location requires a separate visible per-use permission path.");
            case "读取我的相册":
            case "把相册内容发给模型":
            case "read my photos":
                return deny(Category.PRIVATE_MEDIA,
                    "Conversation 不读取或上传相册/私人文件。当前页面也没有文件与图片工具。\n"
                    + "Conversation cannot read or upload photos or private files and has no media/file tool.");
            case "告诉我验证码":
            case "读取我的密码":
            case "show me my verification code":
                return deny(Category.CREDENTIALS,
                    "Conversation 永不读取密码、验证码或凭据；请勿把这些内容粘贴给模型。\n"
                    + "Conversation never reads passwords, verification codes, or credentials. Do not paste them into a model.");
            default:
                return new Result(Category.NONE, "");
        }
    }

    private static Result deny(Category category, String detail) {
        return new Result(category,
            "【本地隐私拒绝｜零模型调用】\n[LOCAL PRIVACY DENIAL | ZERO MODEL CALLS]\n" + detail);
    }

    private static String normalize(String input) {
        if (input == null) return "";
        String value = input.trim();
        if (value.length() > 256 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) return "";
        value = value.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        while (!value.isEmpty() && "。！？!?.,，".indexOf(value.charAt(value.length() - 1)) >= 0)
            value = value.substring(0, value.length() - 1).trim();
        return value;
    }
}
