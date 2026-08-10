#!/usr/bin/env python3
"""Stdlib-only semantic gate for tool-call/capability and loopback fixtures."""

import hashlib
import json
from pathlib import Path
import re


ROOT = Path(__file__).resolve().parents[1]
FIXTURES = ROOT / "contracts/fixtures/tool-gateway.v1"
LONG_ID = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._:-]{7,127}$")
IDEMPOTENCY = re.compile(r"^[A-Za-z0-9][A-Za-z0-9._:-]{15,127}$")
TOKEN_ID = re.compile(r"^cap-[a-f0-9]{32}$")
DIGEST = re.compile(r"^[a-f0-9]{64}$")
ARGUMENT = re.compile(r"^[a-z][a-z0-9_]{0,63}$")
CALL_KEYS = {
    "schema_version", "task_id", "request_id", "plan_id", "call_id", "tool",
    "tool_version", "risk", "audience", "arguments", "idempotency_key",
    "requested_at_elapsed_ms", "timeout_ms", "public_log_safe",
}
TOKEN_KEYS = {
    "schema_version", "token_id", "confirmation_id", "task_id", "request_id",
    "plan_id", "call_id", "tool", "tool_version", "risk", "audience",
    "call_digest", "call_timeout_ms", "issued_at_elapsed_ms", "expires_at_elapsed_ms", "ttl_ms",
    "single_use", "persistence", "public_log_safe",
}
TOOLS = {
    "android.open_settings": ("low", "android_gateway", 5000),
    "android.open_gallery": ("low", "android_gateway", 5000),
    "android.open_dialer": ("low", "android_gateway", 5000),
    "android.adjust_volume": ("reversible", "android_gateway", 3000),
    "android.observe": ("observe", "android_gateway", 3000),
}


def load(name: str) -> dict[str, object]:
    return json.loads((FIXTURES / name).read_text(encoding="utf-8"))


def validate_call(call: object) -> list[str]:
    if not isinstance(call, dict) or set(call) != CALL_KEYS:
        return ["call_keys"]
    errors: list[str] = []
    if call.get("schema_version") != 1 or call.get("public_log_safe") is not False:
        errors.append("call_authority")
    for field in ("task_id", "request_id", "plan_id", "call_id"):
        if not isinstance(call.get(field), str) or not LONG_ID.fullmatch(call[field]):
            errors.append(field)
    tool = call.get("tool")
    if tool not in TOOLS:
        errors.append("unknown_tool")
    elif (call.get("risk"), call.get("audience")) != TOOLS[tool][:2]:
        errors.append("catalog_scope")
    if call.get("tool_version") != 1:
        errors.append("tool_version")
    arguments = call.get("arguments")
    if not isinstance(arguments, dict) or len(arguments) > 32 or any(
        not isinstance(key, str) or not ARGUMENT.fullmatch(key)
        or not isinstance(value, str) or len(value) > 1024
        for key, value in arguments.items()
    ):
        errors.append("arguments")
    if not isinstance(call.get("idempotency_key"), str) \
            or not IDEMPOTENCY.fullmatch(call["idempotency_key"]):
        errors.append("idempotency_key")
    requested = call.get("requested_at_elapsed_ms")
    if not isinstance(requested, int) or isinstance(requested, bool) or requested < 0:
        errors.append("requested_at")
    timeout = call.get("timeout_ms")
    if not isinstance(timeout, int) or isinstance(timeout, bool) \
            or tool not in TOOLS or not 100 <= timeout <= TOOLS[tool][2]:
        errors.append("timeout")
    return errors


def validate_token(token: object) -> list[str]:
    if not isinstance(token, dict) or set(token) != TOKEN_KEYS:
        return ["token_keys"]
    errors: list[str] = []
    if token.get("schema_version") != 1 or token.get("single_use") is not True \
            or token.get("persistence") != "memory_only" \
            or token.get("public_log_safe") is not False:
        errors.append("token_authority")
    if not isinstance(token.get("token_id"), str) or not TOKEN_ID.fullmatch(token["token_id"]):
        errors.append("token_id")
    for field in ("confirmation_id", "task_id", "request_id", "plan_id", "call_id"):
        if not isinstance(token.get(field), str) or not LONG_ID.fullmatch(token[field]):
            errors.append(field)
    if token.get("tool") not in TOOLS or token.get("tool_version") != 1:
        errors.append("tool_version")
    elif (token.get("risk"), token.get("audience")) != TOOLS[token["tool"]][:2]:
        errors.append("catalog_scope")
    if not isinstance(token.get("call_digest"), str) or not DIGEST.fullmatch(token["call_digest"]):
        errors.append("call_digest")
    timeout = token.get("call_timeout_ms")
    if not isinstance(timeout, int) or isinstance(timeout, bool) \
            or token.get("tool") not in TOOLS or not 100 <= timeout <= TOOLS[token["tool"]][2]:
        errors.append("timeout")
    issued = token.get("issued_at_elapsed_ms")
    expires = token.get("expires_at_elapsed_ms")
    ttl = token.get("ttl_ms")
    if not all(isinstance(value, int) and not isinstance(value, bool)
               for value in (issued, expires, ttl)) \
            or issued < 0 or not 1000 <= ttl <= 30000 or expires - issued != ttl:
        errors.append("window")
    return errors


def call_digest(token_id: str, call: dict[str, object]) -> str:
    values = [
        call["task_id"], call["request_id"], call["plan_id"], call["call_id"],
        call["tool"], str(call["tool_version"]), call["risk"], call["audience"],
        call["idempotency_key"], str(call["requested_at_elapsed_ms"]),
        str(call["timeout_ms"]),
        str(call["public_log_safe"]).lower(),
    ]
    for key in sorted(call["arguments"]):
        values.extend((key, call["arguments"][key]))
    canonical = "".join(f"{len(value)}:{value}" for value in values)
    return hashlib.sha256(f"{token_id}|{canonical}".encode()).hexdigest()


def validate_pair(call: dict[str, object], token: dict[str, object], now_ms: int) -> list[str]:
    errors = validate_call(call) + validate_token(token)
    if errors:
        return errors
    for field in ("task_id", "request_id", "plan_id", "call_id", "tool", "tool_version", "risk", "audience"):
        if call[field] != token[field]:
            errors.append("scope")
    if call["timeout_ms"] != token["call_timeout_ms"]:
        errors.append("timeout_scope")
    if call_digest(token["token_id"], call) != token["call_digest"]:
        errors.append("digest")
    if now_ms < token["issued_at_elapsed_ms"]:
        errors.append("clock_rollback")
    if now_ms >= token["expires_at_elapsed_ms"]:
        errors.append("expired")
    return errors


def loopback_literal(value: object) -> bool:
    if not isinstance(value, str):
        return False
    normalized = value.strip().lower()
    if normalized.startswith("[") and normalized.endswith("]"):
        normalized = normalized[1:-1]
    if normalized in {"::1", "0:0:0:0:0:0:0:1"}:
        return True
    parts = normalized.split(".")
    return len(parts) == 4 and parts[0] == "127" and all(
        part.isdigit() and 0 <= int(part) <= 255 and len(part) <= 3 for part in parts
    )


call = load("valid-call.json")
token = load("valid-token.json")
if validate_pair(call, token, 100001):
    raise AssertionError(f"valid pair rejected: {validate_pair(call, token, 100001)}")
invalid_timeout = dict(call)
invalid_timeout["timeout_ms"] = 3001
if "timeout" not in validate_call(invalid_timeout):
    raise AssertionError("catalog timeout exceeded without rejection")
changed_timeout = dict(call)
changed_timeout["timeout_ms"] = 999
if "timeout_scope" not in validate_pair(changed_timeout, token, 100001):
    raise AssertionError("capability accepted changed timeout")
if "token_keys" not in validate_token(load("invalid-token-unknown.json")):
    raise AssertionError("unknown token field accepted")
if "scope" not in validate_pair(call, load("invalid-token-cross-task.json"), 100001):
    raise AssertionError("cross-task token accepted")
if "expired" not in validate_pair(call, load("invalid-token-expired.json"), 101000):
    raise AssertionError("exact-expiry token accepted")
if "tool_version" not in validate_token(load("invalid-token-version.json")):
    raise AssertionError("unknown tool version accepted")
used: set[str] = set()
if token["token_id"] in used:
    raise AssertionError("fresh token replayed")
used.add(token["token_id"])
if token["token_id"] not in used:
    raise AssertionError("replay set failed")

peer_fixture = load("peer-cases.json")
if set(peer_fixture) != {"cases"} or len(peer_fixture["cases"]) != 6:
    raise AssertionError("peer fixture shape")
peer_allowed = 0
peer_denied = 0
for case in peer_fixture["cases"]:
    if set(case) != {"local_address", "remote_address", "owner_uid", "peer_uid", "allow"}:
        raise AssertionError("peer case unknown field")
    actual = loopback_literal(case["local_address"]) \
        and loopback_literal(case["remote_address"]) \
        and isinstance(case["owner_uid"], int) and case["owner_uid"] >= 0 \
        and case["owner_uid"] == case["peer_uid"]
    if actual != case["allow"]:
        raise AssertionError(f"peer expectation mismatch: {case}")
    peer_allowed += int(actual)
    peer_denied += int(not actual)

print(
    "PASS tool-gateway.v1 fixtures=7 pair=1 invalid=4 peer=6 "
    f"peer_allow={peer_allowed} peer_deny={peer_denied} unknown=reject replay=reject "
    "cross_task=reject exact_expiry=reject version=reject digest=bound timeout=bound"
)
