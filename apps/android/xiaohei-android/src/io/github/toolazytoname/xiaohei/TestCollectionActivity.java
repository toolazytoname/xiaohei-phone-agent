package io.github.toolazytoname.xiaohei;

import android.app.Activity;
import android.app.KeyguardManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Process;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Visible, cancel-first caller for the disposable MediaStore test collection. */
public final class TestCollectionActivity extends Activity {
    private TextView state;
    private Button confirm;
    private LocalAndroidToolFlow.Request pending;
    private LocalAndroidToolFlow flow;
    private AuthorizedAndroidToolExecution execution;
    private ToolExecutionCoordinator.CancellationSignal running;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        flow = new LocalAndroidToolFlow(new ToolGateway(), new FreshConfirmationGate(),
            SystemClock::elapsedRealtime, () -> java.util.UUID.randomUUID().toString());
        execution = new AuthorizedAndroidToolExecution(this);
        setTitle("小黑受控工具验收");
        setContentView(buildView());
    }

    private View buildView() {
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        TextView title = new TextView(this);
        title.setText("受控 Android 工具 / Controlled Android tool\n只读验收入口");
        title.setTextSize(24);
        root.addView(title);
        state = new TextView(this);
        state.setPadding(0, pad, 0, pad);
        state.setText("默认不执行。此页只能查询小黑专用测试目录的数量。\n不会读取图片内容、复制、改名、删除或访问私人相册。");
        root.addView(state);
        Button prepare = new Button(this);
        prepare.setText("准备只读查询（不执行）");
        prepare.setOnClickListener(v -> prepare());
        root.addView(prepare);
        confirm = new Button(this);
        confirm.setText("本机确认：查询测试目录数量");
        confirm.setVisibility(View.GONE);
        confirm.setOnClickListener(v -> confirm());
        root.addView(confirm);
        Button cancel = new Button(this);
        cancel.setText("取消待确认或停止当前查询");
        cancel.setOnClickListener(v -> cancel("已取消；没有继续或重试"));
        root.addView(cancel);
        TextView boundary = new TextView(this);
        boundary.setPadding(0, pad, 0, 0);
        boundary.setText("边界：必须在解锁、亮屏、前台页面上由你点击确认；令牌仅此一次、最长 10 秒。全局停止会取消执行。失败不会自动重试。此入口不提供 shell、root、网络或任意路径能力。");
        root.addView(boundary);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        return scroll;
    }

    private void prepare() {
        if (running != null) { state.setText("已有查询正在运行；请先停止或等待结果"); return; }
        pending = flow.prepareQuery();
        confirm.setVisibility(View.VISIBLE);
        state.setText("待确认：仅查询 Pictures/XiaoheiTest/ 的项目数。\n请核对范围后点击下方本机确认；默认不会执行。");
    }

    private void confirm() {
        LocalAndroidToolFlow.Request request = pending;
        pending = null;
        confirm.setVisibility(View.GONE);
        if (request == null) { state.setText("没有有效待确认请求；没有执行"); return; }
        ToolGateway.Result authorization = flow.confirmAndAuthorize(request, deviceState(), Process.myUid());
        if (authorization == null || authorization.decision != ToolGateway.Decision.ALLOW) {
            state.setText("确认或本地授权未通过；没有执行");
            return;
        }
        running = new ToolExecutionCoordinator.CancellationSignal();
        ToolExecutionCoordinator.CancellationSignal signal = running;
        state.setText("正在查询测试目录；最长 10 秒，可随时停止");
        new Thread(() -> {
            ToolExecutionCoordinator.Result result = execution.execute(authorization, request.call, signal);
            runOnUiThread(() -> showResult(result, signal));
        }, "xiaohei-test-collection-query").start();
    }

    private FreshConfirmationGate.DeviceState deviceState() {
        boolean unlocked = !getSystemService(KeyguardManager.class).isKeyguardLocked();
        boolean interactive = getSystemService(PowerManager.class).isInteractive();
        return new FreshConfirmationGate.DeviceState(unlocked, interactive, hasWindowFocus());
    }

    private void showResult(ToolExecutionCoordinator.Result result,
            ToolExecutionCoordinator.CancellationSignal signal) {
        if (running != signal) return;
        running = null;
        if (result.status == ToolExecutionCoordinator.Status.SUCCESS) {
            String count = result.output.get("collection_count");
            state.setText("完成：测试目录项目数为 " + (count == null ? "未知" : count)
                + "。没有读取内容或修改文件。");
        } else state.setText("未完成：" + result.status + " / " + result.errorCode
            + "。没有自动重试；请人工检查权限或状态。");
    }

    private void cancel(String message) {
        pending = null;
        confirm.setVisibility(View.GONE);
        flow.cancel();
        if (running != null) running.cancel(ToolExecutionCoordinator.CancellationSignal.Reason.USER);
        state.setText(message);
    }

    @Override protected void onDestroy() {
        cancel("页面关闭；已取消");
        super.onDestroy();
    }
}
