package io.github.toolazytoname.xiaohei;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Map;
import java.util.HashMap;

final class ConfigMigrator {
    static void run(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("model_channels", Context.MODE_PRIVATE);
        Map<String, Object> migrated = ConfigMigration.migrate(new HashMap<String, Object>(prefs.getAll()));
        if (migrated.equals(prefs.getAll())) return;
        SharedPreferences.Editor editor = prefs.edit().clear();
        for (Map.Entry<String, Object> item : migrated.entrySet()) {
            Object value = item.getValue();
            if (value instanceof String) editor.putString(item.getKey(), (String) value);
            else if (value instanceof Boolean) editor.putBoolean(item.getKey(), (Boolean) value);
            else if (value instanceof Integer) editor.putInt(item.getKey(), (Integer) value);
            else if (value instanceof Long) editor.putLong(item.getKey(), (Long) value);
            else if (value instanceof Float) editor.putFloat(item.getKey(), (Float) value);
        }
        editor.apply();
    }
}
