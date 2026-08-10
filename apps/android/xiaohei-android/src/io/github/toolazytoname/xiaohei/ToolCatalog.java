package io.github.toolazytoname.xiaohei;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
final class ToolCatalog {
    enum Risk { OBSERVE, LOW, REVERSIBLE, HIGH }
    private static final Map<String,Risk> TOOLS;
    static { Map<String,Risk> m=new HashMap<>(); m.put("android.open_settings",Risk.LOW); m.put("android.open_gallery",Risk.LOW); m.put("android.open_dialer",Risk.LOW); m.put("android.adjust_volume",Risk.REVERSIBLE); m.put("android.observe",Risk.OBSERVE); TOOLS=Collections.unmodifiableMap(m); }
    static Risk risk(String tool) { return TOOLS.get(tool); }
    static boolean allowed(String tool, Risk requested) { return TOOLS.containsKey(tool) && TOOLS.get(tool)==requested; }
}
