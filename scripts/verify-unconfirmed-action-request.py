#!/usr/bin/env python3
"""Static enforcement for ROUTE-004 typed, pending ActionRequest creation."""

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei"
BOUNDARY = (JAVA / "UnconfirmedActionRequest.java").read_text(encoding="utf-8")
TEST = (ROOT / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/UnconfirmedActionRequestTest.java").read_text(encoding="utf-8")
SCHEMA = (ROOT / "contracts/action-request.v1.schema.json").read_text(encoding="utf-8")
MAIN = (JAVA / "MainActivity.java").read_text(encoding="utf-8")
CONVERSATION = (JAVA / "ConversationActivity.java").read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL unconfirmed action request: {message}")


require('SCHEMA_VERSION = 1' in BOUNDARY and 'TARGET = "local_service"' in BOUNDARY,
        "schema-v1 local boundary")
require('ACTION = "plan_complex_task"' in BOUNDARY and 'RISK = "high"' in BOUNDARY,
        "fixed action and conservative unreviewed risk")
require('CONFIRMATION_STATE = "pending"' in BOUNDARY and 'this.dryRun = true' in BOUNDARY,
        "creation can only be pending dry-run")
require('source.role != MemoryConversationSession.Role.USER' in BOUNDARY,
        "typed assistant source rejected")
require(BOUNDARY.index('source.role != MemoryConversationSession.Role.USER')
        < BOUNDARY.index('RouteClarificationPolicy.decide(userText)'),
        "assistant text rejected before interpretation")
require('decision.route != IntentRouteClassifier.Route.COMPLEX_TASK' in BOUNDARY,
        "only explicit complex-task routes upgrade")
require('private Request(' in BOUNDARY and 'final String confirmationState' in BOUNDARY,
        "request cannot be directly constructed or mutated")
require('"confirmed"' not in BOUNDARY,
        "production creation boundary contains no confirmed-state literal")
require('this.modelCalls = 0' in BOUNDARY and 'this.actionCalls = 0' in BOUNDARY,
        "boundary records zero side effects")
require('publicLogSafe = false' in BOUNDARY and 'parameters.user_text' in BOUNDARY,
        "raw user request is marked sensitive")

for forbidden in (
    "ActionDispatcher", "PhoneAgentClient", "ConversationClient", "ToolGateway",
    "startActivity", "startService", "sendBroadcast", "ProcessBuilder",
    "Runtime.getRuntime", "java.net.", "android.", "System.out", "Log.",
):
    require(forbidden not in BOUNDARY, f"no execution/model/log path: {forbidden}")

require("UnconfirmedActionRequest" not in MAIN and "UnconfirmedActionRequest" not in CONVERSATION,
        "foundation remains unwired before planning/policy gates")
require("created=10 assistant_forgery=10 non_complex=10 clarification=5 invalid_metadata=4" in TEST,
        "exact 39-case synthetic matrix")
require("confirmed=0 model_calls=0 action_calls=0 execution_paths=0" in TEST,
        "declared no-confirmation and zero-side-effect acceptance")
require('"const": "pending"' in SCHEMA and '"const": true' in SCHEMA,
        "public schema constrains pending confirmation")
require('"dry_run": {' in SCHEMA and '"const": false' in SCHEMA,
        "public schema carries pending dry-run and not-required invariants")

print("PASS unconfirmed action request cases=39 created=10 assistant_forgery=10 confirmed=0 model_calls=0 action_calls=0 wired=0")
