package io.github.toolazytoname.xiaohei;

public final class RouteClarificationPolicyTest {
    private static final String[] TARGET = {
        "打开相册和相机", "打开照片和相机", "打开WiFi和蓝牙", "打开浏览器和拨号盘", "打开闹钟和浏览器",
        "相机还是相册", "WiFi还是蓝牙", "拨号盘或浏览器", "照片和拍照", "浏览器和时钟"
    };
    private static final String[] INTENT = {
        "相册", "照片", "图片", "相机", "蓝牙", "WiFi", "闹钟", "我想看照片", "我想拍照", "想打开相册"
    };
    private static final String[] SCOPE = {
        "打开", "关闭", "导航", "回复", "帮我", "请帮我", "帮我处理一下", "我想", "please help", "open"
    };
    private static final String[] CLEAR = {
        "你好", "相册是什么", "为什么相机需要权限", "怎么打开相册", "回复消息是什么意思",
        "打开相册", "导航到机场", "回复消息说收到", "微信有没有未读消息", "把音量调小",
        "帮我整理下载目录", "打开设置然后检查蓝牙", "help me organize these files", "生成报告并保存", "替我转账给某人",
        "解释半双工", "what is a browser", "如何保护隐私", "聊聊旅行", "随便聊聊"
    };

    public static void main(String[] args) {
        verifyAsk(TARGET, RouteClarificationPolicy.Kind.ASK_TARGET);
        verifyAsk(INTENT, RouteClarificationPolicy.Kind.ASK_INTENT);
        verifyAsk(SCOPE, RouteClarificationPolicy.Kind.ASK_SCOPE);
        for (String input : CLEAR) {
            RouteClarificationPolicy.Decision decision = RouteClarificationPolicy.decide(input);
            check(decision.kind == RouteClarificationPolicy.Kind.ROUTE, input + ": clear route was blocked");
            check(decision.modelCalls == 0 && decision.actionCalls == 0, "clear classification side effect");
        }
        check(TARGET.length + INTENT.length + SCOPE.length == 30, "clarification count");
        System.out.println("PASS RouteClarificationPolicyTest clarifications=30 target=10 intent=10 scope=10 clear=20 guessed_actions=0 model_calls=0 action_calls=0");
    }

    private static void verifyAsk(String[] cases, RouteClarificationPolicy.Kind expected) {
        for (String input : cases) {
            RouteClarificationPolicy.Decision decision = RouteClarificationPolicy.decide(input);
            check(decision.kind == expected, input + ": expected=" + expected + " actual=" + decision.kind);
            check(decision.route == IntentRouteClassifier.Route.CHAT, "clarification must be inert");
            check(decision.command.action == CommandRouter.Action.UNKNOWN, "clarification leaked action");
            check(decision.prompt.contains("不会猜") || decision.prompt.contains("不会执行"), "missing no-guess promise");
            check(decision.modelCalls == 0 && decision.actionCalls == 0, "clarification side effect");
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
