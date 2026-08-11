#!/usr/bin/env python3
import xml.etree.ElementTree as ET
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
ANDROID = ROOT / "apps" / "android" / "xiaohei-android"
ACTIVITY = ANDROID / "src" / "io" / "github" / "toolazytoname" / "xiaohei" / "ConversationActivity.java"
CLIENT = ANDROID / "src" / "io" / "github" / "toolazytoname" / "xiaohei" / "ConversationClient.java"
COORDINATOR = ANDROID / "src" / "io" / "github" / "toolazytoname" / "xiaohei" / "ConversationSessionCoordinator.java"
MEMORY = ANDROID / "src" / "io" / "github" / "toolazytoname" / "xiaohei" / "MemoryConversationSession.java"
CONTROLS = ANDROID / "src" / "io" / "github" / "toolazytoname" / "xiaohei" / "ConversationControlPolicy.java"
MAIN = ANDROID / "src" / "io" / "github" / "toolazytoname" / "xiaohei" / "MainActivity.java"
MANIFEST = ANDROID / "AndroidManifest.xml"
SCHEMA = ROOT / "contracts" / "conversation-session.v1.schema.json"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL Conversation UI boundary: {message}")


activity = ACTIVITY.read_text(encoding="utf-8")
client = CLIENT.read_text(encoding="utf-8")
coordinator = COORDINATOR.read_text(encoding="utf-8")
memory = MEMORY.read_text(encoding="utf-8")
controls = CONTROLS.read_text(encoding="utf-8")
main = MAIN.read_text(encoding="utf-8")
schema = SCHEMA.read_text(encoding="utf-8")

for identifier in (
    "conversation-authority-notice",
    "conversation-state",
    "conversation-input",
    "conversation-send",
    "conversation-talk",
    "conversation-cancel",
    "conversation-stop",
    "conversation-repeat",
    "conversation-clear",
    "conversation-continue",
    "conversation-end",
    "conversation-output",
):
    require(activity.count(f'"{identifier}"') == 1, f"stable UI identifier {identifier}")

for forbidden in (
    "startActivity(",
    "sendBroadcast(",
    "startService(",
    "startForegroundService(",
    "ActionDispatcher",
    "ToolGateway",
    "ToolCatalog",
    "Runtime.getRuntime",
    "ProcessBuilder",
    "AccessibilityService",
):
    require(forbidden not in activity and forbidden not in client
            and forbidden not in coordinator and forbidden not in memory
            and forbidden not in controls,
            f"forbidden action path {forbidden}")

require("模型没有手机操作、工具、通知、文件或 root 权限" in activity,
        "visible zero-authority notice")
require('"action_authority"' in schema and '"const": "none"' in schema,
        "conversation contract action_authority=none")
require("DEFAULT_MAX_TURNS = 6" in coordinator and "DEFAULT_TIMEOUT_MS = 300000L" in coordinator,
        "bounded 6-turn 5-minute follow-up defaults")
require("begin.messages" in activity and "模型回复中（不能输入下一轮）" in activity,
        "half-duplex UI uses bounded context")
require("onLocked()" in activity and "onBackgrounded()" in activity
        and "checkProfile(" in activity and "END_COMMAND_CLEARED" in activity,
        "lock/background/profile/end clearing wired")
require("ConversationControlPolicy.parse(userText)" in activity
        and "applyControl(ConversationControlPolicy.Action" in activity,
        "text/ASR-ready and button controls share one local path")
require("final int modelCalls;" in controls and "this.modelCalls = 0;" in controls,
        "all conversation controls declare zero model calls")
require("ConversationClient" not in controls and "ConversationPromptPolicy" not in controls,
        "control policy cannot construct model requests")
require("new VoiceCommandSession(this" in activity and "AsrProfile.CONVERSATION" in activity,
        "Talk uses the explicit conversation ASR profile")
partial = activity.index("void onPartialTranscript")
final = activity.index("void onFinalTranscript")
require("send();" not in activity[partial:final] and "send();" in activity[final:],
        "partial is display-only and only final can send")
require("CommandRouter" not in activity and "ActionDispatcher" not in activity,
        "Talk cannot route transcript into commands or actions")
require("ConversationVoiceTurnCoordinator" in activity,
        "Talk has an explicit bounded voice-turn lifecycle")
require("registerAudioDeviceCallback" in activity and "unregisterAudioDeviceCallback" in activity
        and "onAudioRouteChanged" in activity
        and "ConversationControlPolicy.Action.STOP" in activity,
        "audio route changes stop rather than continue an active turn")
require("6 轮半双工（无动作权限）" in main, "public main-screen label matches behavior")

tree = ET.parse(MANIFEST)
namespace = "{http://schemas.android.com/apk/res/android}"
activities = tree.findall("./application/activity")
matches = [node for node in activities if node.get(namespace + "name") == ".ConversationActivity"]
require(len(matches) == 1, "one ConversationActivity manifest declaration")
require(matches[0].get(namespace + "exported") == "false", "ConversationActivity must not be exported")

print("PASS Conversation UI boundary ids=12 exported=false action_paths=0 authority=none half_duplex=6 talk=conversation_profile route_change=stop partial=zero-call")
