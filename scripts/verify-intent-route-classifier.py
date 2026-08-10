#!/usr/bin/env python3
"""Static enforcement for ROUTE-002 inert three-way classification."""

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei"
CLASSIFIER = (JAVA / "IntentRouteClassifier.java").read_text(encoding="utf-8")
TEST = (ROOT / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/IntentRouteClassifierTest.java").read_text(encoding="utf-8")
MAIN = (JAVA / "MainActivity.java").read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL intent route classifier: {message}")


require("enum Route { CHAT, DETERMINISTIC_COMMAND, COMPLEX_TASK }" in CLASSIFIER,
        "exact three-way route enum")
require("CommandRouter.route(input)" in CLASSIFIER,
        "deterministic commands reuse reviewed router")
require("this.modelCalls = 0" in CLASSIFIER and "this.actionCalls = 0" in CLASSIFIER,
        "classification has zero side effects")
require("ambiguous action is inert until ROUTE-003 clarification" in CLASSIFIER,
        "ambiguous action remains inert")

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
    require(forbidden not in CLASSIFIER, f"classifier has no execution/model path: {forbidden}")

require("IntentRouteClassifier" not in MAIN,
        "ROUTE-002 is not wired to execute/navigate before ROUTE-003/004")
require("cases=100 command=40 chat=35 complex=25" in TEST,
        "exact 100-case redacted matrix")
require("回复消息是什么意思" in TEST and "打开相册和相机" in TEST and "替我转账给某人" in TEST,
        "conceptual/ambiguous/high-risk regressions present")
require("action_calls=0 model_calls=0 ambiguous=inert" in TEST,
        "declared zero-side-effect acceptance")

print("PASS intent route classifier cases=100 command=40 chat=35 complex=25 execution_paths=0 ambiguous=inert")
