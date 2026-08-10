#!/usr/bin/env python3
"""Stdlib-only semantic gate for confirmation-grant.v1 fixtures."""

import json
from pathlib import Path
import re


ROOT = Path(__file__).resolve().parents[1]
FIXTURES = ROOT / "contracts/fixtures/confirmation-grant.v1"
IDENTIFIER = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._:-]{7,127}$")
DIGEST = re.compile(r"^[a-f0-9]{64}$")
KEYS = {
    "schema_version", "confirmation_id", "task_id", "request_id", "plan_id",
    "target_digest", "content_digest", "issued_at_elapsed_ms", "expires_at_elapsed_ms",
    "ttl_ms", "source", "single_use", "persistence", "public_log_safe",
}


def integer(value: object) -> bool:
    return isinstance(value, int) and not isinstance(value, bool)


def validate(payload: dict[str, object]) -> list[str]:
    errors: list[str] = []
    if set(payload) != KEYS:
        errors.append("keys")
    if payload.get("schema_version") != 1:
        errors.append("schema_version")
    for name in ("confirmation_id", "task_id", "request_id", "plan_id"):
        if not IDENTIFIER.fullmatch(str(payload.get(name, ""))):
            errors.append(name)
    for name in ("target_digest", "content_digest"):
        if not DIGEST.fullmatch(str(payload.get(name, ""))):
            errors.append(name)
    issued = payload.get("issued_at_elapsed_ms")
    expires = payload.get("expires_at_elapsed_ms")
    ttl = payload.get("ttl_ms")
    if not integer(issued) or issued < 0:
        errors.append("issued")
    if not integer(expires) or expires < 1:
        errors.append("expires")
    if not integer(ttl) or not 1000 <= ttl <= 60000:
        errors.append("ttl")
    if integer(issued) and integer(expires) and integer(ttl) and expires - issued != ttl:
        errors.append("window_mismatch")
    if payload.get("source") != "local_user_gesture":
        errors.append("source")
    if payload.get("single_use") is not True:
        errors.append("single_use")
    if payload.get("persistence") != "memory_only":
        errors.append("persistence")
    if payload.get("public_log_safe") is not False:
        errors.append("public_log_safe")
    return errors


def check(name: str, should_pass: bool) -> None:
    payload = json.loads((FIXTURES / name).read_text(encoding="utf-8"))
    errors = validate(payload)
    if bool(not errors) != should_pass:
        raise AssertionError(f"{name}: expected pass={should_pass}, errors={errors}")


check("valid-fresh.json", True)
check("invalid-assistant-source.json", False)
check("invalid-window.json", False)
check("invalid-raw-content.json", False)
print("PASS confirmation-grant.v1 fixtures=4 valid=1 invalid=3 source=user ttl=1..60s single_use=true raw_content=reject")
