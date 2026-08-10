#!/usr/bin/env python3
from pathlib import Path
root = Path(__file__).resolve().parents[1]
core = (root / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/PermissionCenterProjection.java").read_text()
test = (root / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/PermissionCenterProjectionTest.java").read_text()
ui = (root / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/MainActivity.java").read_text()
for text, terms in ((core, ("用途：", "最近使用：", "撤销：", "Root broker：不支持")), (test, ("unsupported=1", "authority=0")), (ui, ("PermissionCenterProjection.visibleText", "ACTION_APPLICATION_DETAILS_SETTINGS"))):
    if any(term not in text for term in terms): raise SystemExit("FAIL permission-center boundary")
print("PASS permission-center purpose/last_use/revoke/unsupported visible=true authority=0")
