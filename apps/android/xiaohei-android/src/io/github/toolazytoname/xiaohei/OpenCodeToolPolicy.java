package io.github.toolazytoname.xiaohei;

import java.util.Locale;

/** Closed policy for future OpenCode adapters. It classifies command intent but never runs it. */
final class OpenCodeToolPolicy {
    enum Decision { ALLOW_PROJECT_SUMMARY, ALLOW_TEST_DIAGNOSIS, ALLOW_CONTROLLED_ORGANIZATION, DENY }
    enum Reason { ALLOWED, EMPTY, ROOT, SENSITIVE_PATH, DESTRUCTIVE_GIT, NETWORK, SHELL_ESCAPE, UNKNOWN }
    static final class Result { final Decision decision; final Reason reason; final int modelCalls = 0; final int executionCalls = 0;
        Result(Decision d, Reason r) { decision=d; reason=r; } }
    private OpenCodeToolPolicy() {}
    static Result evaluate(OpenCodeTaskProtocol.Kind kind, String intent) {
        if (kind == null || intent == null || intent.trim().isEmpty()) return deny(Reason.EMPTY);
        String text = intent.toLowerCase(Locale.ROOT);
        if (has(text, "su ", "sudo", "root", "magisk", "adb root")) return deny(Reason.ROOT);
        if (has(text, "/data/", "/system", "/vendor", "/proc", "/dev/", "/sdcard", ".ssh", "id_rsa", "keychain", "token", "credential")) return deny(Reason.SENSITIVE_PATH);
        if (has(text, "git reset", "git clean", "git push", "git commit", "git rebase", "git checkout", "rm ", "delete ", "删除", "格式化")) return deny(Reason.DESTRUCTIVE_GIT);
        if (has(text, "curl", "wget", "http://", "https://", "upload", "下载", "上传", "联网")) return deny(Reason.NETWORK);
        if (has(text, "sh -c", "bash -c", "|", ";", "&&", "`", "$ (".replace(" ", ""))) return deny(Reason.SHELL_ESCAPE);
        if (kind == OpenCodeTaskProtocol.Kind.PROJECT_SUMMARY && has(text, "summary", "总结", "摘要")) return allow(Decision.ALLOW_PROJECT_SUMMARY);
        if (kind == OpenCodeTaskProtocol.Kind.TEST_DIAGNOSIS && has(text, "test", "测试", "诊断", "失败")) return allow(Decision.ALLOW_TEST_DIAGNOSIS);
        if (kind == OpenCodeTaskProtocol.Kind.CONTROLLED_FILE_ORGANIZATION && has(text, "organize", "整理", "归类")) return allow(Decision.ALLOW_CONTROLLED_ORGANIZATION);
        return deny(Reason.UNKNOWN);
    }
    private static Result allow(Decision value) { return new Result(value, Reason.ALLOWED); }
    private static Result deny(Reason value) { return new Result(Decision.DENY, value); }
    private static boolean has(String text, String... values) { for (String value : values) if (text.contains(value)) return true; return false; }
}
