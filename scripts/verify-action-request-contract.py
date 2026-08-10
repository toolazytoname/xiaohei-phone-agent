#!/usr/bin/env python3
"""Stdlib-only structural and cross-field gate for action-request.v1 fixtures."""

import datetime as dt
import json
from pathlib import Path
import re


ROOT = Path(__file__).resolve().parents[1]
FIXTURES = ROOT / "contracts/fixtures/action-request.v1"
REQUEST_ID = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._:-]{7,127}$")
REQUIRED = {
    "schema_version", "request_id", "target", "action", "risk",
    "requires_confirmation", "confirmation_state", "dry_run", "created_at", "redaction",
}
OPTIONAL = {"parameters", "expires_at"}


def timestamp(value: object) -> bool:
    if not isinstance(value, str):
        return False
    try:
        dt.datetime.fromisoformat(value.replace("Z", "+00:00"))
    except ValueError:
        return False
    return True


def validate(payload: dict[str, object]) -> list[str]:
    errors: list[str] = []
    keys = set(payload)
    if REQUIRED - keys:
        errors.append("missing")
    if keys - REQUIRED - OPTIONAL:
        errors.append("unknown")
    if payload.get("schema_version") != 1:
        errors.append("schema_version")
    if not REQUEST_ID.fullmatch(str(payload.get("request_id", ""))):
        errors.append("request_id")
    if payload.get("target") not in {
        "android_app", "android_notification", "android_accessibility", "local_service"
    }:
        errors.append("target")
    action = payload.get("action")
    if not isinstance(action, str) or not 1 <= len(action) <= 128:
        errors.append("action")
    risk = payload.get("risk")
    if risk not in {"low", "medium", "high"}:
        errors.append("risk")
    requires = payload.get("requires_confirmation")
    dry_run = payload.get("dry_run")
    if not isinstance(requires, bool):
        errors.append("requires_confirmation")
    if not isinstance(dry_run, bool):
        errors.append("dry_run")
    state = payload.get("confirmation_state")
    if state not in {"not_required", "pending", "confirmed", "rejected", "expired"}:
        errors.append("confirmation_state")
    if risk == "high" and requires is not True:
        errors.append("high_requires_confirmation")
    if state == "pending" and (requires is not True or dry_run is not True):
        errors.append("pending_must_be_confirmed_later_and_dry_run")
    if state == "not_required" and requires is not False:
        errors.append("not_required_mismatch")
    if requires is False and state != "not_required":
        errors.append("false_confirmation_mismatch")
    if not timestamp(payload.get("created_at")):
        errors.append("created_at")
    if "expires_at" in payload and not timestamp(payload["expires_at"]):
        errors.append("expires_at")
    if "parameters" in payload and not isinstance(payload["parameters"], dict):
        errors.append("parameters")
    redaction = payload.get("redaction")
    if not isinstance(redaction, dict) or "public_log_safe" not in redaction:
        errors.append("redaction")
    elif set(redaction) - {"public_log_safe", "sensitive_fields"}:
        errors.append("redaction_unknown")
    elif not isinstance(redaction.get("public_log_safe"), bool):
        errors.append("public_log_safe")
    return errors


def check(name: str, should_pass: bool) -> None:
    payload = json.loads((FIXTURES / name).read_text(encoding="utf-8"))
    errors = validate(payload)
    if bool(not errors) != should_pass:
        raise AssertionError(f"{name}: expected pass={should_pass}, errors={errors}")


check("valid-pending-complex.json", True)
check("invalid-pending-live.json", False)
check("invalid-pending-no-confirmation.json", False)
check("invalid-unknown-field.json", False)
print("PASS action-request.v1 fixtures=4 valid=1 invalid=3 pending=dry_run confirmation=required unknown=reject")
