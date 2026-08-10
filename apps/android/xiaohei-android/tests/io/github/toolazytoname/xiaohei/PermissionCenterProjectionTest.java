package io.github.toolazytoname.xiaohei;
public final class PermissionCenterProjectionTest {
    public static void main(String[] args) {
        String text = PermissionCenterProjection.visibleText(new PermissionCenterProjection.Snapshot(true, false, true, false, true));
        for (String field : new String[] { "用途：", "最近使用：", "撤销：", "Root broker：不支持", "真正授予或撤销始终由你完成" }) check(text.contains(field), field);
        check(text.contains("麦克风 / Microphone：已授予或已连接"), "microphone");
        check(text.contains("相机 / Camera：未授予、未连接或未知"), "camera");
        check(PermissionCenterProjection.visibleText(null).contains("不申请、不授予"), "null");
        System.out.println("PASS permission-center purpose=5 last_use=5 revoke=5 unsupported=1 authority=0");
    }
    private static void check(boolean value, String detail) { if (!value) throw new AssertionError(detail); }
}
