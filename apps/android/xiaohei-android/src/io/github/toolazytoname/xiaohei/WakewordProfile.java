package io.github.toolazytoname.xiaohei;

/** User-visible configuration; no OEM model bytes or endpoint data live here. */
final class WakewordProfile {
    final String id;
    final String label;
    final String backend;
    final String action;

    WakewordProfile(String id, String label, String backend, String action) {
        this.id = id;
        this.label = label;
        this.backend = backend;
        this.action = action;
    }

    static WakewordProfile baseManualGallery() {
        return new WakewordProfile(
            "base-manual-gallery",
            "基础模式（手动事件）",
            "app_button",
            "open_gallery"
        );
    }
}
