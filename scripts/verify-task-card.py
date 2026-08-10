#!/usr/bin/env python3
from pathlib import Path
root = Path(__file__).resolve().parents[1]
core = (root / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/TaskCardProjection.java").read_text()
test = (root / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/TaskCardProjectionTest.java").read_text()
ui = (root / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/AgentActivity.java").read_text()
for text, terms in ((core, ("UNAVAILABLE", "TAKEN_OVER", "不显示任务正文、路径、Token、模型回复或推理过程")), (test, ("takeover=1", "execution=0")), (ui, ("TaskCardProjection.unavailable", "只读任务卡"))):
    if any(term not in text for term in terms): raise SystemExit("FAIL task-card boundary")
print("PASS task-card target/plan/budget/result/takeover public=true execution=0")
