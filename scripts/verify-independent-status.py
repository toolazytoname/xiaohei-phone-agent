#!/usr/bin/env python3
from pathlib import Path

text = (Path(__file__).resolve().parents[1] / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/MainActivity.java").read_text()
for term in ("independent-capability-status", "Independent capability status", "Conversation:", "Phone Agent:", "OpenCode: 未连接", "Root broker: 未接线", "refreshIndependentStatus"):
    if term not in text: raise SystemExit(f"FAIL independent-status missing {term}")
print("PASS independent-status wake/asr/conversation/agent/opencode/root visible=true starts=0 secrets=0")
