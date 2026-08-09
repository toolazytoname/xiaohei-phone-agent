package io.github.toolazytoname.xiaohei;

public final class CommandRouterTest {
    public static void main(String[] args) {
        Object[][] cases = {
            {"打开相册", "OPEN_GALLERY"}, {"看看照片", "OPEN_GALLERY"}, {"打开图片", "OPEN_GALLERY"},
            {"打开系统设置", "OPEN_SETTINGS"}, {"打开设置", "OPEN_SETTINGS"}, {"设置", "OPEN_SETTINGS"},
            {"打开WiFi", "OPEN_WIFI_SETTINGS"}, {"无线网络设置", "OPEN_WIFI_SETTINGS"}, {"打开WIFI设置", "OPEN_WIFI_SETTINGS"},
            {"打开蓝牙", "OPEN_BLUETOOTH_SETTINGS"}, {"蓝牙设置", "OPEN_BLUETOOTH_SETTINGS"}, {"请打开蓝牙设置", "OPEN_BLUETOOTH_SETTINGS"},
            {"打开相机", "OPEN_CAMERA"}, {"打开照相机", "OPEN_CAMERA"}, {"我要拍照", "OPEN_CAMERA"},
            {"打开浏览器", "OPEN_BROWSER"}, {"打开网页", "OPEN_BROWSER"}, {"浏览器", "OPEN_BROWSER"},
            {"打开拨号盘", "OPEN_DIALER"}, {"显示电话键盘", "OPEN_DIALER"}, {"拨号", "OPEN_DIALER"},
            {"打开闹钟", "OPEN_ALARMS"}, {"打开时钟", "OPEN_ALARMS"}, {"看看闹钟", "OPEN_ALARMS"},
            {"导航到天安门", "NAVIGATE"}, {"请导航去机场", "NAVIGATE"}, {"导航到西湖", "NAVIGATE"},
            {"打开手电筒", "TORCH_ON"}, {"开手电筒", "TORCH_ON"}, {"关闭手电筒", "TORCH_OFF"},
            {"把音量调大", "VOLUME_UP"}, {"音量大一点", "VOLUME_UP"}, {"把音量调小", "VOLUME_DOWN"}
            ,{"微信有没有未读消息", "QUERY_UNREAD_WECHAT"}, {"有没有未读通知", "QUERY_UNREAD_ALL"},
            {"回复微信说我晚点到", "DRAFT_WECHAT_REPLY"}, {"帮我回复微信未读消息", "DRAFT_WECHAT_REPLY"}
        };
        for (Object[] row : cases) {
            String actual = CommandRouter.route((String) row[0]).action.name();
            if (!row[1].equals(actual))
                throw new AssertionError(row[0] + ": expected=" + row[1] + " actual=" + actual);
        }
        if (CommandRouter.route("替我转账").action != CommandRouter.Action.UNKNOWN)
            throw new AssertionError("unknown command must be denied");
        if (CommandRouter.route("打开相册和相机").action != CommandRouter.Action.AMBIGUOUS)
            throw new AssertionError("multiple targets must require clarification");
        if (!"天安门".equals(CommandRouter.route("导航到天安门").argument))
            throw new AssertionError("navigation target was not preserved");
        if (!"我晚点到".equals(CommandRouter.route("回复微信说我晚点到").argument))
            throw new AssertionError("reply content was not preserved");
        System.out.println("PASS command-router cases=" + cases.length + " deny=1 ambiguous=1");
    }
}
