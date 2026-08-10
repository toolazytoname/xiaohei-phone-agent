#!/usr/bin/env python3
"""Static enforcement for ROUTE-003 zero-side-effect clarification."""

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei"
POLICY = (JAVA / "RouteClarificationPolicy.java").read_text(encoding="utf-8")
TEST = (ROOT / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/RouteClarificationPolicyTest.java").read_text(encoding="utf-8")
MAIN = (JAVA / "MainActivity.java").read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL route clarification policy: {message}")


require("enum Kind { ROUTE, ASK_TARGET, ASK_INTENT, ASK_SCOPE }" in POLICY,
        "exact route/target/intent/scope decision enum")
require("CommandRouter.route(input)" in POLICY and "IntentRouteClassifier.classify(input)" in POLICY,
        "clarification composes the reviewed router and classifier")
require("this.modelCalls = 0" in POLICY and "this.actionCalls = 0" in POLICY,
        "all decisions declare zero side effects")
require("IntentRouteClassifier.Route.CHAT" in POLICY and "CommandRouter.Action.UNKNOWN" in POLICY,
        "clarification cannot carry an executable route or command")
require("不会猜" in POLICY and "不会执行" in POLICY,
        "bilingual prompts explicitly promise no guess/execution")

for forbidden in (
    "ActionDispatcher",
    "PhoneAgentClient",
    "ConversationClient",
    "ToolGateway",
    "startActivity",
    "startService",
    "sendBroadcast",
    "ProcessBuilder",
    "Runtime.getRuntime",
    "java.net.",
    "android.",
):
    require(forbidden not in POLICY, f"policy has no execution/model path: {forbidden}")

require("RouteClarificationPolicy" not in MAIN,
        "ROUTE-003 remains inert before planning/policy/confirmation integration")
require("clarifications=30 target=10 intent=10 scope=10 clear=20" in TEST,
        "exact 50-case redacted acceptance matrix")
require("打开相册和相机" in TEST and "相册是什么" in TEST and "替我转账给某人" in TEST,
        "target/conceptual/high-risk regressions present")
require("guessed_actions=0 model_calls=0 action_calls=0" in TEST,
        "declared no-guess and zero-side-effect acceptance")

print("PASS route clarification cases=50 asks=30 clear=20 guessed_actions=0 model_calls=0 action_calls=0 wired=0")
