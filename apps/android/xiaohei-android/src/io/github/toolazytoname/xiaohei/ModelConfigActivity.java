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
import java.util.HashMap;

/** Independent ASR, Conversation, TTS, and Phone Agent channels with separate secret slots. */
public final class ModelConfigActivity extends Activity {
    private Spinner asr;
    private Switch conversationEnabled;
    private EditText conversationEndpoint;
    private EditText conversationModel;
    private EditText conversationToken;
    private Spinner ttsProvider;
    private EditText ttsRelayEndpoint;
    private EditText ttsVoice;
    private EditText ttsToken;
    private Switch agentEnabled;
    private EditText endpoint;
    private EditText model;
    private EditText token;
    private EditText backup;
    private TextView status;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setTitle("模型与语音渠道 / Model and speech channels");
        setContentView(buildView());
        load();
    }

    private View buildView() {
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        TextView title = new TextView(this);
        title.setText("独立模型与语音渠道\nIndependent model and speech channels");
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

        TextView localModelNotice = new TextView(this);
        localModelNotice.setText("本地小模型建议 / Local-small-model guidance\n"
            + "当前公开 APK 不内置生成式本地模型。未来本地小模型最多用于分类、固定 FAQ、隐私改写和离线解释；它不会自动启用、切换模型、规划任务或调用工具。\n"
            + "The public APK bundles no generative local model. A future local-small model may only assist classification, fixed FAQ, privacy rewrite, and offline explanation; it cannot auto-enable, switch models, plan, or call tools.");
        localModelNotice.setContentDescription("local-small-model-guidance");
        localModelNotice.setPadding(0, pad, 0, 0);
        root.addView(localModelNotice);

        TextView conversationLabel = new TextView(this);
        conversationLabel.setText("Conversation（只负责聊天；独立 Token，不调用工具）");
        conversationLabel.setPadding(0, pad, 0, 0);
        root.addView(conversationLabel);
        conversationEnabled = new Switch(this);
        conversationEnabled.setText("启用 Conversation 渠道");
        conversationEnabled.setContentDescription("conversation-enabled");
        root.addView(conversationEnabled);
        conversationEndpoint = field("Conversation OpenAI-compatible HTTPS base URL");
        conversationEndpoint.setContentDescription("conversation-endpoint");
        conversationModel = field("Conversation 模型名 / model id");
        conversationModel.setContentDescription("conversation-model");
        conversationToken = field("Conversation 新 Token（留空则保持原值）");
        conversationToken.setContentDescription("conversation-token");
        conversationToken.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        root.addView(conversationEndpoint); root.addView(conversationModel); root.addView(conversationToken);
        Button clearConversationToken = new Button(this);
        clearConversationToken.setText("清除 Conversation Token");
        clearConversationToken.setOnClickListener(v -> {
            SecureSecretStore.clear(this, SecureSecretStore.Slot.CONVERSATION);
            show("Conversation Token 已清除");
        });
        root.addView(clearConversationToken);

        TextView ttsLabel = new TextView(this);
        ttsLabel.setText("Conversation TTS（独立输出；切换不改变 Conversation 或 Phone Agent）");
        ttsLabel.setPadding(0, pad, 0, 0);
        root.addView(ttsLabel);
        ttsProvider = new Spinner(this);
        ttsProvider.setContentDescription("tts-provider-selector");
        ttsProvider.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
            new String[] {"关闭 / Off", "系统 TTS / System TTS", "中转 TTS / Relay TTS"}));
        root.addView(ttsProvider);
        ttsRelayEndpoint = field("TTS relay HTTPS URL（或本机 loopback HTTP）");
        ttsRelayEndpoint.setContentDescription("tts-relay-endpoint");
        ttsVoice = field("TTS voice id（可留空）");
        ttsVoice.setContentDescription("tts-voice-id");
        ttsToken = field("TTS relay 新 Token（留空则保持原值）");
        ttsToken.setContentDescription("tts-relay-token");
        ttsToken.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        root.addView(ttsRelayEndpoint); root.addView(ttsVoice); root.addView(ttsToken);
        Button clearTtsToken = new Button(this);
        clearTtsToken.setText("清除 TTS Relay Token");
        clearTtsToken.setContentDescription("clear-tts-relay-token");
        clearTtsToken.setOnClickListener(v -> {
            SecureSecretStore.clear(this, SecureSecretStore.Slot.TTS_RELAY);
            show("TTS Relay Token 已清除；未启动或停止任何服务");
        });
        root.addView(clearTtsToken);

        TextView agentLabel = new TextView(this);
        agentLabel.setText("Phone Agent（只负责复杂任务规划；短命令不调用）");
        agentLabel.setPadding(0, pad, 0, 0);
        root.addView(agentLabel);
        agentEnabled = new Switch(this);
        agentEnabled.setText("启用 Phone Agent 渠道");
        agentEnabled.setContentDescription("phone-agent-enabled");
        root.addView(agentEnabled);
        endpoint = field("OpenAI-compatible HTTPS base URL");
        endpoint.setContentDescription("phone-agent-endpoint");
        model = field("模型名 / model id");
        model.setContentDescription("phone-agent-model");
        token = field("新 Token（留空则保持原值）");
        token.setContentDescription("phone-agent-token");
        token.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        root.addView(endpoint); root.addView(model); root.addView(token);

        Button save = new Button(this);
        save.setText("保存独立配置");
        save.setContentDescription("save-independent-channels");
        save.setOnClickListener(v -> save());
        root.addView(save);
        Button health = new Button(this);
        health.setText("低成本健康检查（只请求 /models）");
        health.setOnClickListener(v -> healthCheck());
        root.addView(health);
        Button clearToken = new Button(this);
        clearToken.setText("清除 Phone Agent Token");
        clearToken.setOnClickListener(v -> { SecureSecretStore.clear(this); show("Token 已清除"); });
        root.addView(clearToken);
        TextView backupLabel = new TextView(this);
        backupLabel.setText("备份与恢复（不含 Token；恢复会停用 Conversation、TTS 与 Agent）");
        backupLabel.setPadding(0, pad, 0, 0);
        root.addView(backupLabel);
        Button exportBackup = new Button(this);
        exportBackup.setText("导出非敏感渠道备份");
        exportBackup.setOnClickListener(v -> exportBackup());
        root.addView(exportBackup);
        backup = new EditText(this);
        backup.setHint("粘贴 xiaohei-model-channels.v2/v3 备份以恢复");
        backup.setMinLines(4);
        backup.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        root.addView(backup);
        Button restoreBackup = new Button(this);
        restoreBackup.setText("恢复备份（清除 Token，保持 Conversation/TTS/Agent 关闭）");
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
        conversationEnabled.setChecked(prefs.getBoolean(ChannelProfileConfig.CONVERSATION_ENABLED, false));
        conversationEndpoint.setText(prefs.getString(ChannelProfileConfig.CONVERSATION_ENDPOINT, ""));
        conversationModel.setText(prefs.getString(ChannelProfileConfig.CONVERSATION_MODEL, ""));
        TtsChannelConfig.Provider provider = TtsChannelConfig.Provider.fromId(
            prefs.getString(TtsChannelConfig.PROVIDER, TtsChannelConfig.Provider.OFF.id));
        ttsProvider.setSelection(provider.ordinal());
        ttsRelayEndpoint.setText(prefs.getString(TtsChannelConfig.RELAY_ENDPOINT, ""));
        ttsVoice.setText(prefs.getString(TtsChannelConfig.VOICE, ""));
        agentEnabled.setChecked(prefs.getBoolean("agent_enabled", false));
        endpoint.setText(prefs.getString("agent_endpoint", ""));
        model.setText(prefs.getString("agent_model", ""));
        show("ASR、Conversation、TTS 与 Phone Agent 相互独立；TTS=" + provider.id
            + "；Conversation Token "
            + (SecureSecretStore.isConfigured(this, SecureSecretStore.Slot.CONVERSATION) ? "已安全配置" : "未配置")
            + "；TTS Relay Token "
            + (SecureSecretStore.isConfigured(this, SecureSecretStore.Slot.TTS_RELAY) ? "已安全配置" : "未配置")
            + "；Phone Agent Token "
            + (SecureSecretStore.isConfigured(this) ? "已安全配置" : "未配置"));
    }

    private void save() {
        String url = endpoint.getText().toString().trim();
        String conversationUrl = conversationEndpoint.getText().toString().trim();
        TtsChannelConfig.Provider selectedTts = selectedTtsProvider();
        String ttsUrl = ttsRelayEndpoint.getText().toString().trim();
        String voice = ttsVoice.getText().toString().trim();
        if (conversationEnabled.isChecked() && !TtsChannelConfig.validEndpoint(conversationUrl)) {
            show("未保存：启用 Conversation 时 URL 必须是 HTTPS，或本机 localhost/127.0.0.1");
            return;
        }
        if (agentEnabled.isChecked() && !TtsChannelConfig.validEndpoint(url)) {
            show("未保存：启用 Agent 时 URL 必须是 HTTPS，或本机 localhost/127.0.0.1");
            return;
        }
        try {
            TtsChannelConfig.withTts(new HashMap<String, Object>(), selectedTts, ttsUrl, voice);
            String newToken = token.getText().toString();
            String newConversationToken = conversationToken.getText().toString();
            String newTtsToken = ttsToken.getText().toString();
            if (!newToken.isEmpty()) SecureSecretStore.save(this, newToken);
            if (!newConversationToken.isEmpty())
                SecureSecretStore.save(this, SecureSecretStore.Slot.CONVERSATION, newConversationToken);
            if (!newTtsToken.isEmpty())
                SecureSecretStore.save(this, SecureSecretStore.Slot.TTS_RELAY, newTtsToken);
            getSharedPreferences("model_channels", Context.MODE_PRIVATE).edit()
                .putInt("asr_mode", asr.getSelectedItemPosition())
                .putBoolean(ChannelProfileConfig.CONVERSATION_ENABLED, conversationEnabled.isChecked())
                .putString(ChannelProfileConfig.CONVERSATION_ENDPOINT, conversationUrl)
                .putString(ChannelProfileConfig.CONVERSATION_MODEL,
                    conversationModel.getText().toString().trim())
                .putString(TtsChannelConfig.PROVIDER, selectedTts.id)
                .putString(TtsChannelConfig.RELAY_ENDPOINT, ttsUrl)
                .putString(TtsChannelConfig.VOICE, voice)
                .putBoolean("agent_enabled", agentEnabled.isChecked())
                .putString("agent_endpoint", url)
                .putString("agent_model", model.getText().toString().trim())
                .apply();
            token.setText("");
            conversationToken.setText("");
            ttsToken.setText("");
            show("已保存；TTS=" + selectedTts.id + "，未启动或停止任何服务。Conversation 无工具权限；Conversation Token "
                + (SecureSecretStore.isConfigured(this, SecureSecretStore.Slot.CONVERSATION) ? "已安全配置" : "未配置")
                + "；TTS Relay Token "
                + (SecureSecretStore.isConfigured(this, SecureSecretStore.Slot.TTS_RELAY) ? "已安全配置" : "未配置")
                + "；Phone Agent Token "
                + (SecureSecretStore.isConfigured(this) ? "已安全配置" : "未配置"));
        } catch (IllegalArgumentException invalid) {
            show("未保存：Relay TTS 必须使用 HTTPS，或本机 localhost/127.0.0.1 HTTP；字段不能含换行或超长");
        } catch (Exception error) {
            show("保存失败：Android Keystore 不可用");
        }
    }

    private void exportBackup() {
        try {
            String value = ModelChannelBackup.export(asr.getSelectedItemPosition(), conversationEnabled.isChecked(),
                conversationEndpoint.getText().toString(), conversationModel.getText().toString(), agentEnabled.isChecked(),
                endpoint.getText().toString(), model.getText().toString(), selectedTtsProvider(),
                ttsRelayEndpoint.getText().toString(), ttsVoice.getText().toString());
            startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND).setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, "Xiaohei non-secret model-channel backup")
                .putExtra(Intent.EXTRA_TEXT, value), "导出小黑非敏感备份"));
            show("已生成不含 Token 的备份；请仅保存到可信位置");
        } catch (IllegalArgumentException invalid) { show("无法导出：渠道字段格式无效"); }
    }

    private void healthCheck() {
        status.setText("正在检查渠道连通性；不会发送规划请求");
        new Thread(() -> {
            String result = PhoneAgentClient.healthCheck(this);
            runOnUiThread(() -> show(result));
        }, "xiaohei-agent-health").start();
    }

    private void restoreBackup() {
        try {
            ModelChannelBackup.Data value = ModelChannelBackup.parse(backup.getText().toString());
            getSharedPreferences("model_channels", Context.MODE_PRIVATE).edit()
                .putInt("asr_mode", value.asrMode)
                .putBoolean(ChannelProfileConfig.CONVERSATION_ENABLED, false)
                .putString(ChannelProfileConfig.CONVERSATION_ENDPOINT, value.conversationEndpoint)
                .putString(ChannelProfileConfig.CONVERSATION_MODEL, value.conversationModel)
                .putString(TtsChannelConfig.PROVIDER, TtsChannelConfig.Provider.OFF.id)
                .putString(TtsChannelConfig.RELAY_ENDPOINT, value.ttsRelayEndpoint)
                .putString(TtsChannelConfig.VOICE, value.ttsVoice)
                .putBoolean("agent_enabled", false)
                .putString("agent_endpoint", value.endpoint)
                .putString("agent_model", value.model)
                .apply();
            SecureSecretStore.clear(this);
            SecureSecretStore.clear(this, SecureSecretStore.Slot.CONVERSATION);
            SecureSecretStore.clear(this, SecureSecretStore.Slot.TTS_RELAY);
            conversationEnabled.setChecked(false);
            conversationEndpoint.setText(value.conversationEndpoint);
            conversationModel.setText(value.conversationModel);
            conversationToken.setText("");
            ttsProvider.setSelection(TtsChannelConfig.Provider.OFF.ordinal());
            ttsRelayEndpoint.setText(value.ttsRelayEndpoint);
            ttsVoice.setText(value.ttsVoice);
            ttsToken.setText("");
            agentEnabled.setChecked(false);
            endpoint.setText(value.endpoint);
            model.setText(value.model);
            token.setText("");
            show("已恢复非敏感配置；三个 Token 均已清除，Conversation、TTS 与 Phone Agent 保持关闭且未启动服务");
        } catch (IllegalArgumentException invalid) { show("无法恢复：备份格式无效或不受支持"); }
    }

    private TtsChannelConfig.Provider selectedTtsProvider() {
        int selected = ttsProvider.getSelectedItemPosition();
        TtsChannelConfig.Provider[] providers = TtsChannelConfig.Provider.values();
        return selected >= 0 && selected < providers.length ? providers[selected] : TtsChannelConfig.Provider.OFF;
    }

    private void show(String text) { status.setText(text); }
}
