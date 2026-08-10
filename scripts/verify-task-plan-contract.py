#!/usr/bin/env python3
"""Stdlib-only semantic gate for public task-plan.v1 fixtures."""

import datetime as dt
import json
from pathlib import Path
import re


ROOT = Path(__file__).resolve().parents[1]
FIXTURES = ROOT / "contracts/fixtures/task-plan.v1"
LONG_ID = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._:-]{7,127}$")
SHORT_ID = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._:-]{0,63}$")
IDEMPOTENCY = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._:-]{15,127}$")
ARGUMENT = re.compile(r"^[a-z][a-z0-9_]{0,63}$")
PLAN_KEYS = {
    "schema_version", "plan_id", "request_id", "dry_run", "step_budget",
    "timeout_ms", "created_at", "public_log_safe", "steps",
}
STEP_KEYS = {
    "id", "tool", "tool_version", "risk", "depends_on", "arguments", "idempotency_key",
}
TOOLS = {
    "android.open_settings": "low",
    "android.open_gallery": "low",
    "android.open_dialer": "low",
    "android.adjust_volume": "reversible",
    "android.observe": "observe",
}


def timestamp(value: object) -> bool:
    if not isinstance(value, str) or not value.endswith("Z"):
        return False
    try:
        dt.datetime.fromisoformat(value.replace("Z", "+00:00"))
    except ValueError:
        return False
    return True


def has_cycle(steps: dict[str, dict[str, object]]) -> bool:
    visiting: set[str] = set()
    visited: set[str] = set()

    def visit(step_id: str) -> bool:
        if step_id in visited:
            return False
        if step_id in visiting:
            return True
        visiting.add(step_id)
        for dependency in steps[step_id]["depends_on"]:
            if visit(dependency):
                return True
        visiting.remove(step_id)
        visited.add(step_id)
        return False

    return any(visit(step_id) for step_id in steps)


def validate(payload: dict[str, object]) -> list[str]:
    errors: list[str] = []
    if set(payload) != PLAN_KEYS:
        errors.append("plan_keys")
    if payload.get("schema_version") != 1:
        errors.append("schema_version")
    if not LONG_ID.fullmatch(str(payload.get("plan_id", ""))):
        errors.append("plan_id")
    if not LONG_ID.fullmatch(str(payload.get("request_id", ""))):
        errors.append("request_id")
    if payload.get("dry_run") is not True or payload.get("public_log_safe") is not False:
        errors.append("authority")
    budget = payload.get("step_budget")
    timeout_ms = payload.get("timeout_ms")
    if not isinstance(budget, int) or isinstance(budget, bool) or not 1 <= budget <= 8:
        errors.append("step_budget")
    if not isinstance(timeout_ms, int) or isinstance(timeout_ms, bool) or not 1000 <= timeout_ms <= 60000:
        errors.append("timeout_ms")
    if not timestamp(payload.get("created_at")):
        errors.append("created_at")
    raw_steps = payload.get("steps")
    if not isinstance(raw_steps, list) or not 1 <= len(raw_steps) <= 8:
        errors.append("step_count")
        return errors
    if isinstance(budget, int) and not isinstance(budget, bool) and len(raw_steps) > budget:
        errors.append("step_budget_exceeded")

    steps: dict[str, dict[str, object]] = {}
    keys: set[str] = set()
    for step in raw_steps:
        if not isinstance(step, dict) or set(step) != STEP_KEYS:
            errors.append("step_keys")
            continue
        step_id = step.get("id")
        if not isinstance(step_id, str) or not SHORT_ID.fullmatch(step_id) or step_id in steps:
            errors.append("step_id")
            continue
        steps[step_id] = step
        tool = step.get("tool")
        if tool not in TOOLS:
            errors.append("unknown_tool")
        elif step.get("risk") != TOOLS[tool]:
            errors.append("risk_mismatch")
        if step.get("tool_version") != 1:
            errors.append("tool_version")
        key = step.get("idempotency_key")
        if not isinstance(key, str) or not IDEMPOTENCY.fullmatch(key) or key in keys:
            errors.append("idempotency_key")
        else:
            keys.add(key)
        dependencies = step.get("depends_on")
        if not isinstance(dependencies, list) or len(dependencies) > 7 \
                or len(set(dependencies)) != len(dependencies) \
                or any(not isinstance(item, str) or not SHORT_ID.fullmatch(item) for item in dependencies):
            errors.append("depends_on")
        arguments = step.get("arguments")
        if not isinstance(arguments, dict) or len(arguments) > 32 \
                or any(not isinstance(key, str) or not ARGUMENT.fullmatch(key)
                       or not isinstance(value, str) or len(value) > 1024
                       for key, value in arguments.items()):
            errors.append("arguments")

    if len(steps) == len(raw_steps):
        for step in steps.values():
            if any(dependency not in steps for dependency in step["depends_on"]):
                errors.append("unknown_dependency")
        if "unknown_dependency" not in errors and has_cycle(steps):
            errors.append("cycle")
    return errors


def check(name: str, should_pass: bool) -> None:
    payload = json.loads((FIXTURES / name).read_text(encoding="utf-8"))
    errors = validate(payload)
    if bool(not errors) != should_pass:
        raise AssertionError(f"{name}: expected pass={should_pass}, errors={errors}")


check("valid-linear.json", True)
check("valid-forward-branch.json", True)
check("invalid-unknown-tool.json", False)
check("invalid-nine-steps.json", False)
check("invalid-cycle.json", False)
print("PASS task-plan.v1 fixtures=5 valid=2 invalid=3 unknown_tool=reject max_steps=8 cycle=reject dry_run=true")
