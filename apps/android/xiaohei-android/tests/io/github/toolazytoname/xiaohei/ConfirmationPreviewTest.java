package io.github.toolazytoname.xiaohei;

public final class ConfirmationPreviewTest {
    public static void main(String[] args) {
        ConfirmationPreview.Card card = ConfirmationPreview.phoneAgent("com.android.settings", "网络和互联网");
        check(card != null && card.requiresLocalGesture && card.executionCalls == 0, "safe card");
        String text = card.visibleText();
        for (String field : new String[] { "App：", "目标 / Target：", "内容 / Content：", "权限 / Permission：", "停止与回滚 / Stop & rollback：", "默认操作是取消" })
            check(text.contains(field), "missing visible field " + field);
        check(ConfirmationPreview.phoneAgent("bad package", "网络") == null, "bad package");
        check(ConfirmationPreview.phoneAgent("com.android.settings", "bad\nlabel") == null, "bad label");
        System.out.println("PASS confirmation-preview app=1 target=1 content=1 permission=1 rollback=1 cancel_default=1 execution=0");
    }
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
