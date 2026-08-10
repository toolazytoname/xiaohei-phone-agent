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
    private TextView state;
    private Button send;
    private Button cancel;
    private PendingConversationCall pending;
    private long generation;
    private boolean destroyed;

    @Override public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setTitle("小黑聊天 / Xiaohei chat");
        setContentView(build());
    }

    private View build() {
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        TextView notice = new TextView(this);
        notice.setText(
                "单轮文字聊天。模型没有手机操作、工具、通知、文件或 root 权限。\n" +
                        "Single-turn chat only; model output cannot operate your phone."
        );
        notice.setContentDescription("conversation-authority-notice");
        root.addView(notice);

        state = new TextView(this);
        state.setText("状态：空闲 / Status: idle");
        state.setPadding(0, pad / 2, 0, 0);
        state.setContentDescription("conversation-state");
        root.addView(state);

        input = new EditText(this);
        input.setHint("输入一句话 / Type one message");
        input.setMinLines(3);
        input.setMaxLines(8);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setContentDescription("conversation-input");
        root.addView(input);

        send = new Button(this);
        send.setText("发送聊天请求（无动作权限）");
        send.setContentDescription("conversation-send");
        send.setOnClickListener(view -> send());
        root.addView(send);

        cancel = new Button(this);
        cancel.setText("取消当前聊天请求");
        cancel.setContentDescription("conversation-cancel");
        cancel.setEnabled(false);
        cancel.setOnClickListener(view -> cancelCurrent());
        root.addView(cancel);

        output = new TextView(this);
        output.setPadding(0, pad, 0, 0);
        output.setText("尚未发送请求");
        output.setTextIsSelectable(true);
        output.setContentDescription("conversation-output");
        root.addView(output);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        return scroll;
    }

    private void send() {
        PendingConversationCall previous = pending;
        if (previous != null) previous.requestCancel();

        PendingConversationCall call = new PendingConversationCall(++generation);
        pending = call;
        setRunning(true);
        state.setText("状态：请求中 / Status: requesting");
        output.setText("正在聊天；不会执行手机操作…");

        ConversationClient.Request next = ConversationClient.ask(
                this,
                input.getText().toString(),
                result -> onResult(call, result)
        );
        call.bind(next::cancel);
    }

    private void cancelCurrent() {
        PendingConversationCall current = pending;
        if (current == null || !current.requestCancel()) return;
        cancel.setEnabled(false);
        state.setText("状态：正在取消 / Status: cancelling");
        output.setText("正在取消当前聊天请求；不会执行手机操作…");
    }

    private void onResult(PendingConversationCall call, ConversationClient.Result result) {
        if (!call.finish()) return;
        runOnUiThread(() -> {
            if (destroyed || pending != call) return;
            pending = null;
            setRunning(false);
            if (result.cancelled) {
                state.setText("状态：已取消 / Status: cancelled");
            } else if (result.ok) {
                state.setText("状态：回复完成（仅显示） / Status: reply shown only");
            } else {
                state.setText("状态：请求失败 / Status: failed");
            }
            output.setText(result.text);
        });
    }

    private void setRunning(boolean running) {
        send.setEnabled(!running);
        cancel.setEnabled(running);
        input.setEnabled(!running);
    }

    @Override protected void onDestroy() {
        destroyed = true;
        PendingConversationCall current = pending;
        pending = null;
        if (current != null) current.requestCancel();
        super.onDestroy();
    }
}
