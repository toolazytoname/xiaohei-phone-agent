#!/usr/bin/env python3
"""Stdlib-only structural and semantic gate for tool-catalog.v1 fixtures."""

import json
from pathlib import Path
import re


ROOT = Path(__file__).resolve().parents[1]
FIXTURES = ROOT / "contracts/fixtures/tool-catalog.v1"
CATALOG_KEYS = {"schema_version", "catalog_id", "tools"}
TOOL_KEYS = {
    "name", "version", "risk", "input_schema", "output_schema",
    "rollback", "audience", "timeout_ms",
}
NAME = re.compile(r"^[a-z][a-z0-9_.-]{2,63}$")
SCHEMA_ID = re.compile(r"^xiaohei\.tool\.(input|output)\.[a-z0-9_.-]{1,96}\.v1$")
RISKS = {"observe", "low", "reversible", "high"}
AUDIENCES = {"android_gateway", "opencode_gateway", "root_broker"}
ROLLBACK_MODES = {"none", "restore_snapshot", "reverse_tool", "manual"}
SCHEMA_FILES = {
    "xiaohei.tool.input.empty.v1": "tool-input-empty.v1.schema.json",
    "xiaohei.tool.input.volume.v1": "tool-input-volume.v1.schema.json",
    "xiaohei.tool.input.observe.v1": "tool-input-observe.v1.schema.json",
    "xiaohei.tool.input.media_test_collection.v1": "tool-input-media_test_collection.v1.schema.json",
    "xiaohei.tool.output.activity.v1": "tool-output-activity.v1.schema.json",
    "xiaohei.tool.output.volume.v1": "tool-output-volume.v1.schema.json",
    "xiaohei.tool.output.observation.v1": "tool-output-observation.v1.schema.json",
    "xiaohei.tool.output.media_test_collection.v1": "tool-output-media_test_collection.v1.schema.json",
}


def validate(payload: object) -> list[str]:
    errors: list[str] = []
    if not isinstance(payload, dict) or set(payload) != CATALOG_KEYS:
        return ["catalog_keys"]
    if payload.get("schema_version") != 1 or payload.get("catalog_id") != "xiaohei.tool-catalog.v1":
        errors.append("identity")
    tools = payload.get("tools")
    if not isinstance(tools, list) or not 1 <= len(tools) <= 64:
        return errors + ["tool_count"]

    identities: set[tuple[str, int]] = set()
    valid_tools: list[dict[str, object]] = []
    for tool in tools:
        if not isinstance(tool, dict) or set(tool) != TOOL_KEYS:
            errors.append("tool_keys")
            continue
        valid_tools.append(tool)
        name = tool.get("name")
        version = tool.get("version")
        if not isinstance(name, str) or not NAME.fullmatch(name):
            errors.append("name")
        if version != 1:
            errors.append("version")
        identity = (str(name), version if isinstance(version, int) else -1)
        if identity in identities:
            errors.append("duplicate")
        identities.add(identity)
        if tool.get("risk") not in RISKS:
            errors.append("risk")
        for field, direction in (("input_schema", "input"), ("output_schema", "output")):
            schema_id = tool.get(field)
            if not isinstance(schema_id, str) or not SCHEMA_ID.fullmatch(schema_id) \
                    or f".tool.{direction}." not in schema_id:
                errors.append(field)
            elif schema_id not in SCHEMA_FILES \
                    or not (ROOT / "contracts" / SCHEMA_FILES[schema_id]).is_file():
                errors.append("schema_reference")
        if tool.get("audience") not in AUDIENCES:
            errors.append("audience")
        timeout = tool.get("timeout_ms")
        if not isinstance(timeout, int) or isinstance(timeout, bool) or not 100 <= timeout <= 60000:
            errors.append("timeout")

    for tool in valid_tools:
        rollback = tool.get("rollback")
        if not isinstance(rollback, dict) or not {"mode"} <= set(rollback) <= {"mode", "tool"}:
            errors.append("rollback")
            continue
        mode = rollback.get("mode")
        target = rollback.get("tool")
        if mode not in ROLLBACK_MODES:
            errors.append("rollback")
        elif mode in {"none", "manual"} and target is not None:
            errors.append("rollback")
        elif mode in {"restore_snapshot", "reverse_tool"}:
            if not isinstance(target, str) or (target, tool.get("version")) not in identities:
                errors.append("rollback")
    return errors


def check(name: str, should_pass: bool, expected_error: str | None = None) -> dict[str, object]:
    payload = json.loads((FIXTURES / name).read_text(encoding="utf-8"))
    errors = validate(payload)
    if bool(not errors) != should_pass or expected_error is not None and expected_error not in errors:
        raise AssertionError(
            f"{name}: expected pass={should_pass} error={expected_error}, errors={errors}"
        )
    return payload


valid = check("valid-builtins.json", True)
check("invalid-duplicate.json", False, "duplicate")
check("invalid-version.json", False, "version")
check("invalid-missing-schema.json", False, "tool_keys")
check("invalid-rollback.json", False, "rollback")
for schema_id, relative in SCHEMA_FILES.items():
    schema = json.loads((ROOT / "contracts" / relative).read_text(encoding="utf-8"))
    if schema.get("$schema") != "https://json-schema.org/draft/2020-12/schema":
        raise AssertionError(f"{schema_id}: draft mismatch")
    if "additionalProperties" not in schema or schema["additionalProperties"] is not False:
        raise AssertionError(f"{schema_id}: open top-level object")

print(
    "PASS tool-catalog.v1 fixtures=5 valid=1 invalid=4 descriptors="
    f"{len(valid['tools'])} duplicate=reject version=reject missing=reject rollback=reject refs=8"
)
