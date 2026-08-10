#!/usr/bin/env python3
"""Static enforcement for CHAT-008 prompt minimization and inert model text."""

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei"
POLICY = (JAVA / "ConversationPromptPolicy.java").read_text(encoding="utf-8")
CLIENT = (JAVA / "ConversationClient.java").read_text(encoding="utf-8")
ACTIVITY = (JAVA / "ConversationActivity.java").read_text(encoding="utf-8")
TEST = (ROOT / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/ConversationPromptPolicyTest.java").read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL Conversation prompt boundary: {message}")


require('VERSION = "xiaohei-conversation-system.v1"' in POLICY, "versioned static prompt")
require('ACTION_AUTHORITY = "none"' in POLICY, "zero action authority")
require("static final String SYSTEM_PROMPT" in POLICY, "one static system prompt")
require("ConversationPromptPolicy.build(source)" in CLIENT, "client uses policy envelope")
require("envelope.messages" in CLIENT and "message.role.apiName" in CLIENT,
        "client serializes fixed roles from envelope")
require('put("role", "system")' not in CLIENT and "You are Xiaohei conversation" not in CLIENT,
        "client cannot maintain a second ad-hoc system prompt")

for forbidden in (
    "Settings.Secure",
    "Build.SERIAL",
    "ANDROID_ID",
    "AccountManager",
    "LocationManager",
    "NotificationManager",
    "TelephonyManager",
    "getInstalledPackages",
    "getLastKnownLocation",
    "SecureSecretStore",
    "SharedPreferences",
):
    require(forbidden not in POLICY, f"prompt policy has no dynamic private context: {forbidden}")

for forbidden in (
    "ActionDispatcher",
    "CommandRouter",
    "ToolGateway",
    "ToolCatalog",
    "PhoneAgentClient",
    "ProcessBuilder",
    "Runtime.getRuntime",
    "startActivity(",
    "startService(",
    "sendBroadcast(",
):
    require(forbidden not in POLICY and forbidden not in CLIENT and forbidden not in ACTIVITY,
            f"prompt/reply path has no action interpreter: {forbidden}")

require("INJECTIONS" in TEST and "FORGERIES" in TEST and "sensitiveUserTextIsNotPromoted" in TEST,
        "adversarial role and privacy matrix present")
require("injections=20 forgeries=10 sensitive=5" in TEST, "declared adversarial case counts")
require("SafeMetadata" in POLICY and "String prompt" not in POLICY[POLICY.index("class SafeMetadata"):POLICY.index("SafeMetadata(")],
        "public-safe metadata carries no prompt text")

print("PASS Conversation prompt boundary version=v1 injections=20 forgeries=10 sensitive=5 action_paths=0")
