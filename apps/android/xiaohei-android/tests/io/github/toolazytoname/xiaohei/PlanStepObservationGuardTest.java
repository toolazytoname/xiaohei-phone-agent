package io.github.toolazytoname.xiaohei;

public final class PlanStepObservationGuardTest {
    public static void main(String[] args) {
        acceptsFreshExpectedPostcondition();
        rejectsAppSwitchRaceAndAllFurtherSteps();
        rejectsStaleAndInvalidObservations();
        rejectsActionFailureWithoutRecoveryLoop();
        System.out.println("PASS plan-step-observation accepted=1 app_switch=1 stale=2 invalid=2 action_failure=1 out_of_scope=0");
    }

    private static void acceptsFreshExpectedPostcondition() {
        PlanStepObservationGuard guard = new PlanStepObservationGuard();
        require(guard.beginStep("s1", "com.android.launcher", "com.android.settings", observation("com.android.launcher", 10))
                == PlanStepObservationGuard.Decision.STEP_ACCEPTED, "begin valid step");
        require(guard.recordActionResult(true) == PlanStepObservationGuard.Decision.STEP_ACCEPTED,
                "action recorded");
        require(guard.verifyPostcondition(observation("com.android.settings", 11))
                == PlanStepObservationGuard.Decision.POSTCONDITION_ACCEPTED, "fresh expected package");
        require(guard.state() == PlanStepObservationGuard.State.READY, "ready after re-observation");
    }

    private static void rejectsAppSwitchRaceAndAllFurtherSteps() {
        PlanStepObservationGuard guard = new PlanStepObservationGuard();
        guard.beginStep("s1", "com.android.launcher", "com.android.settings", observation("com.android.launcher", 20));
        guard.recordActionResult(true);
        require(guard.verifyPostcondition(observation("com.android.gallery3d", 21))
                == PlanStepObservationGuard.Decision.POSTCONDITION_MISMATCH, "external app switch halts");
        require(guard.beginStep("s2", "com.android.gallery3d", "com.android.settings", observation("com.android.gallery3d", 22))
                == PlanStepObservationGuard.Decision.TERMINAL, "no following action after race");
    }

    private static void rejectsStaleAndInvalidObservations() {
        PlanStepObservationGuard stale = new PlanStepObservationGuard();
        stale.beginStep("s1", "com.android.launcher", "com.android.settings", observation("com.android.launcher", 30));
        require(stale.verifyPostcondition(observation("com.android.settings", 30))
                == PlanStepObservationGuard.Decision.INVALID_OBSERVATION, "stale sequence rejected");
        PlanStepObservationGuard invalid = new PlanStepObservationGuard();
        require(invalid.beginStep("s1", "com.android.launcher", "com.android.settings", observation("bad package", 1))
                == PlanStepObservationGuard.Decision.INVALID_OBSERVATION, "invalid package rejected");
    }

    private static void rejectsActionFailureWithoutRecoveryLoop() {
        PlanStepObservationGuard guard = new PlanStepObservationGuard();
        guard.beginStep("s1", "com.android.launcher", "com.android.settings", observation("com.android.launcher", 40));
        require(guard.recordActionResult(false) == PlanStepObservationGuard.Decision.ACTION_FAILED,
                "failed action halts");
        require(guard.recordActionResult(true) == PlanStepObservationGuard.Decision.TERMINAL,
                "no implicit retry");
    }

    private static PlanStepObservationGuard.Observation observation(String pkg, long sequence) {
        return new PlanStepObservationGuard.Observation(pkg, sequence);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
