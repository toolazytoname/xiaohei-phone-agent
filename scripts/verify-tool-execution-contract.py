#!/usr/bin/env python3
"""Stdlib-only semantic gate for bounded tool execution results."""

import json
from pathlib import Path
import re


ROOT = Path(__file__).resolve().parents[1]
FIXTURES = ROOT / "contracts/fixtures/tool-execution.v1"
LONG_ID = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._:-]{7,127}$")
TOOL = re.compile(r"^[a-z][a-z0-9_.-]{2,63}$")
FIELD = re.compile(r"^[a-z][a-z0-9_]{0,63}$")
KEYS = {
    "schema_version", "task_id", "call_id", "tool", "status", "error_code",
    "output", "started_at_elapsed_ms", "finished_at_elapsed_ms", "duration_ms",
    "adapter_calls", "public_log_safe",
}
STATUSES = {"success", "denied", "cancelled", "timeout", "failed", "rollback_required"}
ERRORS = {
    "none", "authorization_denied", "authorization_replay", "authorization_expired",
    "scope_changed", "adapter_missing", "invalid_output", "user_cancelled", "global_stop",
    "client_disconnected", "caller_interrupted", "deadline_exceeded", "network_unavailable",
    "process_exit_nonzero", "adapter_failure", "rollback_required",
}
DENIAL_ERRORS = {"authorization_denied", "authorization_replay", "authorization_expired", "scope_changed"}
CANCEL_ERRORS = {"user_cancelled", "global_stop", "client_disconnected", "caller_interrupted"}
FAILURE_ERRORS = {"adapter_missing", "invalid_output", "network_unavailable", "process_exit_nonzero", "adapter_failure"}


def validate(value: object) -> list[str]:
    if not isinstance(value, dict) or set(value) != KEYS:
        return ["keys"]
    errors: list[str] = []
    if value.get("schema_version") != 1 or value.get("public_log_safe") is not False:
        errors.append("authority")
    for name in ("task_id", "call_id"):
        if not isinstance(value.get(name), str) or not LONG_ID.fullmatch(value[name]):
            errors.append(name)
    if not isinstance(value.get("tool"), str) or not TOOL.fullmatch(value["tool"]):
        errors.append("tool")
    status = value.get("status")
    error = value.get("error_code")
    if status not in STATUSES:
        errors.append("status")
    if error not in ERRORS:
        errors.append("error_code")
    output = value.get("output")
    if not isinstance(output, dict) or len(output) > 32 or any(
        not isinstance(key, str) or not FIELD.fullmatch(key)
        or not isinstance(item, str) or len(item) > 1024
        for key, item in output.items()
    ):
        errors.append("output")
    started = value.get("started_at_elapsed_ms")
    finished = value.get("finished_at_elapsed_ms")
    duration = value.get("duration_ms")
    calls = value.get("adapter_calls")
    if not all(isinstance(item, int) and not isinstance(item, bool)
               for item in (started, finished, duration, calls)):
        errors.append("numeric")
    elif started < 0 or finished < started or duration != finished - started \
            or duration > 60000 or calls not in (0, 1):
        errors.append("lifecycle")
    if status == "success" and (error != "none" or calls != 1):
        errors.append("success_relation")
    if status == "denied" and (error not in DENIAL_ERRORS or calls != 0 or output):
        errors.append("denied_relation")
    if status == "cancelled" and error not in CANCEL_ERRORS:
        errors.append("cancel_relation")
    if status == "timeout" and (error != "deadline_exceeded" or calls != 1 or output):
        errors.append("timeout_relation")
    if status == "failed" and error not in FAILURE_ERRORS:
        errors.append("failure_relation")
    if status == "rollback_required" and (error != "rollback_required" or calls != 1):
        errors.append("rollback_relation")
    return errors


valid_names = ["valid-success.json", "valid-timeout.json", "valid-cancelled.json"]
invalid_names = ["invalid-success-error.json", "invalid-denied-adapter-call.json"]
for name in valid_names:
    errors = validate(json.loads((FIXTURES / name).read_text(encoding="utf-8")))
    if errors:
        raise AssertionError(f"valid fixture rejected: {name}: {errors}")
for name in invalid_names:
    errors = validate(json.loads((FIXTURES / name).read_text(encoding="utf-8")))
    if not errors:
        raise AssertionError(f"invalid fixture accepted: {name}")

print(
    "PASS tool-result.v1 fixtures=5 valid=3 invalid=2 status=structured "
    "adapter_calls=0..1 public_log_safe=false"
)
