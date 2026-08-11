#!/usr/bin/env python3
"""Static gate for bounded, explicit Conversation voice follow-ups."""

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei"
ACTIVITY = (JAVA / "ConversationActivity.java").read_text(encoding="utf-8")
COORDINATOR = (JAVA / "ConversationSessionCoordinator.java").read_text(encoding="utf-8")
CONTROLS = (JAVA / "ConversationControlPolicy.java").read_text(encoding="utf-8")
SESSION_TEST = (ROOT / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/ConversationSessionCoordinatorTest.java").read_text(encoding="utf-8")
CONTROL_TEST = (ROOT / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/ConversationControlPolicyTest.java").read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL Conversation follow-up: {message}")


def method(source: str, signature: str, end_signature: str) -> str:
    start = source.index(signature)
    end = source.index(end_signature, start + len(signature))
    return source[start:end]


set_running = method(ACTIVITY, "    private void setRunning", "    private void initializeSystemTtsIfEnabled")
apply_control = method(ACTIVITY, "    private void applyControl", "    private void scheduleTimeout")
on_resume = method(ACTIVITY, "    @Override protected void onResume()", "    @Override protected void onStop()")
tts_callback = method(ACTIVITY, "        systemTts.initialize", "    private void speakReply")

require("DEFAULT_MAX_TURNS = 6" in COORDINATOR
        and "DEFAULT_TOKEN_BUDGET = 2048" in COORDINATOR
        and "DEFAULT_TIMEOUT_MS = 300000L" in COORDINATOR,
        "one bounded 6-turn/2048-token/5-minute session contract")
require("voiceTurn.state() == ConversationVoiceTurnCoordinator.State.WAITING_FOLLOWUP" in set_running
        and "Continue talking" in set_running,
        "Continue talking is visible only at the explicit follow-up state")
require("talk.setEnabled(!running && controls.canSend() && voiceSession == null)" in set_running,
        "continue control cannot overlap request or current recording")
require("ConversationControlPolicy.parse(userText)" in ACTIVITY
        and "applyControl(ConversationControlPolicy.Action" in ACTIVITY,
        "typed and spoken final control text share one exact local policy")
for action in ("STOP", "REPEAT", "CLEAR", "CONTINUE", "END"):
    require(f"case {action}:" in apply_control, f"visible local handling for {action}")
require("modelCalls = 0" in CONTROLS and "equalsAny(value" in CONTROLS,
        "controls are exact and explicitly zero-model-call")
require("startVoiceTurn()" not in tts_callback,
        "TTS completion cannot begin a follow-up recording")
require("checkProfile(currentProfileFingerprint())" in on_resume
        and "PROFILE_CHANGED_CLEARED" in on_resume,
        "profile changes clear shared context before a new turn")
require("ConversationSessionCoordinator.BeginResult begin = coordinator.begin(" in ACTIVITY
        and "ConversationSessionCoordinator.Code completion = coordinator.complete(" in ACTIVITY,
        "voice and text both use one Conversation session coordinator")
for expected in ("carriesReferentialContext", "enforcesHalfDuplex", "modelSwitchClearsBeforeRequest",
                 "clearsAtConfiguredTurnLimit", "totalTimeoutClears"):
    require(expected in SESSION_TEST, f"shared-session unit case: {expected}")
require("phrases=23" in CONTROL_TEST and "model_calls=0" in CONTROL_TEST,
        "control test retains exact local zero-call coverage")

print("PASS Conversation follow-up context=shared bounds=6/2048/5m controls=local_zero_call "
      "continue=explicit tts_auto_listen=off profile_change=clear")
