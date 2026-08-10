package io.github.toolazytoname.xiaohei;

import java.util.Locale;

/** Deterministic offline help only. It is not a model, planner, or action router. */
final class OfflineFaqFallback {
    static final String LABEL = "【本地固定 FAQ｜不是远端模型】\n[LOCAL FIXED FAQ | NOT A REMOTE MODEL]\n";
    private static final int MAX_INPUT_CHARS = 256;

    static final class Result {
        final boolean handled;
        final String text;
        final int modelCalls;
        final int actionCalls;
        final boolean usesContext;

        private Result(boolean handled, String text) {
            this.handled = handled;
            this.text = text;
            this.modelCalls = 0;
            this.actionCalls = 0;
            this.usesContext = false;
        }
    }

    private OfflineFaqFallback() {}

    static Result answer(String input) {
        String question = normalize(input);
        switch (question) {
            case "你能做什么":
            case "小黑能做什么":
            case "你会做什么":
            case "what can you do":
            case "what can xiaohei do":
                return known("这里只能回答少量离线帮助问题。Conversation 没有手机动作、工具、通知、文件或 root 权限；固定短命令和可见 Phone Agent 是另外的受控入口。\n"
                    + "This fallback answers a small help list only. Conversation has no phone-action, tool, notification, file, or root authority.");
            case "现在离线吗":
            case "我离线了吗":
            case "是不是断网了":
            case "are you offline":
            case "is the phone offline":
                return known("远端 Conversation 本轮没有成功，所以显示这条本地答案；这不等于已经证明整台手机断网。请检查渠道配置与网络后重试。\n"
                    + "The remote Conversation turn did not succeed, so this local answer is shown. It does not prove that the whole phone is offline.");
            case "怎么停止":
            case "如何停止":
            case "怎么取消":
            case "how do i stop":
            case "how do i cancel":
                return known("点“停止”会取消在途请求并暂停；点“清空”或“结束”会删除本页内存上下文。它们都不会额外调用模型。\n"
                    + "Stop cancels the in-flight request and pauses. Clear or End removes this page's in-memory context. These controls add no model call.");
            case "隐私怎么样":
            case "会保存聊天吗":
            case "聊天记录保存吗":
            case "what about privacy":
            case "do you save chats":
                return known("本页会话正文只在内存中保留，锁屏、离开、结束或超限会清空；这条固定 FAQ 不联网、不读取手机数据，也不能执行动作。\n"
                    + "Conversation text is memory-only and clears on lock, leave, end, or limits. This fixed FAQ uses no network or phone data and cannot act.");
            case "这是本地模型吗":
            case "你是本地模型吗":
            case "有没有本地模型":
            case "is this a local model":
            case "do you have a local model":
                return known("这不是生成式本地模型，而是确定性的固定 FAQ。当前公开 APK 不内置 0.6B 权重，也不会把固定答案伪装成模型回复。\n"
                    + "This is deterministic fixed FAQ, not a generative local model. The public APK bundles no 0.6B weights and never presents this as model output.");
            default:
                return new Result(false, "本地固定 FAQ 无匹配 / No local fixed FAQ match");
        }
    }

    private static Result known(String answer) {
        return new Result(true, LABEL + answer);
    }

    private static String normalize(String input) {
        if (input == null) return "";
        String value = input.trim();
        if (value.isEmpty() || value.length() > MAX_INPUT_CHARS
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) return "";
        value = value.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        while (!value.isEmpty() && "。！？!?.,，".indexOf(value.charAt(value.length() - 1)) >= 0)
            value = value.substring(0, value.length() - 1).trim();
        return value;
    }
}
