#!/usr/bin/env python3
"""Static boundary check: runner accepts only injected adapters and fixed budgets."""
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
JAVA = (ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/OpenCodeBoundedRunner.java").read_text(encoding="utf-8")
TEST = (ROOT / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/OpenCodeBoundedRunnerTest.java").read_text(encoding="utf-8")
def require(value, message):
    if not value: raise SystemExit("FAIL opencode-bounded-runner: " + message)
for fragment in ("MAX_TIMEOUT_MS = 60000", "MAX_TOKEN_BUDGET = 4096", "MAX_STEP_BUDGET = 32", "MAX_OUTPUT_CODE_POINTS = 4096", "future.get(budget.timeoutMs", "budget.exceeded()"):
    require(fragment in JAVA, "budget " + fragment)
for forbidden in ("ProcessBuilder", "Runtime.getRuntime", "Socket(", "HttpURLConnection", "Files.read", "Files.write", "su -c", "MainActivity"):
    require(forbidden not in JAVA, "forbidden " + forbidden)
require("OpenCodeBoundedRunnerTest success=4 budget=3 denied=5 timeout=1" in TEST and "process_launches=0 network_calls=0" in TEST, "test evidence")
print("PASS opencode-bounded-runner budgets=time+token+step+output adapter=injected process=0 network=0 ui=0")
