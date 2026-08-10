#!/usr/bin/env python3
import json
import subprocess
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


def run(*args: str) -> str:
    result = subprocess.run(
        ["bash", "scripts/status-summary.sh", *args],
        cwd=ROOT,
        check=True,
        capture_output=True,
        text=True,
    )
    return result.stdout


text = run()
data = json.loads(run("--json"))

assert data["schema_version"] == "xiaohei.status-summary.v1"
assert data["task_total"] == sum(data["counts"].values())
assert data["task_total"] >= 100
assert len(data["current"]) <= 1
assert isinstance(data["blocked"], list)
assert isinstance(data["human_gates"], list)
assert data["recent_prs"]
assert data["recent_evidence"]
assert "Blocked / 阻断:" in text
assert "Recent merged PRs / 最近合并 PR:" in text
assert "Recent evidence / 最近证据:" in text
assert "docs/articles/" not in text
assert "docs/articles/" not in json.dumps(data, ensure_ascii=False)

print(
    "PASS status summary text=json counts=current/next/blocked/human "
    "recent_prs=evidence private_untracked=hidden"
)
