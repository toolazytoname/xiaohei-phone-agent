package io.github.toolazytoname.xiaohei.dsp;

import android.app.ActivityThread;
import android.content.Context;
import android.hardware.soundtrigger.SoundTrigger;
import android.hardware.soundtrigger.SoundTriggerModule;
import android.media.permission.Identity;
import android.os.Build;
import android.os.Process;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

/** Device-gated SoundTrigger preflight and attach/detach lifecycle. */
final class SoundTriggerGateway {
    private static SoundTriggerModule module;
    private static int modelHandle = -1;
    private static String lastCallback = "无";
    private static final File MODEL_FILE =
        new File("/system_ext/etc/xiaohei/sm4_xiaobuxiaobu.uim");

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
                + "；当前=" + state());
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
            if (modelHandle >= 0) {
                int status = module.unloadSoundModel(modelHandle);
                if (status != 0) return new ProbeResult(false,
                    "detach 前 unload 失败，状态=" + status + "；仍保持 attach");
                modelHandle = -1;
            }
            module.detach();
            module = null;
            return new ProbeResult(true, "已 detach；客户端已释放。最近回调：" + lastCallback);
        } catch (Throwable error) {
            module = null;
            return failure(error);
        }
    }

    static synchronized ProbeResult loadModel(Context context) {
        if (!isExpectedDevice()) return new ProbeResult(false, "设备 gate 不匹配；未加载模型");
        if (modelHandle >= 0) return new ProbeResult(true, "模型已加载，handle=" + modelHandle);
        ProbeResult attached = attach(context);
        if (!attached.ok || module == null) return attached;
        if (!MODEL_FILE.isFile() || !MODEL_FILE.canRead()) {
            return new ProbeResult(false, "私有模型不可读：" + MODEL_FILE
                + "；未调用 loadSoundModel");
        }
        try {
            byte[] data = readModel(MODEL_FILE);
            ArrayList<SoundTrigger.ModuleProperties> modules = modules();
            SoundTrigger.Keyphrase phrase = new SoundTrigger.Keyphrase(
                0, 1, Locale.forLanguageTag("en-US"), "xiaobuxiaobu.0220.0828", new int[0]);
            SoundTrigger.KeyphraseSoundModel model = new SoundTrigger.KeyphraseSoundModel(
                UUID.randomUUID(), modules.get(0).getUuid(), data,
                new SoundTrigger.Keyphrase[] { phrase });
            int[] handles = new int[20];
            int status = module.loadSoundModel(model, handles);
            if (status != 0) return new ProbeResult(false,
                "loadSoundModel 失败，状态=" + status + "；保持 attach 供诊断");
            modelHandle = handles[0];
            return new ProbeResult(true, "私有模型已加载，handle=" + modelHandle
                + "；尚未启动识别");
        } catch (Throwable error) {
            return failure(error);
        }
    }

    static synchronized ProbeResult unloadModel(Context context) {
        if (modelHandle < 0) return new ProbeResult(true, "当前没有已加载模型；无需卸载");
        try {
            int handle = modelHandle;
            int status = module.unloadSoundModel(handle);
            if (status != 0) return new ProbeResult(false,
                "unloadSoundModel 失败，状态=" + status + "；handle=" + handle);
            modelHandle = -1;
            return new ProbeResult(true, "模型已卸载，handle=" + handle
                + "；模块仍保持 attach");
        } catch (Throwable error) {
            return failure(error);
        }
    }

    private static byte[] readModel(File file) throws Exception {
        long length = file.length();
        if (length <= 0 || length > 32L * 1024L * 1024L) {
            throw new IllegalStateException("模型尺寸异常：" + length);
        }
        byte[] data = new byte[(int) length];
        try (FileInputStream input = new FileInputStream(file)) {
            int offset = 0;
            while (offset < data.length) {
                int read = input.read(data, offset, data.length - offset);
                if (read < 0) throw new IllegalStateException("模型读取不完整：" + offset);
                offset += read;
            }
        }
        return data;
    }

    private static String state() {
        if (module == null) return "DETACHED";
        return modelHandle < 0 ? "ATTACHED" : "MODEL_LOADED(handle=" + modelHandle + ")";
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
        @Override public void onServiceDied() {
            lastCallback = "service died";
            module = null;
            modelHandle = -1;
        }
        @Override public void onModelUnloaded(int modelHandle) {
            lastCallback = "model unloaded handle=" + modelHandle;
            if (SoundTriggerGateway.modelHandle == modelHandle) SoundTriggerGateway.modelHandle = -1;
        }
    }
}
