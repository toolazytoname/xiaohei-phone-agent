package io.github.toolazytoname.xiaohei;

public final class AudioInterruptionPolicyTest {
    public static void main(String[] args) {
        for (AudioInterruptionPolicy.Source source : AudioInterruptionPolicy.Source.values()) {
            AudioInterruptionPolicy.Result result = AudioInterruptionPolicy.decide(source, true, false);
            require(result.decision == AudioInterruptionPolicy.Decision.STOP_AND_RELEASE
                    && result.stopInput && result.stopOutput && result.releaseOwnership && !result.autoResume,
                    "all sources release input");
            result = AudioInterruptionPolicy.decide(source, false, true);
            require(result.decision == AudioInterruptionPolicy.Decision.STOP_AND_RELEASE
                    && !result.autoResume, "all sources release output");
        }
        require(AudioInterruptionPolicy.decide(AudioInterruptionPolicy.Source.CALL, false, false).decision
                == AudioInterruptionPolicy.Decision.IGNORE, "idle ignored");
        require(AudioInterruptionPolicy.decide(null, true, true).decision
                == AudioInterruptionPolicy.Decision.IGNORE, "unknown ignored");
        System.out.println("PASS audio-interruption call=2 alarm=2 media=2 activity=2 release=input+output resume=0 unknown=ignore");
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
