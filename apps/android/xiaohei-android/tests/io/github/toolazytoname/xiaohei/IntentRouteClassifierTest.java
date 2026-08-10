package io.github.toolazytoname.xiaohei;

public final class IntentRouteClassifierTest {
    private static final String[] COMMANDS = {
        "打开相册", "看看照片", "打开图片", "打开像册", "打开系统设置", "设置", "打开WiFi", "无线网络设置",
        "打开蓝牙", "蓝牙设置", "请打开蓝牙设置", "打开相机", "我要拍照", "打开浏览器", "浏览器", "打开拨号盘",
        "显示电话键盘", "拨号", "打开闹钟", "看看闹钟", "导航到天安门", "请导航去机场", "打开手电筒", "开手电筒",
        "关闭手电筒", "关手电筒", "把音量调大", "音量大一点", "把音量调小", "音量小一点",
        "微信有没有未读消息", "有没有未读通知", "读取未读消息", "回复微信说我晚点到", "帮我回复微信未读消息",
        "回复消息说收到", "威信有没有未读消息", "打开wi-fi", "帮我打开相册", "请关闭手电筒"
    };
    private static final String[] CHATS = {
        "你好", "你是谁", "介绍一下你自己", "相册是什么", "为什么相机需要权限", "打开相册安全吗",
        "如何使用蓝牙", "WiFi是什么", "浏览器和搜索引擎有什么区别", "拨号是什么意思", "闹钟为什么会响",
        "导航的原理是什么", "手电筒会不会耗电", "怎么调节音量更健康", "通知隐私是什么", "微信是什么",
        "回复消息是什么意思", "照片和图片有什么区别", "聊聊旅行", "解释半双工", "什么是内存会话",
        "为什么限制六轮", "如何理解 token", "模型是什么", "本地模型和远端模型的区别", "你怎么看隐私",
        "帮我解释蓝牙原理", "help me understand privacy", "what is a browser", "why does wifi disconnect",
        "how do models work", "打开相册和相机", "相机还是相册", "", "随便聊聊"
    };
    private static final String[] COMPLEX = {
        "打开相册然后找到昨天的照片", "打开设置然后检查蓝牙并返回", "先打开浏览器然后搜索天气",
        "帮我整理下载目录", "帮我查找最大的五个文件", "帮我比较两份配置", "帮我同步文档到备份目录",
        "替我安排明天的行程", "替我转账给某人", "批量重命名这些照片", "遍历目录生成清单",
        "汇总并生成报告", "生成报告并保存", "检查所有应用然后列出异常", "打开相册并且整理截图",
        "找到日志并把结果写入文件", "完成后再帮我检查一次", "先找文件接着生成摘要", "帮我总结并创建待办",
        "help me organize these files", "help me find and compare two documents", "help me compare these configs",
        "help me sync this folder", "create a report after that", "open settings and then inspect bluetooth"
    };

    public static void main(String[] args) {
        verify(COMMANDS, IntentRouteClassifier.Route.DETERMINISTIC_COMMAND);
        verify(CHATS, IntentRouteClassifier.Route.CHAT);
        verify(COMPLEX, IntentRouteClassifier.Route.COMPLEX_TASK);
        check(COMMANDS.length + CHATS.length + COMPLEX.length == 100, "matrix must contain 100 cases");
        IntentRouteClassifier.Result ambiguous = IntentRouteClassifier.classify("打开相册和相机");
        check(ambiguous.route == IntentRouteClassifier.Route.CHAT
                && ambiguous.command.action == CommandRouter.Action.UNKNOWN, "ambiguous route must be inert");
        System.out.println("PASS IntentRouteClassifierTest cases=100 command=40 chat=35 complex=25 action_calls=0 model_calls=0 ambiguous=inert");
    }

    private static void verify(String[] cases, IntentRouteClassifier.Route expected) {
        for (String input : cases) {
            IntentRouteClassifier.Result result = IntentRouteClassifier.classify(input);
            check(result.route == expected, input + ": expected=" + expected + " actual=" + result.route);
            check(result.actionCalls == 0 && result.modelCalls == 0, input + ": classifier caused side effect");
            if (expected == IntentRouteClassifier.Route.DETERMINISTIC_COMMAND)
                check(result.command.action != CommandRouter.Action.UNKNOWN
                        && result.command.action != CommandRouter.Action.AMBIGUOUS, input + ": missing command");
            else check(result.command.action == CommandRouter.Action.UNKNOWN, input + ": non-command leaked action");
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
