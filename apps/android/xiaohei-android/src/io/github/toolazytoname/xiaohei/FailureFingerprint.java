package io.github.toolazytoname.xiaohei;

/** In-memory failure dedupe; it grants at most one recovery after evidence changes. */
final class FailureFingerprint {
    enum RecoveryDecision { RECORDED, UNCHANGED_DENIED, RECOVERY_GRANTED, RECOVERY_ALREADY_USED, INVALID }

    static final class RecoveryGate {
        private String latest;
        private boolean recoveryUsed;

        RecoveryDecision recordFailure(String fingerprint) {
            if (!valid(fingerprint)) return RecoveryDecision.INVALID;
            if (fingerprint.equals(latest)) return RecoveryDecision.UNCHANGED_DENIED;
            latest = fingerprint;
            return RecoveryDecision.RECORDED;
        }

        RecoveryDecision requestRecovery(String evidenceFingerprint) {
            if (!valid(latest) || !valid(evidenceFingerprint)) return RecoveryDecision.INVALID;
            if (recoveryUsed) return RecoveryDecision.RECOVERY_ALREADY_USED;
            if (latest.equals(evidenceFingerprint)) return RecoveryDecision.UNCHANGED_DENIED;
            latest = evidenceFingerprint;
            recoveryUsed = true;
            return RecoveryDecision.RECOVERY_GRANTED;
        }

        boolean recoveryUsed() { return recoveryUsed; }
    }

    static String of(String task, String tool, String target, String condition, String error) {
        return clean(task)+"|"+clean(tool)+"|"+clean(target)+"|"+clean(condition)+"|"+clean(error);
    }
    static boolean canRecover(String prior, String next, boolean recoveryAlreadyUsed) {
        return !recoveryAlreadyUsed && prior != null && !prior.equals(next);
    }
    private static boolean valid(String value) { return value != null && !value.isEmpty() && value.length() <= 512; }
    private static String clean(String value) { return value == null ? "" : value.trim().replace("|", "_"); }
}
