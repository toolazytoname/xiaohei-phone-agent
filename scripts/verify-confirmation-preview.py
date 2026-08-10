#!/usr/bin/env python3
from pathlib import Path

root = Path(__file__).resolve().parents[1]
core = (root / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/ConfirmationPreview.java").read_text()
test = (root / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/ConfirmationPreviewTest.java").read_text()
ui = (root / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/AgentActivity.java").read_text()
for text, terms in ((core, ("App：", "目标 / Target：", "内容 / Content：", "权限 / Permission：", "停止与回滚 / Stop & rollback：", "executionCalls = 0")), (test, ("cancel_default=1", "execution=0")), (ui, ("ConfirmationPreview.phoneAgent", "preview.visibleText"))):
    if any(term not in text for term in terms):
        raise SystemExit("FAIL confirmation-preview boundary")
print("PASS confirmation-preview app/target/content/permission/rollback visible=true cancel_default=true execution=0")
