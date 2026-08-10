#!/usr/bin/env python3
import xml.etree.ElementTree as ET
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
ANDROID = ROOT / "apps" / "android" / "xiaohei-android"
ACTIVITY = ANDROID / "src" / "io" / "github" / "toolazytoname" / "xiaohei" / "ConversationActivity.java"
CLIENT = ANDROID / "src" / "io" / "github" / "toolazytoname" / "xiaohei" / "ConversationClient.java"
MANIFEST = ANDROID / "AndroidManifest.xml"
SCHEMA = ROOT / "contracts" / "conversation-session.v1.schema.json"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL Conversation UI boundary: {message}")


activity = ACTIVITY.read_text(encoding="utf-8")
client = CLIENT.read_text(encoding="utf-8")
schema = SCHEMA.read_text(encoding="utf-8")

for identifier in (
    "conversation-authority-notice",
    "conversation-state",
    "conversation-input",
    "conversation-send",
    "conversation-cancel",
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
    require(forbidden not in activity and forbidden not in client, f"forbidden action path {forbidden}")

require("模型没有手机操作、工具、通知、文件或 root 权限" in activity,
        "visible zero-authority notice")
require('"action_authority"' in schema and '"const": "none"' in schema,
        "conversation contract action_authority=none")

tree = ET.parse(MANIFEST)
namespace = "{http://schemas.android.com/apk/res/android}"
activities = tree.findall("./application/activity")
matches = [node for node in activities if node.get(namespace + "name") == ".ConversationActivity"]
require(len(matches) == 1, "one ConversationActivity manifest declaration")
require(matches[0].get(namespace + "exported") == "false", "ConversationActivity must not be exported")

print("PASS Conversation UI boundary ids=6 exported=false action_paths=0 authority=none")
