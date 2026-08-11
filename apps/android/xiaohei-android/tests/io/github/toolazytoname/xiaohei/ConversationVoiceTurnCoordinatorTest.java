package io.github.toolazytoname.xiaohei;

public final class ConversationVoiceTurnCoordinatorTest {
    public static void main(String[] args) {
        ConversationVoiceTurnCoordinator turn = new ConversationVoiceTurnCoordinator();
        require(turn.state() == ConversationVoiceTurnCoordinator.State.IDLE, "initial idle");
        require(!turn.beginThinking(), "cannot skip listening");
        require(turn.beginListening(), "start first turn");
        require(turn.partial(), "partial only while listening");
        require(turn.finalTranscript(), "final enters review");
        require(!turn.partial(), "late partial rejected");
        require(turn.beginThinking(), "review sends once");
        require(!turn.beginListening(), "cannot listen while thinking");
        require(turn.beginSpeaking(), "reply starts speech");
        require(turn.speechFinished(), "speech waits explicitly");
        require(turn.beginListening(), "follow-up needs explicit start");
        require(turn.stop(), "stop releases active turn");
        require(!turn.stop(), "terminal stop idempotent");
        require(turn.reset(), "reset explicit");
        require(turn.beginListening(), "listen after reset");
        require(turn.fail(), "failure terminal");
        require(!turn.beginListening(), "failure does not auto-restart");
        System.out.println("PASS conversation-voice-turn legal_transitions=12 late_callbacks=reject");
    }
    private static void require(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
}
