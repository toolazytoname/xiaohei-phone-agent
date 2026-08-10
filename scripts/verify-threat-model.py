#!/usr/bin/env python3
"""Keep documented injection/root boundaries explicit in both languages."""

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
CASES = (
    (
        ROOT / "docs/threat-model.md",
        (
            "Injection, tool poisoning, and root escalation",
            "Chat/notification prompt injection",
            "Tool/schema poisoning",
            "OpenCode workspace traversal",
            "Root escalation",
            "Destructive/exfiltration request",
        ),
    ),
    (
        ROOT / "docs/threat-model.zh-CN.md",
        (
            "注入、工具投毒与 Root 提权",
            "聊天/通知提示注入",
            "工具/Schema 投毒",
            "OpenCode 工作区穿越",
            "Root 提权",
            "破坏/外传请求",
        ),
    ),
)

for path, required in CASES:
    text = path.read_text(encoding="utf-8")
    missing = [term for term in required if term not in text]
    if missing:
        raise SystemExit(f"FAIL threat-model {path.name}: missing {', '.join(missing)}")

print("PASS threat-model injection/tool-poisoning/root-escalation coverage=5")
