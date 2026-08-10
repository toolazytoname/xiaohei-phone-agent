#!/usr/bin/env python3
"""Stdlib-only semantic gate for the public conversation-session v1 fixtures."""

import datetime as dt
import json
import pathlib
import re
import sys


ROOT = pathlib.Path(__file__).resolve().parents[1]
FIXTURES = ROOT / "contracts" / "fixtures" / "conversation-session.v1"
SESSION_ID = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._:-]{7,127}$")
PROFILE_ID = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._-]{0,63}$")
REQUIRED = {
    "schema_version", "session_id", "profile_id", "mode", "input_modality",
    "output_modality", "state", "turn_index", "max_turns", "token_budget",
    "timeout_ms", "action_authority", "persistence", "started_at", "public_log_safe",
}
OPTIONAL = {"cancel_reason"}


def is_int(value):
    return isinstance(value, int) and not isinstance(value, bool)


def valid_timestamp(value):
    if not isinstance(value, str):
        return False
    try:
        dt.datetime.fromisoformat(value.replace("Z", "+00:00"))
    except ValueError:
        return False
    return True


def validate(payload):
    errors = []
    keys = set(payload)
    missing = REQUIRED - keys
    unknown = keys - REQUIRED - OPTIONAL
    if missing:
        errors.append("missing=" + ",".join(sorted(missing)))
    if unknown:
        errors.append("unknown=" + ",".join(sorted(unknown)))
    if payload.get("schema_version") != 1:
        errors.append("schema_version")
    if not SESSION_ID.fullmatch(payload.get("session_id", "")):
        errors.append("session_id")
    if not PROFILE_ID.fullmatch(payload.get("profile_id", "")):
        errors.append("profile_id")
    if payload.get("mode") not in {"single_turn", "follow_up"}:
        errors.append("mode")
    if payload.get("input_modality") not in {"text", "voice"}:
        errors.append("input_modality")
    if payload.get("output_modality") not in {"text", "speech", "text_and_speech"}:
        errors.append("output_modality")
    state = payload.get("state")
    if state not in {"created", "listening", "transcribing", "thinking", "speaking", "waiting_follow_up", "completed", "cancelled", "failed"}:
        errors.append("state")
    for name, lower, upper in (("turn_index", 0, 8), ("max_turns", 1, 8), ("token_budget", 64, 8192), ("timeout_ms", 1000, 900000)):
        value = payload.get(name)
        if not is_int(value) or not lower <= value <= upper:
            errors.append(name)
    if is_int(payload.get("turn_index")) and is_int(payload.get("max_turns")) and payload["turn_index"] > payload["max_turns"]:
        errors.append("turn_index_exceeds_max_turns")
    if payload.get("mode") == "single_turn" and payload.get("max_turns") != 1:
        errors.append("single_turn_max_turns")
    if state == "waiting_follow_up" and (payload.get("mode") != "follow_up" or payload.get("turn_index", 0) < 1):
        errors.append("waiting_follow_up")
    if state == "cancelled" and payload.get("cancel_reason") not in {"user", "timeout", "interrupted", "privacy", "error"}:
        errors.append("cancel_reason")
    if payload.get("action_authority") != "none":
        errors.append("action_authority")
    if payload.get("persistence") != "memory_only":
        errors.append("persistence")
    if not valid_timestamp(payload.get("started_at")):
        errors.append("started_at")
    if payload.get("public_log_safe") is not True:
        errors.append("public_log_safe")
    return errors


def check(name, should_pass):
    payload = json.loads((FIXTURES / name).read_text(encoding="utf-8"))
    errors = validate(payload)
    if bool(not errors) != should_pass:
        raise AssertionError(f"{name}: expected pass={should_pass}, errors={errors}")


def main():
    check("valid-minimal.json", True)
    check("valid-boundary-follow-up.json", True)
    check("invalid-unknown-field.json", False)
    check("invalid-over-budget.json", False)
    check("invalid-turn-exceeds-max.json", False)
    print("PASS conversation-session.v1 fixtures=5 valid=2 invalid=3")


if __name__ == "__main__":
    try:
        main()
    except (AssertionError, OSError, json.JSONDecodeError) as error:
        print(f"FAIL conversation-session.v1: {error}", file=sys.stderr)
        raise SystemExit(1)
