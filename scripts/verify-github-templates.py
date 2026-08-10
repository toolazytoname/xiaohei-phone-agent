#!/usr/bin/env python3
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
FORM = ROOT / ".github" / "ISSUE_TEMPLATE" / "delivery-task.yml"
CONFIG = ROOT / ".github" / "ISSUE_TEMPLATE" / "config.yml"
PR_TEMPLATE = ROOT / ".github" / "pull_request_template.md"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL GitHub templates: {message}")


form = FORM.read_text(encoding="utf-8")
config = CONFIG.read_text(encoding="utf-8")
pr_template = PR_TEMPLATE.read_text(encoding="utf-8")

for top_level in ("name:", "description:", "body:"):
    require(form.startswith("name:") if top_level == "name:" else f"\n{top_level}" in form,
            f"delivery form missing {top_level}")

required_ids = (
    "task_id",
    "state",
    "dependencies",
    "scope",
    "acceptance",
    "evidence",
    "rollback",
    "human_gates",
    "failure_fingerprint",
    "boundaries",
)
for field_id in required_ids:
    marker = f"    id: {field_id}\n"
    require(form.count(marker) == 1, f"field {field_id} must occur exactly once")
    block = form.split(marker, 1)[1].split("\n  - type:", 1)[0]
    require("required: true" in block, f"field {field_id} must be required")

require(form.count("\n  - type:") == 11,
        "delivery form must contain one guidance block and ten bounded inputs")
require("blank_issues_enabled: false" in config,
        "blank public issues must be disabled")
require(config.count("https://") == 2,
        "template chooser must contain delivery-status and private-security links")
require(pr_template.count("Task ID / 任务 ID:") == 1,
        "PR template must contain exactly one task declaration field")

print("PASS GitHub templates form_fields=10 required=10 blank_issues=disabled contact_links=2")
