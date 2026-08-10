package io.github.toolazytoname.xiaohei;

import java.util.Locale;

/** Pure three-way routing preview. Classification never starts a model or Android action. */
final class IntentRouteClassifier {
    enum Route { CHAT, DETERMINISTIC_COMMAND, COMPLEX_TASK }

    static final class Result {
        final Route route;
        final CommandRouter.Request command;
        final String reason;
        final int modelCalls;
        final int actionCalls;

        private Result(Route route, CommandRouter.Request command, String reason) {
            this.route = route;
            this.command = command;
            this.reason = reason;
            this.modelCalls = 0;
            this.actionCalls = 0;
        }
    }

    private IntentRouteClassifier() {}

    static Result classify(String input) {
        String text = normalize(input);
        CommandRouter.Request command = CommandRouter.route(input);
        if (isExplicitNotificationOrDraft(text, command.action))
            return result(Route.DETERMINISTIC_COMMAND, command, "existing exact notification/draft command");
        if (hasMultiStepMarker(text))
            return result(Route.COMPLEX_TASK, unknown(), "explicit multi-step marker");
        if (isExplicitCommand(text, command))
            return result(Route.DETERMINISTIC_COMMAND, command, "existing deterministic command plus imperative cue");
        if (command.action == CommandRouter.Action.AMBIGUOUS)
            return result(Route.CHAT, unknown(), "ambiguous action is inert until ROUTE-003 clarification");
        if (isComplexTask(text))
            return result(Route.COMPLEX_TASK, unknown(), "explicit complex-task cue");
        return result(Route.CHAT, unknown(), "default conversational route");
    }

    private static boolean isExplicitCommand(String text, CommandRouter.Request command) {
        if (command.action == CommandRouter.Action.UNKNOWN || command.action == CommandRouter.Action.AMBIGUOUS)
            return false;
        if (containsAny(text, "为什么", "是什么", "什么意思", "怎么", "如何", "会不会", "是否", "区别",
                "解释", "介绍", "原理", "安全吗", "吗", "呢", "?", "？")) return false;
        return startsWithAny(text, "打开", "关闭", "请打开", "请关闭", "帮我打开", "帮我关闭", "开手电筒",
                "关手电筒", "导航到", "导航去", "请导航", "看看", "显示电话键盘", "我要拍照", "把音量",
                "音量大一点", "音量小一点", "无线网络设置", "蓝牙设置")
            || text.equals("设置") || text.equals("浏览器") || text.equals("拨号");
    }

    private static boolean isExplicitNotificationOrDraft(String text, CommandRouter.Action action) {
        if (containsAny(text, "为什么", "是什么", "什么意思", "怎么", "如何", "会不会", "是否", "区别",
                "解释", "介绍", "原理", "安全吗", "吗", "呢", "?", "？")) return false;
        if (action == CommandRouter.Action.QUERY_UNREAD_WECHAT
                || action == CommandRouter.Action.QUERY_UNREAD_ALL) {
            return text.contains("未读") && startsWithAny(text, "微信有没有", "威信有没有", "有没有",
                "读取", "查询", "检查");
        }
        if (action == CommandRouter.Action.DRAFT_WECHAT_REPLY
                || action == CommandRouter.Action.DRAFT_MESSAGE_REPLY) {
            return startsWithAny(text, "回复微信说", "回复消息说", "回复未读消息说", "帮我回复微信");
        }
        return false;
    }

    private static boolean hasMultiStepMarker(String text) {
        return containsAny(text, "然后", "接着", "完成后", "最后再", "并且", "并把", "再帮我",
            " and then ", " after that ", " then ");
    }

    private static boolean isComplexTask(String text) {
        if (containsAny(text, "解释", "介绍", "聊聊", "为什么", "是什么", "如何理解", "怎么看", "help me understand"))
            return false;
        return containsAny(text, "帮我整理", "帮我查找", "帮我比较", "帮我同步", "帮我总结并", "替我安排",
            "替我转账", "批量", "遍历", "汇总并", "生成报告", "检查所有", "help me organize",
            "help me find", "help me compare", "help me sync", "create a report");
    }

    private static Result result(Route route, CommandRouter.Request command, String reason) {
        return new Result(route, command, reason);
    }

    private static CommandRouter.Request unknown() {
        return new CommandRouter.Request(CommandRouter.Action.UNKNOWN, "");
    }

    private static String normalize(String input) {
        if (input == null) return "";
        String text = input.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        return text.length() <= 2048 ? text : text.substring(0, 2048);
    }

    private static boolean containsAny(String text, String... values) {
        for (String value : values) if (text.contains(value)) return true;
        return false;
    }

    private static boolean startsWithAny(String text, String... values) {
        for (String value : values) if (text.startsWith(value)) return true;
        return false;
    }
}
