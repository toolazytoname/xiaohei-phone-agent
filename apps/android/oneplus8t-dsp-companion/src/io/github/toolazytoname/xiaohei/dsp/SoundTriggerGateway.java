package io.github.toolazytoname.xiaohei.dsp;

import android.app.ActivityThread;
import android.content.Context;
import android.hardware.soundtrigger.SoundTrigger;
import android.hardware.soundtrigger.SoundTriggerModule;
import android.media.permission.Identity;
import android.os.Build;
import android.os.Process;
import java.util.ArrayList;

/** Device-gated SoundTrigger preflight and attach/detach lifecycle. */
final class SoundTriggerGateway {
    private static SoundTriggerModule module;
    private static String lastCallback = "无";

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
        if (!isExpectedDevice()) return new ProbeResult(false, "设备 gate 不匹配；未调用 SoundTrigger");
        try {
            ArrayList<SoundTrigger.ModuleProperties> modules = modules();
            if (modules.isEmpty()) return new ProbeResult(false, "没有发现 SoundTrigger 模块");
            SoundTrigger.ModuleProperties first = modules.get(0);
            return new ProbeResult(true, "发现 " + modules.size() + " 个模块；模块 0 id="
                + first.getId() + " vendor=" + first.getUuid()
                + "；当前=" + (module == null ? "DETACHED" : "ATTACHED"));
        } catch (Throwable error) {
            return failure(error);
        }
    }

    static synchronized ProbeResult attach(Context context) {
        if (!isExpectedDevice()) return new ProbeResult(false, "设备 gate 不匹配；未 attach");
        if (module != null) return new ProbeResult(true, "已 attach；未加载模型");
        try {
            ArrayList<SoundTrigger.ModuleProperties> modules = modules();
            if (modules.isEmpty()) return new ProbeResult(false, "没有模块可 attach");
            module = SoundTrigger.attachModuleAsOriginator(
                modules.get(0).getId(), new Listener(), null, identity());
            if (module == null) return new ProbeResult(false, "attach 返回 null");
            return new ProbeResult(true, "模块 0 已 attach；未加载模型、未启动识别");
        } catch (Throwable error) {
            module = null;
            return failure(error);
        }
    }

    static synchronized ProbeResult detach(Context context) {
        if (module == null) return new ProbeResult(true, "当前未 attach；无需释放");
        try {
            module.detach();
            module = null;
            return new ProbeResult(true, "已 detach；客户端已释放。最近回调：" + lastCallback);
        } catch (Throwable error) {
            module = null;
            return failure(error);
        }
    }

    private static ArrayList<SoundTrigger.ModuleProperties> modules() {
        ArrayList<SoundTrigger.ModuleProperties> modules = new ArrayList<>();
        int status = SoundTrigger.listModulesAsOriginator(modules, identity());
        if (status != 0) throw new IllegalStateException("读取模块失败，状态=" + status);
        return modules;
    }

    private static Identity identity() {
        Identity identity = new Identity();
        identity.packageName = ActivityThread.currentOpPackageName();
        identity.uid = Process.myUid();
        identity.pid = Process.myPid();
        return identity;
    }

    private static boolean isExpectedDevice() { return "OnePlus8T".equals(Build.DEVICE); }

    private static ProbeResult failure(Throwable error) {
        Throwable cause = error.getCause() == null ? error : error.getCause();
        String message = cause.getMessage();
        return new ProbeResult(false, cause.getClass().getSimpleName()
            + (message == null ? "" : "：" + message));
    }

    private static final class Listener implements SoundTrigger.StatusListener {
        @Override public void onRecognition(SoundTrigger.RecognitionEvent event) {
            lastCallback = "unexpected recognition（未启动识别）";
        }
        @Override public void onResourcesAvailable() { lastCallback = "resources available"; }
        @Override public void onServiceDied() { lastCallback = "service died"; module = null; }
        @Override public void onModelUnloaded(int modelHandle) {
            lastCallback = "model unloaded handle=" + modelHandle;
        }
    }
}
