package io.github.toolazytoname.xiaohei;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.Arrays;

/** Visible control surface for bounded semantic Phone Agent tasks. */
public final class AgentActivity extends Activity {
    private TextView state;
    private TextView snapshot;
    private EditText taskInput;
    private Button confirmProposal;
    private PhoneAgentClient.Proposal pendingProposal;
    private final TaskCard[] safeCards = new TaskCard[] {
        new TaskCard("系统设置：网络和互联网", "com.android.settings", "网络和互联网"),
        new TaskCard("计算器：数字 1", "com.android.calculator2", "1"),
        new TaskCard("文件：Documents", "com.android.documentsui", "Documents"),
        new TaskCard("照片：相册", "org.lineageos.glimpse", "相册"),
        new TaskCard("日历：今天", "org.lineageos.etar", "今天"),
        new TaskCard("浏览器：更多", "org.lineageos.jelly", "更多")
    };

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("小黑 Phone Agent");
        setContentView(buildView());
        consume(getIntent());
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        consume(intent);
    }

    @Override protected void onResume() {
        super.onResume();
        refresh();
    }

    private View buildView() {
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        TextView title = new TextView(this);
        title.setText("可见 Phone Agent\nVisible, bounded, stoppable");
        title.setTextSize(24);
        root.addView(title);
        state = new TextView(this);
        state.setPadding(0, pad, 0, pad);
        root.addView(state);

        taskInput = new EditText(this);
        taskInput.setHint("描述一个低风险任务，例如：打开设置里的网络和互联网");
        taskInput.setSingleLine(false);
        root.addView(taskInput);
        Button plan = new Button(this);
        plan.setText("请求 Phone Agent 规划（只预览，不执行）");
        plan.setOnClickListener(v -> requestPlan());
        root.addView(plan);
        confirmProposal = new Button(this);
        confirmProposal.setText("确认并执行这一个语义步骤");
        confirmProposal.setVisibility(View.GONE);
        confirmProposal.setOnClickListener(v -> confirmPlan());
        root.addView(confirmProposal);

        Button access = new Button(this);
        access.setText("打开 Android 无障碍设置（用户主动授权）");
        access.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        root.addView(access);
        Button observe = new Button(this);
        observe.setText("观察当前语义树（不执行）");
        observe.setOnClickListener(v -> showSnapshot());
        root.addView(observe);
        Button demo = new Button(this);
        demo.setText("真机验收：设置 → 网络和互联网（单步）");
        demo.setOnClickListener(v -> runSettingsTask("网络和互联网"));
        root.addView(demo);
        Button multi = new Button(this);
        multi.setText("真机验收：设置 → 网络和互联网 → 互联网（两步）");
        multi.setOnClickListener(v -> runSettingsSteps("网络和互联网", "互联网"));
        root.addView(multi);
        Button calculator = new Button(this);
        calculator.setText("真机验收：计算器 → 1（跨 App 单步）");
        calculator.setOnClickListener(v -> runAppTask("com.android.calculator2", "1"));
        root.addView(calculator);
        TextView safeCardLabel = new TextView(this);
        safeCardLabel.setText("常用安全入口（固定 App + 精确标签；仍需无障碍授权）");
        safeCardLabel.setPadding(0, pad, 0, 0);
        root.addView(safeCardLabel);
        Spinner safeCardSpinner = new Spinner(this);
        String[] safeCardNames = new String[safeCards.length];
        for (int i = 0; i < safeCards.length; i++) safeCardNames[i] = safeCards[i].name;
        safeCardSpinner.setAdapter(new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item, safeCardNames));
        root.addView(safeCardSpinner);
        Button launchSafeCard = new Button(this);
        launchSafeCard.setText("执行所选安全入口（单步）");
        launchSafeCard.setOnClickListener(v -> {
            TaskCard card = safeCards[safeCardSpinner.getSelectedItemPosition()];
            runAppTask(card.packageName, card.label);
        });
        root.addView(launchSafeCard);
        Button stopGate = new Button(this);
        stopGate.setText("安全验收：启动等待任务（随后测试全局停止）");
        stopGate.setOnClickListener(v -> runSettingsTask("不存在的验收目标"));
        root.addView(stopGate);
        Button denyGate = new Button(this);
        denyGate.setText("安全验收：敏感目标必须拒绝");
        denyGate.setOnClickListener(v -> runSettingsTask("输入验证码"));
        root.addView(denyGate);
        Button stop = new Button(this);
        stop.setText("全局停止 Phone Agent");
        stop.setOnClickListener(v -> {
            XiaoheiAccessibilityService.stopTask("用户全局停止");
            state.setText("Agent 已停止；没有待处理动作");
        });
        root.addView(stop);
        Button export = new Button(this);
        export.setText("导出脱敏 Agent 轨迹（JSONL）");
        export.setOnClickListener(v -> shareTrace());
        root.addView(export);
        Button clear = new Button(this);
        clear.setText("清除本机 Agent 轨迹");
        clear.setOnClickListener(v -> { AgentTraceStore.clear(this); state.setText("Agent 轨迹已清除"); });
        root.addView(clear);

        TextView policy = new TextView(this);
        policy.setText("边界：最多 8 步 / 60 秒 / 一次恢复 / 重复动作保护。支付、银行、凭据、密码和验证码页面默认拒绝；发送、删除、安装、卸载、授权和拨号需单独确认。当前不使用截图回退。");
        policy.setPadding(0, pad, 0, pad);
        root.addView(policy);
        snapshot = new TextView(this);
        root.addView(snapshot);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        return scroll;
    }

    private void consume(Intent intent) {
        if (intent != null && intent.getBooleanExtra("agent_stop", false)) {
            XiaoheiAccessibilityService.stopTask("用户从通知停止");
            intent.removeExtra("agent_stop");
        }
    }

    private void refresh() {
        state.setText(XiaoheiAccessibilityService.isConnected()
            ? "状态：CONNECTED；等待用户启动任务" : "状态：未授权或服务尚未连接");
    }

    private void showSnapshot() {
        AgentSnapshot value = XiaoheiAccessibilityService.observeNow();
        snapshot.setText(value == null ? "无法观察：服务未连接"
            : truncate(value.compact(), 4000));
    }

    private void runSettingsTask(String label) {
        if (!XiaoheiAccessibilityService.startTask(label)) {
            state.setText("无法启动：请先授权服务，或已有任务正在执行");
            return;
        }
        state.setText("RUNNING：即将打开系统设置；目标=" + label + "；可从通知停止");
        startActivity(new Intent(Settings.ACTION_SETTINGS));
    }

    private void runSettingsSteps(String... labels) {
        if (!XiaoheiAccessibilityService.startTask(Arrays.asList(labels))) {
            state.setText("无法启动多步任务：请先授权服务，或已有任务正在执行");
            return;
        }
        state.setText("RUNNING：两步语义任务；每步后重新观察；可随时全局停止");
        startActivity(new Intent(Settings.ACTION_SETTINGS));
    }

    private void runAppTask(String packageName, String label) {
        if (!XiaoheiAccessibilityService.startTask(packageName, label)) {
            state.setText("无法启动跨 App 任务：请先授权服务，或已有任务正在执行");
            return;
        }
        Intent launch = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launch == null) {
            XiaoheiAccessibilityService.stopTask("验收目标未安装");
            state.setText("验收目标未安装；没有执行");
            return;
        }
        state.setText("RUNNING：预期 App=" + packageName + "；目标=" + label);
        startActivity(launch);
    }

    private void shareTrace() {
        String trace = AgentTraceStore.export(this);
        if (trace.isEmpty()) { state.setText("暂无可导出的 Agent 轨迹"); return; }
        startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND).setType("application/x-ndjson")
            .putExtra(Intent.EXTRA_SUBJECT, "Xiaohei redacted agent trace")
            .putExtra(Intent.EXTRA_TEXT, trace), "导出脱敏 Agent 轨迹"));
    }

    private void requestPlan() {
        String task = taskInput.getText().toString().trim();
        if (task.isEmpty()) { state.setText("请先描述任务"); return; }
        pendingProposal = null;
        confirmProposal.setVisibility(View.GONE);
        state.setText("PLANNING：仅请求一个低风险步骤；此时不会操作手机");
        new Thread(() -> {
            PhoneAgentClient.Proposal proposal = PhoneAgentClient.plan(this, task);
            runOnUiThread(() -> {
                if (!proposal.ok) {
                    state.setText(proposal.explanation);
                    return;
                }
                pendingProposal = proposal;
                state.setText("待确认计划\nApp：" + proposal.packageName + "\n语义目标："
                    + proposal.label + "\n说明：" + proposal.explanation
                    + "\n模型没有执行权限；只有下面的确认按钮能进入本地策略层。");
                confirmProposal.setVisibility(View.VISIBLE);
            });
        }, "xiaohei-agent-plan").start();
    }

    private void confirmPlan() {
        PhoneAgentClient.Proposal proposal = pendingProposal;
        pendingProposal = null;
        confirmProposal.setVisibility(View.GONE);
        if (proposal == null || AgentPolicy.assess(proposal.packageName, "", proposal.label)
                != AgentPolicy.Decision.ALLOW || !AgentPolicy.packageAllowed(proposal.packageName)) {
            state.setText("计划已过期或被本地策略拒绝");
            return;
        }
        if (!XiaoheiAccessibilityService.startTask(proposal.packageName, proposal.label)) {
            state.setText("执行层未连接或已有任务；没有执行");
            return;
        }
        if (proposal.packageName.equals("com.android.settings")) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
            return;
        }
        Intent launch = getPackageManager().getLaunchIntentForPackage(proposal.packageName);
        if (launch == null) {
            XiaoheiAccessibilityService.stopTask("目标 App 不可启动");
            state.setText("目标 App 不可启动；计划已停止");
            return;
        }
        startActivity(launch);
    }

    private static String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max) + "\n…[truncated]";
    }

    private static final class TaskCard {
        final String name;
        final String packageName;
        final String label;
        TaskCard(String name, String packageName, String label) {
            this.name = name;
            this.packageName = packageName;
            this.label = label;
        }
    }
}
