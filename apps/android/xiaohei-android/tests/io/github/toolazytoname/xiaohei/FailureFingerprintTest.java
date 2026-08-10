package io.github.toolazytoname.xiaohei;

public final class FailureFingerprintTest {
    public static void main(String[] args) {
        String first = FailureFingerprint.of("task-1", "android.observe", "settings", "v1", "timeout");
        String changed = FailureFingerprint.of("task-1", "android.observe", "launcher", "v2", "timeout");
        require(!FailureFingerprint.canRecover(first, first, false), "same evidence denied");
        require(FailureFingerprint.canRecover(first, changed, false), "changed evidence eligible");
        require(!FailureFingerprint.canRecover(first, changed, true), "used recovery denied");

        FailureFingerprint.RecoveryGate gate = new FailureFingerprint.RecoveryGate();
        require(gate.recordFailure(first) == FailureFingerprint.RecoveryDecision.RECORDED, "first failure recorded");
        require(gate.recordFailure(first) == FailureFingerprint.RecoveryDecision.UNCHANGED_DENIED, "same failure deduped");
        require(gate.requestRecovery(first) == FailureFingerprint.RecoveryDecision.UNCHANGED_DENIED, "unchanged recovery denied");
        require(gate.requestRecovery(changed) == FailureFingerprint.RecoveryDecision.RECOVERY_GRANTED, "one changed recovery granted");
        require(gate.recoveryUsed(), "recovery marked used");
        require(gate.requestRecovery(FailureFingerprint.of("task-1", "android.observe", "settings", "v3", "ok"))
                == FailureFingerprint.RecoveryDecision.RECOVERY_ALREADY_USED, "second recovery denied");
        require(gate.recordFailure(null) == FailureFingerprint.RecoveryDecision.INVALID, "invalid rejected");
        System.out.println("PASS failure-fingerprint unchanged_retry=reject evidence_change=required recovery_once=true second_recovery=reject");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
