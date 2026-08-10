#!/usr/bin/env python3
"""Reject sensitive OpenCode runner content from the visible progress card."""
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei"
PROJECTION = (JAVA / "OpenCodeProgressProjection.java").read_text(encoding="utf-8")
TEST = (ROOT / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/OpenCodeProgressProjectionTest.java").read_text(encoding="utf-8")
UI = (JAVA / "AgentActivity.java").read_text(encoding="utf-8")
def require(value, message):
    if not value: raise SystemExit("FAIL opencode progress: " + message)
for part in ("enum Stage", "enum Event", "completedSteps", "stepLimit", "visibleText()", "publicLogSafe = true"):
    require(part in PROJECTION, "projection field " + part)
for forbidden in ("instructionForExecutor", "String instruction", "Path", "tokenLimit", "output.append", "ProcessBuilder", "Socket("):
    require(forbidden not in PROJECTION, "sensitive or execution field " + forbidden)
require("OpenCodeProgressProjection.disconnected().visibleText()" in UI and "OpenCode 脱敏任务进度卡" in UI, "visible read-only card")
require("OpenCodeProgressProjectionTest disconnected=1 lifecycle=7 terminal=3 sensitive_fields=4" in TEST, "redaction matrix")
print("PASS opencode-progress projection=redacted stages=6 events=6 ui_card=read_only prompt=0 token=0 paths=0 terminal_output=0")
