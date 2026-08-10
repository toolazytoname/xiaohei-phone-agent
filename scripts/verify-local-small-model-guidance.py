#!/usr/bin/env python3
from pathlib import Path

root = Path(__file__).resolve().parents[1]
ui = (root / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/ModelConfigActivity.java").read_text()
for term in ("local-small-model-guidance", "不会自动启用、切换模型、规划任务或调用工具", "cannot auto-enable, switch models, plan, or call tools"):
    if term not in ui: raise SystemExit(f"FAIL local-small-model missing UI boundary: {term}")
for name, terms in {"docs/local-small-model-guidance.md": ("no generative local-model weights", "may not automatically enable", "OpenCode/root"), "docs/local-small-model-guidance.zh-CN.md": ("不包含生成式本地模型权重", "不能自动启用自身", "OpenCode/root")}.items():
    text = (root / name).read_text()
    if any(term not in text for term in terms): raise SystemExit(f"FAIL local-small-model doc: {name}")
print("PASS local-small-model visible=true automatic=false authority=none weights=absent")
