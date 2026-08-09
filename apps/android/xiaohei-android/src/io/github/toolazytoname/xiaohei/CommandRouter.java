package io.github.toolazytoname.xiaohei;

/** Deterministic P0 command router. Unknown text never becomes a UI action. */
final class CommandRouter {
    enum Action { OPEN_GALLERY, UNKNOWN }

    static Action route(String transcript) {
        String text = transcript == null ? "" : transcript.replace(" ", "");
        return (text.contains("相册") || text.contains("照片") || text.contains("图片"))
            ? Action.OPEN_GALLERY : Action.UNKNOWN;
    }
}
