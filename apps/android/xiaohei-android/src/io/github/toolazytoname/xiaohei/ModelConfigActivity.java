package io.github.toolazytoname.xiaohei;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import java.net.URI;

/** Independent ASR and Phone Agent channels; changing one never starts or stops the other. */
public final class ModelConfigActivity extends Activity {
    private Spinner asr;
    private Switch agentEnabled;
    private EditText endpoint;
    private EditText model;
    private EditText token;
    private TextView status;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setTitle("模型渠道 / Model channels");
        setContentView(buildView());
        load();
    }

    private View buildView() {
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        TextView title = new TextView(this);
        title.setText("独立模型渠道\nIndependent model channels");
        title.setTextSize(24);
        root.addView(title);

        TextView asrLabel = new TextView(this);
        asrLabel.setText("语音识别 / ASR（只负责转写）");
        asrLabel.setPadding(0, pad, 0, 0);
        root.addView(asrLabel);
        asr = new Spinner(this);
        asr.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
            new String[] {"小黑离线中文 ASR", "Android 系统识别服务"}));
        root.addView(asr);

        TextView agentLabel = new TextView(this);
        agentLabel.setText("Phone Agent（只负责复杂任务规划；短命令不调用）");
        agentLabel.setPadding(0, pad, 0, 0);
        root.addView(agentLabel);
        agentEnabled = new Switch(this);
        agentEnabled.setText("启用 Phone Agent 渠道");
        root.addView(agentEnabled);
        endpoint = field("OpenAI-compatible HTTPS base URL");
        model = field("模型名 / model id");
        token = field("新 Token（留空则保持原值）");
        token.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        root.addView(endpoint); root.addView(model); root.addView(token);

        Button save = new Button(this);
        save.setText("保存独立配置");
        save.setOnClickListener(v -> save());
        root.addView(save);
        Button clearToken = new Button(this);
        clearToken.setText("清除 Phone Agent Token");
        clearToken.setOnClickListener(v -> { SecureSecretStore.clear(this); show("Token 已清除"); });
        root.addView(clearToken);
        status = new TextView(this);
        status.setPadding(0, pad, 0, 0);
        root.addView(status);
        return root;
    }

    private EditText field(String hint) {
        EditText value = new EditText(this);
        value.setHint(hint);
        value.setSingleLine(true);
        return value;
    }

    private void load() {
        android.content.SharedPreferences prefs = getSharedPreferences("model_channels", Context.MODE_PRIVATE);
        asr.setSelection(prefs.getInt("asr_mode", 0));
        agentEnabled.setChecked(prefs.getBoolean("agent_enabled", false));
        endpoint.setText(prefs.getString("agent_endpoint", ""));
        model.setText(prefs.getString("agent_model", ""));
        show("ASR 与 Phone Agent 相互独立；Token "
            + (SecureSecretStore.isConfigured(this) ? "已安全配置" : "未配置"));
    }

    private void save() {
        String url = endpoint.getText().toString().trim();
        if (agentEnabled.isChecked() && !validEndpoint(url)) {
            show("未保存：启用 Agent 时 URL 必须是 HTTPS，或本机 localhost/127.0.0.1");
            return;
        }
        try {
            String newToken = token.getText().toString();
            if (!newToken.isEmpty()) SecureSecretStore.save(this, newToken);
            getSharedPreferences("model_channels", Context.MODE_PRIVATE).edit()
                .putInt("asr_mode", asr.getSelectedItemPosition())
                .putBoolean("agent_enabled", agentEnabled.isChecked())
                .putString("agent_endpoint", url)
                .putString("agent_model", model.getText().toString().trim())
                .apply();
            token.setText("");
            show("已保存；未启动任何服务。Token "
                + (SecureSecretStore.isConfigured(this) ? "已安全配置" : "未配置"));
        } catch (Exception error) {
            show("保存失败：Android Keystore 不可用");
        }
    }

    private static boolean validEndpoint(String value) {
        try {
            URI uri = URI.create(value);
            String host = uri.getHost();
            return "https".equalsIgnoreCase(uri.getScheme()) ||
                ("http".equalsIgnoreCase(uri.getScheme()) &&
                    ("127.0.0.1".equals(host) || "localhost".equalsIgnoreCase(host)));
        } catch (RuntimeException ignored) { return false; }
    }

    private void show(String text) { status.setText(text); }
}
