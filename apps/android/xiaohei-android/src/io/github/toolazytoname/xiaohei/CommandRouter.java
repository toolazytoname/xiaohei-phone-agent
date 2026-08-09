package io.github.toolazytoname.xiaohei;

/** Deterministic router. Unknown text never becomes an Android action. */
final class CommandRouter {
    enum Action {
        OPEN_GALLERY, OPEN_SETTINGS, OPEN_WIFI_SETTINGS, OPEN_BLUETOOTH_SETTINGS, OPEN_CAMERA, OPEN_BROWSER,
        OPEN_DIALER, OPEN_ALARMS, NAVIGATE, TORCH_ON, TORCH_OFF, VOLUME_UP, VOLUME_DOWN,
        AMBIGUOUS, UNKNOWN
    }

    static final class Request {
        final Action action;
        final String argument;
        Request(Action action, String argument) {
            this.action = action;
            this.argument = argument == null ? "" : argument;
        }
    }

    static Request route(String transcript) {
        String text = transcript == null ? "" : transcript.replaceAll("[\\s，。！？,.!?]", "");
        int candidates = 0;
        candidates += containsAny(text, "相册", "照片", "图片") ? 1 : 0;
        candidates += containsAny(text, "相机", "照相机", "拍照") ? 1 : 0;
        candidates += containsAny(text, "WiFi", "WIFI", "无线网络") ? 1 : 0;
        candidates += text.contains("蓝牙") ? 1 : 0;
        candidates += containsAny(text, "浏览器", "网页") ? 1 : 0;
        candidates += containsAny(text, "拨号", "电话键盘") ? 1 : 0;
        candidates += containsAny(text, "闹钟", "时钟") ? 1 : 0;
        if (candidates > 1) return new Request(Action.AMBIGUOUS, text);
        if (text.contains("相册") || text.contains("照片") || text.contains("图片"))
            return request(Action.OPEN_GALLERY);
        if (text.contains("WiFi") || text.contains("WIFI") || text.contains("无线网络"))
            return request(Action.OPEN_WIFI_SETTINGS);
        if (text.contains("蓝牙")) return request(Action.OPEN_BLUETOOTH_SETTINGS);
        if (text.contains("系统设置") || text.equals("打开设置") || text.equals("设置"))
            return request(Action.OPEN_SETTINGS);
        if (text.contains("相机") || text.contains("照相机") || text.contains("拍照"))
            return request(Action.OPEN_CAMERA);
        if (text.contains("浏览器") || text.contains("网页"))
            return request(Action.OPEN_BROWSER);
        if (text.contains("拨号") || text.contains("电话键盘"))
            return request(Action.OPEN_DIALER);
        if (text.contains("闹钟") || text.contains("时钟"))
            return request(Action.OPEN_ALARMS);
        if (text.contains("关闭手电筒") || text.contains("关手电筒"))
            return request(Action.TORCH_OFF);
        if (text.contains("打开手电筒") || text.contains("开手电筒"))
            return request(Action.TORCH_ON);
        if (text.contains("音量") && (text.contains("调大") || text.contains("增大")
                || text.contains("大一点"))) return request(Action.VOLUME_UP);
        if (text.contains("音量") && (text.contains("调小") || text.contains("减小")
                || text.contains("小一点"))) return request(Action.VOLUME_DOWN);
        int navigation = Math.max(text.indexOf("导航到"), text.indexOf("导航去"));
        if (navigation >= 0) {
            String target = text.substring(navigation + 3);
            if (!target.isEmpty() && target.length() <= 80)
                return new Request(Action.NAVIGATE, target);
        }
        return request(Action.UNKNOWN);
    }

    private static Request request(Action action) { return new Request(action, ""); }

    private static boolean containsAny(String text, String... needles) {
        for (String needle : needles) if (text.contains(needle)) return true;
        return false;
    }
}
