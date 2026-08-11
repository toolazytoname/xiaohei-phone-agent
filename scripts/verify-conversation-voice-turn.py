#!/usr/bin/env python3
"""Static gate for the safe, explicit single Conversation voice turn.

This intentionally proves wiring and ordering only.  It cannot prove real ASR,
remote replies, audible TTS, or release of platform resources on a phone.
"""

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei"
ACTIVITY = (JAVA / "ConversationActivity.java").read_text(encoding="utf-8")
VOICE_SESSION = (JAVA / "VoiceCommandSession.java").read_text(encoding="utf-8")
LOCAL_ASR = (JAVA / "XiaoheiRecognitionService.java").read_text(encoding="utf-8")
TURN = (JAVA / "ConversationVoiceTurnCoordinator.java").read_text(encoding="utf-8")
TURN_TEST = (ROOT / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/ConversationVoiceTurnCoordinatorTest.java").read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL Conversation voice turn: {message}")


def method(source: str, signature: str, end_signature: str) -> str:
    start = source.index(signature)
    end = source.index(end_signature, start + len(signature))
    return source[start:end]


start_turn = method(ACTIVITY, "    private void startVoiceTurn()", "    private void cancelCurrent()")
final_callback = method(start_turn, "            @Override public void onFinalTranscript", "            @Override public void onSpeechError")
partial_callback = method(start_turn, "            @Override public void onPartialTranscript", "            @Override public void onFinalTranscript")
tts_callback = method(ACTIVITY, "        systemTts.initialize", "    private void speakReply")
on_stop = method(ACTIVITY, "    @Override protected void onStop()", "    @Override protected void onDestroy()")
on_destroy = method(ACTIVITY, "    @Override protected void onDestroy()", "    private void stopVoiceListening()")
global_stop = method(ACTIVITY, "    private boolean stopForGlobalRequest()", "    private String currentProfileFingerprint()")
on_results = method(VOICE_SESSION, "    @Override public void onResults", "    @Override public void onPartialResults")
recognize_finally = method(LOCAL_ASR, "        } finally {", "    private static Bundle results")

require(start_turn.index('interruptSpeech("开始听取前已停止播报') < start_turn.index("new VoiceCommandSession"),
        "Talk must interrupt output before constructing the input session")
require("AsrProfile.CONVERSATION" in start_turn,
        "Talk must select the explicit Conversation ASR profile")
require("controls.requestInFlight() || pending != null" in start_turn,
        "Talk must reject an in-flight request")
require("if (!voiceTurn.beginListening())" in start_turn,
        "Talk must obtain a legal voice-turn state before recording")
require("voiceSession.isAvailable()" in start_turn and "voiceSession.start();" in start_turn,
        "Talk must fail visibly rather than silently switch ASR providers")
require("send();" not in partial_callback and "input.setText(text);" in partial_callback,
        "partial transcript must be display-only")
require("voiceSession = null;" in final_callback and "voiceTurn.finalTranscript()" in final_callback
        and "send();" in final_callback,
        "a final transcript must pass the one-way turn transition before send")
require(on_results.index("stop();") < on_results.index("listener.onFinalTranscript(text)"),
        "recognizer must release session ownership before delivering final text")
require("ProcessAudioDuplex.shared().release(inputLease);" in recognize_finally
        and "audio.release();" in recognize_finally,
        "local ASR must release its lease and recorder in finally")
require("beginThinking()" in ACTIVITY and "beginSpeaking()" in ACTIVITY
        and "speechFinished()" in ACTIVITY,
        "final → model → speech must use the explicit coordinator")
require("startVoiceTurn()" not in tts_callback,
        "TTS completion must not automatically reopen the microphone")
require("WAITING_FOLLOWUP" in ACTIVITY and "Continue talking" in ACTIVITY,
        "follow-up must be visible and explicit")
require("stopVoiceListening();" in on_stop and "stopVoiceListening();" in on_destroy
        and "stopVoiceListening();" in global_stop,
        "lifecycle and global stop must end active voice input")
require("IDLE, LISTENING, REVIEWING, THINKING, SPEAKING, WAITING_FOLLOWUP, STOPPED, FAILED" in TURN,
        "voice-turn state set must remain bounded")
for expected in ("cannot skip listening", "late partial rejected", "cannot listen while thinking",
                 "follow-up needs explicit start", "failure does not auto-restart"):
    require(expected in TURN_TEST, f"unit test retains transition case: {expected}")

print("PASS Conversation voice turn talk=explicit partial=zero_send final=one_way "
      "input_output=leased tts_auto_listen=off lifecycle_stop=covered")
