package io.github.toolazytoname.xiaohei;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import org.json.JSONObject;

/** Versioned support report. No identifiers, endpoints, tokens, text content, or UI trees. */
final class DiagnosticReport {
    static String build(Context context, WakewordBroker.State assistant, DspProfileClient.Status dsp) {
        try {
            JSONObject capabilities = new JSONObject()
                .put("local_asr", LocalAsrEngine.isBundled())
                .put("cpu_kws", LocalKwsEngine.isBundled())
                .put("dsp_profile", new DspProfileClient(context).isInstalled())
                .put("accessibility_connected", XiaoheiAccessibilityService.isConnected())
                .put("notification_access", XiaoheiNotificationListener.accessGranted(context));
            JSONObject permissions = new JSONObject()
                .put("record_audio", granted(context, Manifest.permission.RECORD_AUDIO))
                .put("camera", granted(context, Manifest.permission.CAMERA))
                .put("post_notifications", Build.VERSION.SDK_INT < 33
                    || granted(context, Manifest.permission.POST_NOTIFICATIONS));
            android.content.SharedPreferences cpu = context.getSharedPreferences(
                "cpu_wakeword", Context.MODE_PRIVATE);
            int schema = context.getSharedPreferences("model_channels", Context.MODE_PRIVATE)
                .getInt("config_schema", 0);
            JSONObject report = new JSONObject()
                .put("schema_version", 1)
                .put("app_version", "0.2.0-alpha.2")
                .put("android_api", Build.VERSION.SDK_INT)
                .put("device_family", safe(Build.MANUFACTURER) + " " + safe(Build.MODEL))
                .put("assistant_state", String.valueOf(assistant))
                .put("dsp_state", dsp == null ? "UNAVAILABLE" : safe(dsp.state))
                .put("cpu_kws_state", safe(cpu.getString("state", "OFF")))
                .put("config_schema", schema)
                .put("capabilities", capabilities)
                .put("permissions", permissions)
                .put("public_log_safe", true);
            return report.toString(2);
        } catch (Exception error) {
            return "{\"schema_version\":1,\"app_version\":\"0.2.0-alpha.2\","
                + "\"android_api\":26,\"device_family\":\"unavailable\","
                + "\"assistant_state\":\"UNAVAILABLE\",\"dsp_state\":\"UNAVAILABLE\","
                + "\"cpu_kws_state\":\"OFF\",\"config_schema\":0,"
                + "\"capabilities\":{},\"permissions\":{},\"public_log_safe\":true}";
        }
    }

    private static boolean granted(Context context, String permission) {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    private static String safe(String value) {
        if (value == null) return "";
        String clean = value.replaceAll("[\\p{Cntrl}]", " ");
        return clean.substring(0, Math.min(clean.length(), 64));
    }
}
