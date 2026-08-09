package io.github.toolazytoname.xiaohei;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/** Debug-only, visible acceptance harness. It uses the production semantic executor unchanged. */
public final class AgentMatrixActivity extends Activity {
    private static final Case[] CASES = new Case[] {
        new Case("Settings", "com.android.settings", "Network & internet"),
        new Case("Contacts", "com.android.contacts", "Open navigation drawer"),
        new Case("Clock", "com.android.deskclock", "More options"),
        new Case("Dialer", "com.android.dialer", "Contacts"),
        new Case("Files", "com.android.documentsui", "Images"),
        new Case("Camera", "com.android.camera2", "Options"),
        new Case("Calendar", "com.android.calendar", "Today"),
        new Case("Gallery", "com.android.gallery3d", "Albums"),
        new Case("Messaging", "com.android.messaging", "More options"),
        new Case("WebView shell", "org.chromium.webview_shell", "Load URL")
    };

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<String> results = new ArrayList<>();
    private TextView status;
    private Button start;
    private int index;
    private long caseDeadline;
    private boolean awaitingReturn;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) == 0) {
            finish();
            return;
        }
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);
        TextView title = new TextView(this);
        title.setText("10 App Phone Agent acceptance matrix\n固定低风险标签；逐项包绑定并重新观察");
        title.setTextSize(22);
        root.addView(title);
        status = new TextView(this);
        status.setPadding(0, pad, 0, pad);
        status.setText("准备就绪。该调试入口不会出现在 release 包。\n无障碍服务必须已由测试员启用。");
        root.addView(status);
        start = new Button(this);
        start.setText("运行第 1 项");
        start.setOnClickListener(v -> runVisibleCase());
        root.addView(start);
        setContentView(root);
    }

    @Override protected void onResume() {
        super.onResume();
        if (awaitingReturn) handler.postDelayed(this::recordReturnedCase, 250);
    }

    private void runVisibleCase() {
        if (!XiaoheiAccessibilityService.isConnected()) {
            status.setText("无法开始：无障碍执行器未连接");
            return;
        }
        if (index >= CASES.length) {
            results.clear();
            index = 0;
        }
        Case value = CASES[index];
        Intent launch = getPackageManager().getLaunchIntentForPackage(value.packageName);
        if (launch == null) {
            results.add(value.name + "\tSKIP_NOT_INSTALLED");
            index++;
            updateReport();
            return;
        }
        if (!XiaoheiAccessibilityService.startTask(value.packageName, value.label)) {
            results.add(value.name + "\tSTART_REJECTED");
            index++;
            updateReport();
            return;
        }
        start.setEnabled(false);
        awaitingReturn = true;
        status.setText("RUNNING " + (index + 1) + "/" + CASES.length + "\nApp="
            + value.packageName + "\n精确标签=" + value.label
            + "\n动作完成后请返回本页记录结果；不会后台拉起下一 App。");
        caseDeadline = System.currentTimeMillis() + 15_000;
        startActivity(launch);
    }

    private void recordReturnedCase() {
        if (!awaitingReturn) return;
        if (XiaoheiAccessibilityService.isTaskRunning()) {
            if (System.currentTimeMillis() >= caseDeadline) {
                XiaoheiAccessibilityService.stopTask("矩阵单项 15 秒超时");
            } else {
                handler.postDelayed(this::recordReturnedCase, 250);
                return;
            }
        }
        Case value = CASES[index];
        String result = XiaoheiAccessibilityService.lastTaskResult();
        results.add(value.name + "\t" + (result == null ? "NO_RESULT" : result));
        awaitingReturn = false;
        index++;
        updateReport();
    }

    private void updateReport() {
        int passed = 0;
        StringBuilder report = new StringBuilder(index >= CASES.length ? "完成：\n" : "当前结果：\n");
        for (String result : results) {
            report.append(result).append('\n');
            if (result.contains("\tsuccess:")) passed++;
        }
        report.append("passed=").append(passed).append('/').append(CASES.length);
        status.setText(report.toString());
        start.setEnabled(true);
        start.setText(index >= CASES.length ? "重新运行矩阵" : "运行第 " + (index + 1) + " 项");
    }

    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private static final class Case {
        final String name;
        final String packageName;
        final String label;
        Case(String name, String packageName, String label) {
            this.name = name;
            this.packageName = packageName;
            this.label = label;
        }
    }
}
