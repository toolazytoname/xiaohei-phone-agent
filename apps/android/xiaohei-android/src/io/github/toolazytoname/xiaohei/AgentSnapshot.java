package io.github.toolazytoname.xiaohei;

import android.view.accessibility.AccessibilityNodeInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/** Versioned, bounded semantic snapshot. Password values are always redacted. */
final class AgentSnapshot {
    private static final AtomicLong VERSIONS = new AtomicLong();
    final long version;
    final long capturedAtMillis;
    final String packageName;
    final List<String> nodes;

    private AgentSnapshot(String packageName, List<String> nodes) {
        this.version = VERSIONS.incrementAndGet();
        this.capturedAtMillis = System.currentTimeMillis();
        this.packageName = packageName == null ? "" : packageName;
        this.nodes = nodes;
    }

    static AgentSnapshot capture(AccessibilityNodeInfo root) {
        List<String> nodes = new ArrayList<>();
        collect(root, nodes, 0);
        CharSequence pkg = root == null ? null : root.getPackageName();
        return new AgentSnapshot(pkg == null ? "" : pkg.toString(), nodes);
    }

    String visibleText() {
        StringBuilder out = new StringBuilder();
        for (String node : nodes) {
            if (out.length() > 0) out.append('\n');
            out.append(node);
        }
        return out.toString();
    }

    String compact() {
        return "snapshot.v1 version=" + version + " package=" + packageName
            + " nodes=" + nodes.size() + "\n" + visibleText();
    }

    private static void collect(AccessibilityNodeInfo node, List<String> out, int depth) {
        if (node == null || out.size() >= 160 || depth > 14) return;
        CharSequence value = node.isPassword() ? "[REDACTED]" : node.getText();
        CharSequence description = node.getContentDescription();
        if ((value != null && value.length() > 0) || (description != null && description.length() > 0)) {
            String text = value == null ? "" : value.toString();
            String desc = description == null ? "" : description.toString();
            out.add((node.isClickable() ? "[clickable] " : "") + text
                + (desc.isEmpty() || desc.equals(text) ? "" : " {" + desc + "}"));
        }
        for (int i = 0; i < node.getChildCount() && out.size() < 160; i++)
            collect(node.getChild(i), out, depth + 1);
    }
}
