package io.github.toolazytoname.xiaohei;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/** OpenAI-compatible planner. It returns a proposal and never performs an Android action. */
final class PhoneAgentClient {
    static final class Proposal {
        final boolean ok;
        final String packageName;
        final String label;
        final String explanation;
        final String requestId;
        Proposal(boolean ok, String packageName, String label, String explanation, String requestId) {
            this.ok = ok; this.packageName = packageName; this.label = label;
            this.explanation = explanation; this.requestId = requestId;
        }
    }

    /** Sends private text only from a current-user, pending dry-run request created by the local UI. */
    static Proposal plan(Context context, UnconfirmedActionRequest.Request pending) {
        if (!validPendingRequest(pending)) return fail("待规划请求无效或已失效");
        String task = pending.userTextForPlanner();
        if (task == null || task.trim().isEmpty() || task.length() > 1024)
            return fail("任务为空或超过 1024 个字符");
        android.content.SharedPreferences prefs =
            context.getSharedPreferences("model_channels", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("agent_enabled", false))
            return fail("Phone Agent 渠道未启用");
        String endpoint = prefs.getString("agent_endpoint", "");
        String model = prefs.getString("agent_model", "");
        try {
            JSONObject request = new JSONObject();
            request.put("model", model);
            request.put("temperature", 0);
            request.put("max_tokens", 160);
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content",
                "Return JSON only: {\"package\":\"com.android.settings\",\"label\":\"exact visible text\",\"explanation\":\"short\"}. "
                + "Only propose one low-risk semantic click. Never propose payment, credentials, OTP, send, delete, install, permission grant, or calls."));
            messages.put(new JSONObject().put("role", "user").put("content", task));
            request.put("messages", messages);
            URL url = new URL(endpoint.replaceAll("/+$", "") + "/chat/completions");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(7000);
            connection.setReadTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            String token = SecureSecretStore.load(context);
            if (!token.isEmpty()) connection.setRequestProperty("Authorization", "Bearer " + token);
            connection.setDoOutput(true);
            byte[] body = request.toString().getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(body.length);
            try (OutputStream output = connection.getOutputStream()) { output.write(body); }
            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 300)
                return fail("规划服务 HTTP " + connection.getResponseCode());
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null && response.length() < 65536)
                    response.append(line);
            }
            String content = new JSONObject(response.toString()).getJSONArray("choices")
                .getJSONObject(0).getJSONObject("message").getString("content").trim();
            if (content.startsWith("```")) content = content.replaceFirst("^```(?:json)?", "")
                .replaceFirst("```$", "").trim();
            JSONObject plan = new JSONObject(content);
            String pkg = plan.getString("package");
            String label = plan.getString("label");
            if (!AgentPolicy.packageAllowed(pkg)
                    || AgentPolicy.assess(pkg, "", label) != AgentPolicy.Decision.ALLOW)
                return fail("模型提议被本地安全策略拒绝");
            return new Proposal(true, pkg, label, plan.optString("explanation", "低风险单步动作"),
                pending.requestId);
        } catch (Exception error) {
            return fail("规划失败：" + error.getClass().getSimpleName());
        }
    }

    /** A user-invoked low-cost connectivity check; it never sends a planning prompt. */
    static String healthCheck(Context context) {
        android.content.SharedPreferences prefs =
            context.getSharedPreferences("model_channels", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("agent_enabled", false)) return "Phone Agent 渠道未启用";
        String endpoint = prefs.getString("agent_endpoint", "");
        try {
            URL url = new URL(endpoint.replaceAll("/+$", "") + "/models");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(7000);
            connection.setRequestMethod("GET");
            String token = SecureSecretStore.load(context);
            if (!token.isEmpty()) connection.setRequestProperty("Authorization", "Bearer " + token);
            int code = connection.getResponseCode();
            connection.disconnect();
            return code >= 200 && code < 300 ? "健康检查通过（未发送规划请求）"
                : "健康检查 HTTP " + code + "（未发送规划请求）";
        } catch (Exception error) { return "健康检查失败：" + error.getClass().getSimpleName(); }
    }

    private static boolean validPendingRequest(UnconfirmedActionRequest.Request request) {
        String task = request == null ? null : request.userTextForPlanner();
        return request != null && request.schemaVersion == UnconfirmedActionRequest.SCHEMA_VERSION
            && request.requestId != null && request.requestId.matches("[A-Za-z0-9][A-Za-z0-9._:-]{7,127}")
            && UnconfirmedActionRequest.TARGET.equals(request.target)
            && UnconfirmedActionRequest.ACTION.equals(request.action)
            && UnconfirmedActionRequest.RISK.equals(request.risk)
            && request.requiresConfirmation
            && UnconfirmedActionRequest.CONFIRMATION_STATE.equals(request.confirmationState)
            && request.dryRun && !request.publicLogSafe
            && request.sensitiveFields.equals(Collections.singletonList(UnconfirmedActionRequest.SENSITIVE_FIELD))
            && task != null && !task.trim().isEmpty() && task.codePointCount(0, task.length()) <= 1024;
    }

    private static Proposal fail(String detail) { return new Proposal(false, "", "", detail, ""); }
}
