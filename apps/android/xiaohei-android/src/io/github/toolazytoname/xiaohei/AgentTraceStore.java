package io.github.toolazytoname.xiaohei;

import android.content.Context;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;

/** Bounded, redacted JSONL trace. It never stores snapshots, model prompts, or secrets. */
final class AgentTraceStore {
    private static final String FILE = "agent-trace.v1.jsonl";
    private static final long MAX_BYTES = 256 * 1024;
    private static final String PREF = "agent_trace_storage_enabled";

    static synchronized void append(Context context, String taskId, int step,
            long before, long after, String pkg, String label, String decision,
            boolean executed, String result) {
        if (!enabled(context)) return;
        try {
            File file = new File(context.getFilesDir(), FILE);
            if (file.length() > MAX_BYTES) context.deleteFile(FILE);
            JSONObject row = new JSONObject();
            row.put("schema_version", 1).put("task_id", taskId).put("step_index", step)
                .put("snapshot_before", Math.max(1, before));
            if (after > 0) row.put("snapshot_after", after);
            row.put("tool", executed ? "click_text" : "observe")
                .put("target_package", safe(pkg, 255))
                .put("target_label", publicLabel(label))
                .put("policy_decision", decision).put("executed", executed)
                .put("result", result).put("captured_at", isoNow())
                .put("public_log_safe", true);
            try (FileOutputStream output = context.openFileOutput(FILE, Context.MODE_APPEND)) {
                output.write((row.toString() + "\n").getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception ignored) { }
    }

    static synchronized String export(Context context) {
        File file = new File(context.getFilesDir(), FILE);
        if (!file.isFile()) return "";
        StringBuilder value = new StringBuilder("# Xiaohei redacted agent trace v1\n");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null && value.length() < 64 * 1024)
                value.append(line).append('\n');
        } catch (Exception ignored) { return ""; }
        return value.toString();
    }

    static void clear(Context context) { context.deleteFile(FILE); }

    static boolean enabled(Context context) {
        return context.getSharedPreferences("agent_trace", Context.MODE_PRIVATE).getBoolean(PREF, false);
    }

    static void setEnabled(Context context, boolean enabled) {
        context.getSharedPreferences("agent_trace", Context.MODE_PRIVATE).edit().putBoolean(PREF, enabled).apply();
        if (!enabled) clear(context);
    }

    /** Removes exactly the newest bounded redacted record; returns false if there was no record. */
    static synchronized boolean deleteLatest(Context context) {
        File file = new File(context.getFilesDir(), FILE);
        if (!file.isFile()) return false;
        List<String> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String row; while ((row = reader.readLine()) != null) if (!row.trim().isEmpty()) rows.add(row);
        } catch (Exception failure) { return false; }
        if (rows.isEmpty()) return false; rows.remove(rows.size() - 1);
        try (FileOutputStream output = context.openFileOutput(FILE, Context.MODE_PRIVATE)) {
            for (String row : rows) output.write((row + "\n").getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Exception failure) { return false; }
    }

    private static String publicLabel(String value) {
        String safe = safe(value, 80);
        String lower = safe.toLowerCase(java.util.Locale.ROOT);
        if (lower.matches(".*(密码|验证码|口令|token|secret|password|otp|银行卡|支付).*"))
            return "[REDACTED_POLICY_TARGET]";
        return safe;
    }

    private static String safe(String value, int max) {
        if (value == null) return "";
        String clean = value.replaceAll("[\\p{Cntrl}]", " ").trim();
        return clean.length() <= max ? clean : clean.substring(0, max);
    }

    private static String isoNow() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            java.util.Locale.ROOT).format(new java.util.Date());
    }
}
