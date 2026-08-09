package io.github.toolazytoname.xiaohei;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.HardwareBuffer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** User-enabled semantic executor. No screenshots and no hidden/background task starts. */
public final class XiaoheiAccessibilityService extends AccessibilityService {
    private static final String TAG = "XiaoheiAgent";
    private static final String CHANNEL = "xiaohei_agent";
    private static final int NOTIFICATION_ID = 1210;
    private static final long RECOVERY_HANDOFF_MS = 15_000;
    private static final String ACTION_CAPTURE_VISUAL =
        "io.github.toolazytoname.xiaohei.action.CAPTURE_VISUAL_RECOVERY";
    private static volatile XiaoheiAccessibilityService active;
    /** One-shot local preview. Never persisted, traced, or sent to a model. */
    private static volatile Bitmap visualRecoveryPreview;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String pendingLabel;
    private String pendingPackage;
    private List<String> pendingLabels;
    private int pendingIndex;
    private String taskId;
    private long deadline;
    private int steps;
    private int recoveries;
    private boolean recoveryScheduled;
    private String lastActionKey;
    private long lastActionAt;
    private boolean executing;

    static boolean isConnected() { return active != null; }
    static AgentSnapshot observeNow() {
        XiaoheiAccessibilityService service = active;
        return service == null ? null : AgentSnapshot.capture(service.getRootInActiveWindow());
    }
    static boolean startTask(String label) {
        XiaoheiAccessibilityService service = active;
        return service != null && service.begin("com.android.settings", Arrays.asList(label));
    }
    static boolean startTask(List<String> labels) {
        XiaoheiAccessibilityService service = active;
        return service != null && service.begin("com.android.settings", labels);
    }
    static boolean startTask(String packageName, String label) {
        XiaoheiAccessibilityService service = active;
        return service != null && service.begin(packageName, Arrays.asList(label));
    }
    static void stopTask(String reason) {
        XiaoheiAccessibilityService service = active;
        if (service != null) service.stopInternal(reason);
    }
    static Bitmap consumeVisualRecoveryPreview() {
        Bitmap preview = visualRecoveryPreview;
        visualRecoveryPreview = null;
        return preview;
    }
    static boolean hasVisualRecoveryPreview() { return visualRecoveryPreview != null; }

    @Override protected void onServiceConnected() {
        active = this;
        Log.i(TAG, "accessibility service connected");
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (pendingLabel == null || executing || recoveryScheduled) return;
        if (System.currentTimeMillis() > deadline) {
            stopInternal("任务超时，未执行");
            return;
        }
        CharSequence pkg = event.getPackageName();
        if (pkg == null || !pkg.toString().equals(pendingPackage)) return;
        executing = true;
        handler.postDelayed(this::executePendingStep, 350);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_CAPTURE_VISUAL.equals(intent.getAction())) captureVisualRecovery();
        return START_NOT_STICKY;
    }

    @Override public void onInterrupt() { stopInternal("系统中断"); }
    @Override public void onDestroy() {
        stopInternal("服务关闭");
        if (active == this) active = null;
        super.onDestroy();
    }

    private boolean begin(String packageName, List<String> labels) {
        if (packageName == null || !AgentPolicy.packageAllowed(packageName) || labels == null
                || labels.isEmpty() || labels.size() > 8 || pendingLabel != null) return false;
        for (String label : labels) if (label == null || label.trim().isEmpty()) return false;
        pendingLabels = new java.util.ArrayList<>();
        for (String label : labels) pendingLabels.add(label.trim());
        pendingIndex = 0;
        pendingLabel = pendingLabels.get(0);
        pendingPackage = packageName;
        taskId = UUID.randomUUID().toString();
        deadline = System.currentTimeMillis() + 60_000;
        steps = 0;
        recoveries = 0;
        recoveryScheduled = false;
        lastActionKey = null;
        showNotification("等待目标页面：" + pendingLabel);
        Log.i(TAG, "task=start id=" + taskId + " max_steps=8 timeout_ms=60000 labels=" + pendingLabels.size());
        return true;
    }

    private void executePendingStep() {
        executing = false;
        if (pendingLabel == null) return;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        AgentSnapshot before = AgentSnapshot.capture(root);
        if (!before.packageName.equals(pendingPackage)) return;
        if (!AgentPolicy.packageAllowed(before.packageName)) {
            trace(before, 0, "deny", false, "denied");
            stopInternal("拒绝：App 不在允许列表 " + before.packageName);
            return;
        }
        AgentPolicy.Decision decision = AgentPolicy.assess(
            before.packageName, before.visibleText(), pendingLabel);
        if (decision != AgentPolicy.Decision.ALLOW) {
            trace(before, 0, decision == AgentPolicy.Decision.DENY ? "deny" : "require_confirmation",
                false, "denied");
            stopInternal(decision == AgentPolicy.Decision.DENY
                ? "敏感页面或动作已拒绝" : "动作需要单独确认，本任务未执行");
            return;
        }
        if (++steps > 8) { stopInternal("超过最大 8 步"); return; }
        String key = before.packageName + "|" + before.version + "|" + pendingLabel;
        if (key.equals(lastActionKey) && System.currentTimeMillis() - lastActionAt < 2000) {
            stopInternal("重复动作保护已触发");
            return;
        }
        AccessibilityNodeInfo target = find(root, pendingLabel);
        if (target == null) {
            if (recoveries++ == 0) {
                Log.i(TAG, "step=observe version=" + before.version + " result=not_found recovery=1");
                trace(before, 0, "allow", false, "not_found");
                recoveryScheduled = true;
                handler.postDelayed(() -> {
                    recoveryScheduled = false;
                    executePendingStep();
                }, RECOVERY_HANDOFF_MS);
            } else { trace(before, 0, "allow", false, "not_found"); stopInternal("一次恢复后仍未找到目标"); }
            return;
        }
        AccessibilityNodeInfo clickable = target;
        while (clickable != null && !clickable.isClickable()) clickable = clickable.getParent();
        if (clickable == null) { trace(before, 0, "allow", false, "not_clickable"); stopInternal("目标不可点击"); return; }
        lastActionKey = key;
        lastActionAt = System.currentTimeMillis();
        executing = true;
        boolean clicked = clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        Log.i(TAG, "step=" + steps + " before=" + before.version + " action=click label="
            + pendingLabel + " ok=" + clicked);
        if (!clicked) { trace(before, 0, "allow", false, "error"); executing = false; stopInternal("系统拒绝语义点击"); return; }
        handler.postDelayed(() -> {
            AgentSnapshot after = AgentSnapshot.capture(getRootInActiveWindow());
            Log.i(TAG, "step=" + steps + " after=" + after.version + " package=" + after.packageName);
            if (!pendingPackage.equals(after.packageName)) {
                AgentTraceStore.append(this, taskId, steps, before.version, after.version,
                    before.packageName, pendingLabel, "allow", true, "package_changed");
                executing = false;
                stopInternal("动作后离开目标 App；已停止，未继续执行");
                return;
            }
            AgentTraceStore.append(this, taskId, steps, before.version, after.version,
                before.packageName, pendingLabel, "allow", true, "success");
            executing = false;
            pendingIndex++;
            if (pendingLabels != null && pendingIndex < pendingLabels.size()) {
                pendingLabel = pendingLabels.get(pendingIndex);
                recoveries = 0;
                showNotification("步骤 " + (pendingIndex + 1) + "/" + pendingLabels.size() + "：" + pendingLabel);
                handler.postDelayed(this::executePendingStep, 650);
            } else complete("完成 " + steps + " 个语义动作；已重新观察 snapshot " + after.version);
        }, 650);
    }

    /**
     * User invokes this from the persistent Agent notification after semantic recovery failed.
     * It is deliberately a local, one-shot handoff: no disk, trace, clipboard, or network use.
     */
    private void captureVisualRecovery() {
        if (pendingLabel == null || executing) {
            showNotification("没有可恢复的 Agent 任务；未截取屏幕");
            return;
        }
        AgentSnapshot before = AgentSnapshot.capture(getRootInActiveWindow());
        if (!pendingPackage.equals(before.packageName) || !AgentPolicy.packageAllowed(before.packageName)
                || AgentPolicy.assess(before.packageName, before.visibleText(), pendingLabel)
                    != AgentPolicy.Decision.ALLOW) {
            stopInternal("视觉恢复已拒绝：目标页面不安全或已改变");
            return;
        }
        executing = true;
        handler.removeCallbacksAndMessages(null);
        takeScreenshot(android.view.Display.DEFAULT_DISPLAY, getMainExecutor(),
            new TakeScreenshotCallback() {
                @Override public void onSuccess(ScreenshotResult result) {
                    HardwareBuffer buffer = result.getHardwareBuffer();
                    Bitmap hardware = Bitmap.wrapHardwareBuffer(buffer, result.getColorSpace());
                    Bitmap preview = hardware == null ? null
                        : hardware.copy(Bitmap.Config.ARGB_8888, false);
                    buffer.close();
                    if (preview == null) {
                        stopInternal("视觉恢复失败：系统没有返回可显示图像");
                        return;
                    }
                    visualRecoveryPreview = scalePreview(preview);
                    stopInternal("已截取一次本机视觉预览；未上传、未执行动作");
                }
                @Override public void onFailure(int errorCode) {
                    stopInternal("视觉恢复失败：系统错误 " + errorCode);
                }
            });
    }

    private static Bitmap scalePreview(Bitmap source) {
        final int maxEdge = 960;
        int width = source.getWidth();
        int height = source.getHeight();
        int edge = Math.max(width, height);
        if (edge <= maxEdge) return source;
        float ratio = (float) maxEdge / edge;
        Bitmap scaled = Bitmap.createScaledBitmap(source, Math.round(width * ratio),
            Math.round(height * ratio), true);
        source.recycle();
        return scaled;
    }

    private void trace(AgentSnapshot before, long after, String decision, boolean executed, String result) {
        AgentTraceStore.append(this, taskId == null ? "unknown-task" : taskId, Math.max(1, steps),
            before.version, after, before.packageName, pendingLabel, decision, executed, result);
    }

    private static AccessibilityNodeInfo find(AccessibilityNodeInfo node, String label) {
        AccessibilityNodeInfo clickableMatch = findClickableMatch(node, label);
        return clickableMatch != null ? clickableMatch : findAnyMatch(node, label);
    }

    /** Prefer an exact label that can actually be acted on over passive output text. */
    private static AccessibilityNodeInfo findClickableMatch(AccessibilityNodeInfo node, String label) {
        if (node == null) return null;
        if (matches(node, label) && hasClickableAncestor(node)) return node;
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo found = findClickableMatch(node.getChild(i), label);
            if (found != null) return found;
        }
        return null;
    }

    private static AccessibilityNodeInfo findAnyMatch(AccessibilityNodeInfo node, String label) {
        if (node == null) return null;
        if (matches(node, label)) return node;
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo found = findAnyMatch(node.getChild(i), label);
            if (found != null) return found;
        }
        return null;
    }

    private static boolean matches(AccessibilityNodeInfo node, String label) {
        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        return (text != null && text.toString().equals(label))
            || (desc != null && desc.toString().equals(label));
    }

    private static boolean hasClickableAncestor(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo current = node;
        while (current != null) {
            if (current.isClickable()) return true;
            current = current.getParent();
        }
        return false;
    }

    private void complete(String detail) {
        Log.i(TAG, "task=complete steps=" + steps + " detail=" + detail);
        pendingLabel = null;
        pendingPackage = null;
        pendingLabels = null;
        recoveryScheduled = false;
        showNotification(detail);
    }

    private void stopInternal(String reason) {
        if (pendingLabel != null) Log.i(TAG, "task=stopped steps=" + steps + " reason=" + reason);
        pendingLabel = null;
        pendingPackage = null;
        pendingLabels = null;
        recoveryScheduled = false;
        executing = false;
        handler.removeCallbacksAndMessages(null);
        showNotification("已停止：" + reason);
    }

    private void showNotification(String detail) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(new NotificationChannel(
            CHANNEL, "小黑 Phone Agent", NotificationManager.IMPORTANCE_LOW));
        PendingIntent open = PendingIntent.getActivity(this, 21,
            new Intent(this, AgentActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent stop = PendingIntent.getActivity(this, 22,
            new Intent(this, AgentActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra("agent_stop", true),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent visual = PendingIntent.getService(this, 23,
            new Intent(this, XiaoheiAccessibilityService.class).setAction(ACTION_CAPTURE_VISUAL),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder notification = new Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_xiaohei_tile)
            .setContentTitle("小黑 Phone Agent")
            .setContentText(detail).setStyle(new Notification.BigTextStyle().bigText(detail))
            .setContentIntent(open).setOngoing(pendingLabel != null).setOnlyAlertOnce(true)
            .addAction(new Notification.Action.Builder(null, "停止 Agent", stop).build());
        if (pendingLabel != null) notification.addAction(
            new Notification.Action.Builder(null, "一次本机视觉预览", visual).build());
        manager.notify(NOTIFICATION_ID, notification.build());
    }
}
