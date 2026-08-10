#!/usr/bin/env python3
"""Stdlib-only semantic gate for path-free OpenCode workspace lease metadata."""

import json
from pathlib import Path
import re


ROOT = Path(__file__).resolve().parents[1]
FIXTURES = ROOT / "contracts/fixtures/opencode-workspace-lease.v1"
KEYS = {"schema_version", "task_id", "allowed_areas", "persistence", "path_exposure", "public_log_safe"}
LONG_ID = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._:-]{7,127}$")


def validate(value: object) -> list[str]:
    if not isinstance(value, dict) or set(value) != KEYS:
        return ["keys"]
    errors: list[str] = []
    if value.get("schema_version") != 1:
        errors.append("schema")
    if not isinstance(value.get("task_id"), str) or not LONG_ID.fullmatch(value["task_id"]):
        errors.append("task_id")
    if value.get("allowed_areas") != ["input", "output"]:
        errors.append("areas")
    if value.get("persistence") != "private_app_storage" or value.get("path_exposure") != "none" \
            or value.get("public_log_safe") is not False:
        errors.append("privacy")
    return errors


for name in ("valid-lease.json",):
    errors = validate(json.loads((FIXTURES / name).read_text(encoding="utf-8")))
    if errors:
        raise AssertionError(f"valid fixture rejected: {name}: {errors}")
for name in ("invalid-path-field.json", "invalid-area.json"):
    errors = validate(json.loads((FIXTURES / name).read_text(encoding="utf-8")))
    if not errors:
        raise AssertionError(f"invalid fixture accepted: {name}")

print("PASS opencode-workspace-lease.v1 fixtures=3 valid=1 invalid=2 areas=input+output "
      "paths=absent public_log_safe=false")
