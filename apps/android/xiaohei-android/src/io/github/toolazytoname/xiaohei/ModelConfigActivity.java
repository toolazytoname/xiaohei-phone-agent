package io.github.toolazytoname.xiaohei;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ScrollView;
import java.net.URI;

/** Independent ASR and Phone Agent channels; changing one never starts or stops the other. */
public final class ModelConfigActivity extends Activity {
    private Spinner asr;
    private Switch agentEnabled;
    private EditText endpoint;
    private EditText model;
    private EditText token;
    private EditText backup;
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
        TextView backupLabel = new TextView(this);
        backupLabel.setText("备份与恢复（不含 Token；恢复会停用 Agent）");
        backupLabel.setPadding(0, pad, 0, 0);
        root.addView(backupLabel);
        Button exportBackup = new Button(this);
        exportBackup.setText("导出非敏感渠道备份");
        exportBackup.setOnClickListener(v -> exportBackup());
        root.addView(exportBackup);
        backup = new EditText(this);
        backup.setHint("粘贴 xiaohei-model-channels.v1 备份以恢复");
        backup.setMinLines(4);
        backup.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        root.addView(backup);
        Button restoreBackup = new Button(this);
        restoreBackup.setText("恢复备份（清除 Token，保持 Agent 关闭）");
        restoreBackup.setOnClickListener(v -> restoreBackup());
        root.addView(restoreBackup);
        status = new TextView(this);
        status.setPadding(0, pad, 0, 0);
        root.addView(status);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        return scroll;
    }

    private EditText field(String hint) {
        EditText value = new EditText(this);
        value.setHint(hint);
        value.setSingleLine(true);
        return value;
    }

    private void load() {
        android.content.SharedPreferences prefs = getSharedPreferences("model_channels", Context.MODE_PRIVATE);
        asr.setSelection(prefs.getInt("asr_mode", LocalAsrEngine.isBundled() ? 0 : 1));
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

    private void exportBackup() {
        try {
            String value = ModelChannelBackup.export(asr.getSelectedItemPosition(), agentEnabled.isChecked(),
                endpoint.getText().toString(), model.getText().toString());
            startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND).setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, "Xiaohei non-secret model-channel backup")
                .putExtra(Intent.EXTRA_TEXT, value), "导出小黑非敏感备份"));
            show("已生成不含 Token 的备份；请仅保存到可信位置");
        } catch (IllegalArgumentException invalid) { show("无法导出：渠道字段格式无效"); }
    }

    private void restoreBackup() {
        try {
            ModelChannelBackup.Data value = ModelChannelBackup.parse(backup.getText().toString());
            getSharedPreferences("model_channels", Context.MODE_PRIVATE).edit()
                .putInt("asr_mode", value.asrMode)
                .putBoolean("agent_enabled", false)
                .putString("agent_endpoint", value.endpoint)
                .putString("agent_model", value.model)
                .apply();
            SecureSecretStore.clear(this);
            agentEnabled.setChecked(false);
            endpoint.setText(value.endpoint);
            model.setText(value.model);
            token.setText("");
            show("已恢复非敏感配置；Token 已清除，Phone Agent 保持关闭且未启动服务");
        } catch (IllegalArgumentException invalid) { show("无法恢复：备份格式无效或不受支持"); }
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
