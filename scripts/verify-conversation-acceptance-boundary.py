#!/usr/bin/env python3
"""Static enforcement for the automated CHAT-012 acceptance boundary."""

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei"
POLICY = (JAVA / "ConversationPrivacyPolicy.java").read_text(encoding="utf-8")
ACTIVITY = (JAVA / "ConversationActivity.java").read_text(encoding="utf-8")
CLIENT = (JAVA / "ConversationClient.java").read_text(encoding="utf-8")
MATRIX = (ROOT / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/ConversationAcceptanceMatrixTest.java").read_text(encoding="utf-8")
STATUS = (ROOT / "STATUS.md").read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL Conversation acceptance boundary: {message}")


for category in ("NOTIFICATIONS", "CONTACTS", "LOCATION", "PRIVATE_MEDIA", "CREDENTIALS"):
    require(category in POLICY, f"privacy category: {category}")
require(POLICY.count("case \"") == 15, "15 exact bilingual privacy phrases")
require("this.modelCalls = 0" in POLICY and "this.actionCalls = 0" in POLICY,
        "privacy denials are zero model/action")
require("【本地隐私拒绝｜零模型调用】" in POLICY and "LOCAL PRIVACY DENIAL | ZERO MODEL CALLS" in POLICY,
        "privacy denial source labels")

for forbidden in (
    "import android.",
    "ConversationClient",
    "BoundedConversationTransport",
    "SecureSecretStore",
    "SharedPreferences",
    "CommandRouter",
    "ActionDispatcher",
    "ToolGateway",
    "ProcessBuilder",
    "Runtime.getRuntime",
    "java.net.",
    "java.io.",
):
    require(forbidden not in POLICY, f"privacy policy has no external/action path: {forbidden}")

privacy_check = ACTIVITY.index("ConversationPrivacyPolicy.evaluate(userText)")
session_begin = ACTIVITY.index("coordinator.begin(")
model_ask = ACTIVITY.index("ConversationClient.ask(")
require(privacy_check < session_begin < model_ask,
        "privacy denial happens before session/model request")
require("状态：本地隐私拒绝；零模型/动作调用" in ACTIVITY,
        "visible local privacy state")

for source in (ACTIVITY, CLIENT, POLICY):
    for audio_api in ("AudioRecord", "MediaRecorder", "startRecording", "RECORD_AUDIO"):
        require(audio_api not in source, f"Conversation path owns no recorder API: {audio_api}")

require("questions=20 interruptions=5 timeouts=5 privacy_denials=5" in MATRIX,
        "exact automated acceptance counts")
require("真人确认中文 TTS 可懂、打断自然且无录音残留" in STATUS,
        "human speech/TTS gate remains explicit")

print("PASS Conversation acceptance boundary questions=20 interruptions=5 timeouts=5 privacy=5 recorder_apis=0 human_gate=retained")
