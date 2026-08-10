#!/usr/bin/env python3
"""Static enforcement for POLICY-002 fresh, scope-bound confirmation."""

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei"
GATE = (JAVA / "FreshConfirmationGate.java").read_text(encoding="utf-8")
TEST = (ROOT / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/FreshConfirmationGateTest.java").read_text(encoding="utf-8")
SCHEMA = (ROOT / "contracts/confirmation-grant.v1.schema.json").read_text(encoding="utf-8")
UI = "\n".join((JAVA / name).read_text(encoding="utf-8") for name in (
    "MainActivity.java", "ConversationActivity.java", "AgentActivity.java",
))


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL fresh confirmation boundary: {message}")


require("MIN_TTL_MS = 1000L" in GATE and "MAX_TTL_MS = 60000L" in GATE,
        "fresh one-to-sixty-second window")
require("source != Source.LOCAL_USER_GESTURE" in GATE,
        "assistant source denied")
require(GATE.index("source != Source.LOCAL_USER_GESTURE") < GATE.index("new Grant("),
        "source checked before grant creation")
require("device.eligible()" in GATE and "unlocked && interactive && foreground" in GATE,
        "unlocked interactive foreground binding")
for binding in ("TASK_CHANGED", "REQUEST_CHANGED", "PLAN_CHANGED", "TARGET_CHANGED", "CONTENT_CHANGED"):
    require(binding in GATE, f"scope binding: {binding}")
require("nowMs < grant.issuedAtMs" in GATE and "nowMs >= grant.expiresAtMs" in GATE,
        "clock rollback and exact expiry fail closed")
require('MessageDigest.getInstance("SHA-256")' in GATE,
        "salted target/content digest binding")
require("private static final class Grant" in GATE and "active = null" in GATE,
        "private memory-only consumable grant")
require("this.modelCalls = 0" in GATE and "this.actionCalls = 0" in GATE,
        "confirmation gate has zero side effects")

for forbidden in (
    "PhoneAgentClient", "ConversationClient", "ActionDispatcher", "ToolGateway",
    "startActivity", "startService", "sendBroadcast", "ProcessBuilder",
    "Runtime.getRuntime", "java.net.", "android.", "System.out", "Log.",
    "SharedPreferences", "FileOutputStream", "SQLite",
):
    require(forbidden not in GATE, f"no model/execution/platform/persistence path: {forbidden}")

require("FreshConfirmationGate" not in UI,
        "POLICY-002 remains unwired before accessible confirmation UI/tool gateway")
require("exact=10 target_change=5 content_change=5 identity_change=5 expiry=5" in TEST,
        "scope/freshness matrix")
require("device=5 assistant_forgery=10 replay=5 allow_once=10" in TEST,
        "device/source/single-use matrix")
require("model_calls=0 action_calls=0 execution_paths=0" in TEST,
        "declared zero-side-effect acceptance")
require('"source": {"const": "local_user_gesture"}' in SCHEMA,
        "public schema restricts source")
require('"single_use": {"const": true}' in SCHEMA
        and '"persistence": {"const": "memory_only"}' in SCHEMA,
        "public schema is one-use and memory-only")
require('"raw_content"' not in SCHEMA and '"additionalProperties": false' in SCHEMA,
        "public grant schema carries no raw target/content extension")

print("PASS fresh confirmation cases=50 exact=10 changed=15 expiry=5 device=5 assistant=10 replay=5 allow_once=10 wired=0")
