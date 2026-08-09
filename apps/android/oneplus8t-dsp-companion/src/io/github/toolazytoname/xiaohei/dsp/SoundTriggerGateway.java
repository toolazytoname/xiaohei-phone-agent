package io.github.toolazytoname.xiaohei.dsp;

import android.app.ActivityThread;
import android.content.Context;
import android.hardware.soundtrigger.SoundTrigger;
import android.media.permission.Identity;
import android.os.Build;
import android.os.Process;
import java.util.ArrayList;

/**
 * Android 14's hidden SoundTrigger API is represented by compile-only local
 * stubs. At runtime the boot-classpath implementation is used. This preflight
 * only lists modules; it does not attach, load a model, or capture audio.
 */
final class SoundTriggerGateway {
    static final class ProbeResult {
        final boolean ok;
        final String detail;
        ProbeResult(boolean ok, String detail) { this.ok = ok; this.detail = detail; }
        String toDisplayText() {
            return "设备：" + Build.DEVICE + " · Android " + Build.VERSION.RELEASE + "\n"
                + "预检：" + (ok ? "通过" : "未通过") + "\n" + detail;
        }
    }

    static ProbeResult probe(Context context) {
        if (!"OnePlus8T".equals(Build.DEVICE)) {
            return new ProbeResult(false, "设备 gate 不匹配；未调用 SoundTrigger");
        }
        try {
            Identity identity = new Identity();
            identity.packageName = ActivityThread.currentOpPackageName();
            identity.uid = Process.myUid();
            identity.pid = Process.myPid();
            ArrayList<SoundTrigger.ModuleProperties> modules = new ArrayList<>();
            int status = SoundTrigger.listModulesAsOriginator(modules, identity);
            if (status != 0) return new ProbeResult(false, "读取模块失败，状态=" + status);
            if (modules.isEmpty()) return new ProbeResult(false, "没有发现 SoundTrigger 模块");
            SoundTrigger.ModuleProperties first = modules.get(0);
            return new ProbeResult(true, "发现 " + modules.size() + " 个模块；模块 0 id="
                + first.getId() + " vendor=" + first.getUuid());
        } catch (Throwable error) {
            Throwable cause = error.getCause() == null ? error : error.getCause();
            String message = cause.getMessage();
            return new ProbeResult(false, cause.getClass().getSimpleName()
                + (message == null ? "" : "：" + message));
        }
    }
}
