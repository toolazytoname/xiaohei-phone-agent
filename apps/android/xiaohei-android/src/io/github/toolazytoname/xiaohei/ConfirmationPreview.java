package io.github.toolazytoname.xiaohei;

/** Fixed, user-visible review of one bounded Phone Agent proposal; it grants and executes nothing. */
final class ConfirmationPreview {
    static final class Card {
        final String app;
        final String target;
        final String content;
        final String permission;
        final String rollback;
        final boolean requiresLocalGesture;
        final int executionCalls = 0;

        Card(String app, String target, String content, String permission, String rollback) {
            this.app = app;
            this.target = target;
            this.content = content;
            this.permission = permission;
            this.rollback = rollback;
            this.requiresLocalGesture = true;
        }

        String visibleText() {
            return "确认前请逐项核对 / Review before confirming\n"
                + "App：" + app + "\n目标 / Target：" + target + "\n内容 / Content：" + content
                + "\n权限 / Permission：" + permission + "\n停止与回滚 / Stop & rollback：" + rollback
                + "\n默认操作是取消；只有本机前台点击确认才会进入既有本地策略层。";
        }
    }

    static Card phoneAgent(String packageName, String target) {
        if (!validPackage(packageName) || !validLabel(target)) return null;
        return new Card(packageName, target, "仅尝试一次精确的低风险语义步骤；不发送、不删除、不授权",
            "需要用户已主动授予的 Android 无障碍服务；本卡不申请或扩大权限",
            "随时点“全局停止 Phone Agent”；未完成动作不会自动重试或继续");
    }

    private static boolean validPackage(String value) {
        return value != null && value.matches("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+")
            && value.length() <= 128;
    }

    private static boolean validLabel(String value) {
        return value != null && !value.trim().isEmpty() && value.indexOf('\n') < 0
            && value.codePointCount(0, value.length()) <= 80;
    }
}
