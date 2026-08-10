package io.github.toolazytoname.xiaohei;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Single-turn chat surface. Model output is display-only and has zero action authority. */
public final class ConversationActivity extends Activity {
    private EditText input;
    private TextView output;
    private Button send;
    private ConversationClient.Request request;

    @Override public void onCreate(Bundle state) { super.onCreate(state); setTitle("小黑聊天 / Xiaohei chat"); setContentView(build()); }
    private View build() {
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(pad, pad, pad, pad);
        TextView notice = new TextView(this);
        notice.setText("单轮文字聊天。模型没有手机操作、工具、通知、文件或 root 权限。\nSingle-turn chat only; model output cannot operate your phone.");
        root.addView(notice);
        input = new EditText(this); input.setHint("输入一句话 / Type one message"); input.setMinLines(3); input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE); root.addView(input);
        send = new Button(this); send.setText("发送聊天请求（无动作权限）"); send.setOnClickListener(v -> send()); root.addView(send);
        Button cancel = new Button(this); cancel.setText("取消当前聊天请求"); cancel.setOnClickListener(v -> { if (request != null) request.cancel(); }); root.addView(cancel);
        output = new TextView(this); output.setPadding(0, pad, 0, 0); output.setText("尚未发送请求"); root.addView(output);
        ScrollView scroll = new ScrollView(this); scroll.addView(root); return scroll;
    }
    private void send() {
        if (request != null) request.cancel();
        send.setEnabled(false); output.setText("正在聊天；不会执行手机操作…");
        request = ConversationClient.ask(this, input.getText().toString(), result -> runOnUiThread(() -> {
            output.setText(result.text); send.setEnabled(true); request = null;
        }));
    }
    @Override protected void onDestroy() { if (request != null) request.cancel(); super.onDestroy(); }
}
