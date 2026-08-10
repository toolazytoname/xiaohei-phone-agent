package io.github.toolazytoname.xiaohei;

/** Requires a fresh observed postcondition in addition to a successful adapter result. */
final class ToolOutcomeEvidenceGate {
    enum Decision { PENDING, VERIFIED, ADAPTER_FAILED, STALE_OBSERVATION, POSTCONDITION_MISMATCH, INVALID }
    static final class Observation { final String foregroundPackage; final long snapshot; Observation(String p, long s) { foregroundPackage=p; snapshot=s; } }
    private final String expectedPackage;
    private final long beforeSnapshot;
    private boolean terminal;

    ToolOutcomeEvidenceGate(String expectedPackage, Observation before) {
        this.expectedPackage = expectedPackage;
        this.beforeSnapshot = before == null ? -1 : before.snapshot;
        terminal = !validPackage(expectedPackage) || !valid(before);
    }

    Decision verify(boolean adapterSucceeded, Observation after) {
        if (terminal) return Decision.INVALID;
        if (!adapterSucceeded) { terminal = true; return Decision.ADAPTER_FAILED; }
        if (!valid(after) || after.snapshot <= beforeSnapshot) { terminal = true; return Decision.STALE_OBSERVATION; }
        if (!expectedPackage.equals(after.foregroundPackage)) { terminal = true; return Decision.POSTCONDITION_MISMATCH; }
        terminal = true;
        return Decision.VERIFIED;
    }
    private static boolean valid(Observation value) { return value != null && validPackage(value.foregroundPackage) && value.snapshot >= 0; }
    private static boolean validPackage(String value) { return value != null && value.matches("[A-Za-z][A-Za-z0-9_.]{0,254}"); }
}
