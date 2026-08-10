#!/usr/bin/env python3
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "manifests" / "github-progress.v1.json"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL GitHub progress manifest: {message}")


data = json.loads(MANIFEST.read_text(encoding="utf-8"))
expected_states = ["Inbox", "Ready", "In progress", "Verify", "Done"]
expected_gates = ["None", "Human", "Device", "Power", "Offline media"]

require(data.get("schema_version") == "xiaohei.github-progress.v1", "schema version")
require(data.get("owner") and data.get("repository"), "repository binding")

project = data.get("project", {})
views = project.get("views", [])
require(len(views) == 1, "exactly one delivery board view")
require(views[0].get("layout") == "board", "view layout must be board")
require(views[0].get("columns") == expected_states, "ordered five-column state model")

fields = {field.get("name"): field for field in project.get("fields", [])}
state_field = fields.get("Delivery State / 交付状态", {})
gate_field = fields.get("Gate / 门禁", {})
require(state_field.get("options") == expected_states, "state options must match columns")
require(gate_field.get("options") == expected_gates, "gate options")
for field_name in (
    "Task ID / 任务 ID",
    "Dependency / 依赖",
    "Evidence / 证据",
    "Next / 下一步",
):
    require(fields.get(field_name, {}).get("type") == "text", f"text field {field_name}")

labels = data.get("labels", [])
label_names = [label.get("name") for label in labels]
require(len(label_names) == len(set(label_names)) == 10, "ten unique labels")
require(label_names[0] == "delivery", "delivery label must be first")
expected_state_labels = [f"state:{state.lower().replace(' ', '-')}" for state in expected_states]
require(label_names[1:6] == expected_state_labels, "state labels must match columns")
require(label_names[6:] == ["gate:human", "gate:device", "gate:power", "gate:offline-media"],
        "gate labels")
for label in labels:
    require(len(label.get("color", "")) == 6, f"six-digit color for {label.get('name')}")
    require(" / " in label.get("description", ""), f"bilingual description for {label.get('name')}")

print("PASS GitHub progress manifest columns=5 fields=6 labels=10 gates=5")
