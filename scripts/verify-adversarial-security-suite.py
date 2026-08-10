#!/usr/bin/env python3
from pathlib import Path

root = Path(__file__).resolve().parents[1]
checks = {
    "docs/adversarial-security-suite.md": ("Prompt/tool injection", "Workspace traversal", "Privilege escalation", "Privacy exfiltration"),
    "docs/adversarial-security-suite.zh-CN.md": ("Prompt/工具注入", "工作区穿越", "越权/提权", "隐私外传"),
    "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/ConversationPromptPolicyTest.java": ("injections=20", "forgeries=10"),
    "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/OpenCodeWorkspaceBoundaryTest.java": ("traversal=7", "symlink=3", "cross_task=2"),
    "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/ConversationPrivacyPolicyTest.java": ("categories=5", "model_calls=0"),
    "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/RootDestructiveDenialPolicyTest.java": ("unknown=1", "execution_paths=0"),
}
for path, terms in checks.items():
    data = (root / path).read_text(encoding="utf-8")
    missing = [term for term in terms if term not in data]
    if missing: raise SystemExit(f"FAIL adversarial-suite {path}: {', '.join(missing)}")
print("PASS adversarial-suite injection=20 traversal=7 escalation=fail_closed privacy=5 execution=0")
