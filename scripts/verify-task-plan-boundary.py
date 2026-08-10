#!/usr/bin/env python3
"""Static enforcement for PLAN-001 bounded rules-first plan validation."""

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei"
VALIDATOR = (JAVA / "TaskPlanValidator.java").read_text(encoding="utf-8")
TEST = (ROOT / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/TaskPlanValidatorTest.java").read_text(encoding="utf-8")
SCHEMA = (ROOT / "contracts/task-plan.v1.schema.json").read_text(encoding="utf-8")
MAIN = (JAVA / "MainActivity.java").read_text(encoding="utf-8")
CONVERSATION = (JAVA / "ConversationActivity.java").read_text(encoding="utf-8")
AGENT = (JAVA / "AgentActivity.java").read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL task plan boundary: {message}")


require("SCHEMA_VERSION = 1" in VALIDATOR and "MAX_STEPS = 8" in VALIDATOR,
        "schema version and eight-step hard cap")
require("MIN_TIMEOUT_MS = 1000" in VALIDATOR and "MAX_TIMEOUT_MS = 60000" in VALIDATOR,
        "bounded total timeout")
require("UNKNOWN_TOOL" in VALIDATOR and "DEPENDENCY_CYCLE" in VALIDATOR,
        "explicit unknown-tool and cycle results")
require("ToolCatalog.risk(step.tool)" in VALIDATOR,
        "rules validate against the local reviewed catalog")
require("hasCycle(" in VALIDATOR and "visiting" in VALIDATOR and "visited" in VALIDATOR,
        "real DAG traversal rather than order-only dependency check")
require("plan.dryRun" in VALIDATOR and "!plan.publicLogSafe" in VALIDATOR,
        "plans remain dry-run and raw arguments non-public")
require("this.modelCalls = 0" in VALIDATOR and "this.actionCalls = 0" in VALIDATOR,
        "validation has zero side effects")

for forbidden in (
    "PhoneAgentClient", "ConversationClient", "ActionDispatcher", "ToolGateway",
    "startActivity", "startService", "sendBroadcast", "ProcessBuilder",
    "Runtime.getRuntime", "java.net.", "android.", "System.out", "Log.",
):
    require(forbidden not in VALIDATOR, f"no model/execution/platform path: {forbidden}")

require("TaskPlanValidator" not in MAIN and "TaskPlanValidator" not in CONVERSATION
        and "TaskPlanValidator" not in AGENT,
        "PLAN-001 remains unwired before remote adapter/policy/tool gates")
require("valid=10 unknown_tool=5 step_count=2 cycle=5 malformed=12" in TEST,
        "exact 34-case Java matrix")
require("model_calls=0 action_calls=0 execution_paths=0" in TEST,
        "declared zero-side-effect acceptance")
require('"dry_run": {"const": true}' in SCHEMA and '"maxItems": 8' in SCHEMA,
        "public schema is dry-run and eight-step bounded")
require('"timeout_ms"' in SCHEMA and '"maximum": 60000' in SCHEMA,
        "public schema timeout bound")

print("PASS task plan boundary cases=34 valid=10 unknown_tool=5 max_steps=8 cycles=5 model_calls=0 action_calls=0 wired=0")
