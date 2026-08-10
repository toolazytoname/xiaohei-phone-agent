package io.github.toolazytoname.xiaohei;

import java.util.Locale;

/** Converts low-confidence routing into a local question; it never guesses an action. */
final class RouteClarificationPolicy {
    enum Kind { ROUTE, ASK_TARGET, ASK_INTENT, ASK_SCOPE }

    static final class Decision {
        final Kind kind;
        final IntentRouteClassifier.Route route;
        final CommandRouter.Request command;
        final String prompt;
        final int modelCalls;
        final int actionCalls;

        private Decision(Kind kind, IntentRouteClassifier.Route route,
                CommandRouter.Request command, String prompt) {
            this.kind = kind;
            this.route = route;
            this.command = command;
            this.prompt = prompt;
            this.modelCalls = 0;
            this.actionCalls = 0;
        }
    }

    private RouteClarificationPolicy() {}

    static Decision decide(String input) {
        String text = normalize(input);
        CommandRouter.Request raw = CommandRouter.route(input);
        if (raw.action == CommandRouter.Action.AMBIGUOUS)
            return ask(Kind.ASK_TARGET,
                "检测到多个动作目标。请一次选择一个，例如“打开相册”或“打开相机”；我不会猜。\n"
                + "Multiple action targets detected. Choose one target; no action was guessed.");
        if (isIncomplete(text))
            return ask(Kind.ASK_SCOPE,
                "任务还不完整。请说明要处理什么、期望什么结果；在你说清楚前不会执行。\n"
                + "The task is incomplete. State the target and desired result; nothing will run yet.");

        IntentRouteClassifier.Result classified = IntentRouteClassifier.classify(input);
        if (isBareAmbiguousTopic(text)
                || (classified.route == IntentRouteClassifier.Route.CHAT
                && raw.action != CommandRouter.Action.UNKNOWN
                && !isClearlyConceptual(text))) {
            return ask(Kind.ASK_INTENT,
                "你是想执行这个手机动作，还是只聊这个主题？请用完整命令或完整问题说明；我不会猜。\n"
                + "Do you want a phone action or a discussion? Use a complete command or question; nothing was guessed.");
        }
        return new Decision(Kind.ROUTE, classified.route, classified.command, "");
    }

    private static Decision ask(Kind kind, String prompt) {
        return new Decision(kind, IntentRouteClassifier.Route.CHAT,
            new CommandRouter.Request(CommandRouter.Action.UNKNOWN, ""), prompt);
    }

    private static boolean isIncomplete(String text) {
        return text.equals("打开") || text.equals("关闭") || text.equals("导航") || text.equals("回复")
            || text.equals("帮我") || text.equals("请帮我") || text.equals("帮我处理一下") || text.equals("我想")
            || text.equals("please help") || text.equals("open") || text.equals("close")
            || text.equals("navigate") || text.equals("reply");
    }

    private static boolean isBareAmbiguousTopic(String text) {
        return text.equals("相册") || text.equals("照片") || text.equals("图片") || text.equals("相机")
            || text.equals("蓝牙") || text.equals("wifi") || text.equals("wi-fi") || text.equals("闹钟");
    }

    private static boolean isClearlyConceptual(String text) {
        return containsAny(text, "为什么", "是什么", "什么意思", "怎么", "如何", "会不会", "是否", "区别",
            "解释", "介绍", "原理", "安全吗", "吗", "呢", "?", "？", "what ", "why ", "how ");
    }

    private static String normalize(String input) {
        if (input == null) return "";
        return input.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ")
            .replaceAll("[。！？!?.,，]+$", "").trim();
    }

    private static boolean containsAny(String text, String... values) {
        for (String value : values) if (text.contains(value)) return true;
        return false;
    }
}
