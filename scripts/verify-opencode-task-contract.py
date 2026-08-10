#!/usr/bin/env python3
"""Stdlib-only semantic gate for private pending OpenCode task proposals."""

import json
from pathlib import Path
import re


ROOT = Path(__file__).resolve().parents[1]
FIXTURES = ROOT / "contracts/fixtures/opencode-task.v1"
LONG_ID = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._:-]{7,127}$")
KEYS = {
    "schema_version", "task_id", "request_id", "plan_id", "kind", "source",
    "instruction", "dry_run", "requires_confirmation", "confirmation_state",
    "execution_state", "audience", "redaction",
}
KINDS = {"project_summary", "test_diagnosis", "controlled_file_organization"}
FORBIDDEN_FIELDS = {"command", "argv", "environment", "cwd", "workspace", "url", "token", "root"}


def validate(value: object) -> list[str]:
    if not isinstance(value, dict) or set(value) != KEYS:
        return ["keys"]
    errors: list[str] = []
    if value.get("schema_version") != 1:
        errors.append("schema")
    for field in ("task_id", "request_id", "plan_id"):
        if not isinstance(value.get(field), str) or not LONG_ID.fullmatch(value[field]):
            errors.append(field)
    if value.get("kind") not in KINDS:
        errors.append("kind")
    if value.get("source") != "typed_user":
        errors.append("source")
    instruction = value.get("instruction")
    if not isinstance(instruction, str) or not 1 <= len(instruction) <= 2048:
        errors.append("instruction")
    if value.get("dry_run") is not True or value.get("requires_confirmation") is not True \
            or value.get("confirmation_state") != "pending" \
            or value.get("execution_state") != "not_started" \
            or value.get("audience") != "opencode_gateway":
        errors.append("lifecycle")
    if value.get("redaction") != {"public_log_safe": False, "sensitive_fields": ["instruction"]}:
        errors.append("redaction")
    if FORBIDDEN_FIELDS.intersection(value):
        errors.append("authority")
    return errors


valid_names = ["valid-project-summary.json", "valid-test-diagnosis.json"]
invalid_names = ["invalid-command-field.json", "invalid-source.json", "invalid-live-state.json"]
for name in valid_names:
    errors = validate(json.loads((FIXTURES / name).read_text(encoding="utf-8")))
    if errors:
        raise AssertionError(f"valid fixture rejected: {name}: {errors}")
for name in invalid_names:
    errors = validate(json.loads((FIXTURES / name).read_text(encoding="utf-8")))
    if not errors:
        raise AssertionError(f"invalid fixture accepted: {name}")

print("PASS opencode-task.v1 fixtures=5 valid=2 invalid=3 kinds=3 pending=dry_run "
      "unknown=reject generic_shell=absent public_log_safe=false")
