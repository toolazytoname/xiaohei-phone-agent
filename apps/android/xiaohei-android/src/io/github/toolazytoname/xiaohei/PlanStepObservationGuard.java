package io.github.toolazytoname.xiaohei;

/**
 * Fails closed when a planned action is not followed by a fresh, expected foreground observation.
 * It carries package metadata only; it never captures screen text, trees, images, or media.
 */
final class PlanStepObservationGuard {
    enum State { READY, AWAITING_POSTCONDITION, HALTED }
    enum Decision {
        STEP_ACCEPTED,
        POSTCONDITION_ACCEPTED,
        INVALID_STEP,
        PRECONDITION_MISMATCH,
        ACTION_FAILED,
        POSTCONDITION_MISMATCH,
        INVALID_OBSERVATION,
        TERMINAL
    }

    static final class Observation {
        final String foregroundPackage;
        final long sequence;

        Observation(String foregroundPackage, long sequence) {
            this.foregroundPackage = foregroundPackage;
            this.sequence = sequence;
        }
    }

    private State state = State.READY;
    private String expectedAfterPackage;
    private long acceptedSequence = -1L;

    State state() { return state; }

    Decision beginStep(String stepId, String expectedBeforePackage, String expectedAfter,
            Observation before) {
        if (state == State.HALTED || state == State.AWAITING_POSTCONDITION) return Decision.TERMINAL;
        if (!validId(stepId) || !validPackage(expectedBeforePackage) || !validPackage(expectedAfter)
                || !validObservation(before) || !expectedBeforePackage.equals(before.foregroundPackage)) {
            state = State.HALTED;
            return validObservation(before) ? Decision.PRECONDITION_MISMATCH : Decision.INVALID_OBSERVATION;
        }
        expectedAfterPackage = expectedAfter;
        acceptedSequence = before.sequence;
        state = State.AWAITING_POSTCONDITION;
        return Decision.STEP_ACCEPTED;
    }

    Decision recordActionResult(boolean success) {
        if (state != State.AWAITING_POSTCONDITION) return Decision.TERMINAL;
        if (!success) {
            state = State.HALTED;
            return Decision.ACTION_FAILED;
        }
        return Decision.STEP_ACCEPTED;
    }

    Decision verifyPostcondition(Observation after) {
        if (state != State.AWAITING_POSTCONDITION) return Decision.TERMINAL;
        if (!validObservation(after) || after.sequence <= acceptedSequence) {
            state = State.HALTED;
            return Decision.INVALID_OBSERVATION;
        }
        if (!expectedAfterPackage.equals(after.foregroundPackage)) {
            state = State.HALTED;
            return Decision.POSTCONDITION_MISMATCH;
        }
        expectedAfterPackage = null;
        acceptedSequence = after.sequence;
        state = State.READY;
        return Decision.POSTCONDITION_ACCEPTED;
    }

    private static boolean validObservation(Observation value) {
        return value != null && validPackage(value.foregroundPackage) && value.sequence >= 0L;
    }

    private static boolean validPackage(String value) {
        return value != null && value.matches("[A-Za-z][A-Za-z0-9_.]{0,254}");
    }

    private static boolean validId(String value) {
        return value != null && value.matches("[A-Za-z0-9][A-Za-z0-9._:-]{0,63}");
    }
}
