package io.github.toolazytoname.xiaohei;
final class FailureFingerprint {
    static String of(String task, String tool, String target, String condition, String error) {
        return clean(task)+"|"+clean(tool)+"|"+clean(target)+"|"+clean(condition)+"|"+clean(error);
    }
    static boolean canRecover(String prior, String next, boolean recoveryAlreadyUsed) {
        return !recoveryAlreadyUsed && prior != null && !prior.equals(next);
    }
    private static String clean(String value) { return value == null ? "" : value.trim().replace("|", "_"); }
}
