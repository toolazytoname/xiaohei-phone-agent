#!/usr/bin/env python3
"""Static enforcement for TOOL-002 loopback capability authorization boundary."""

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei"
GATEWAY = (JAVA / "ToolGateway.java").read_text(encoding="utf-8")
CONFIRMATION = (JAVA / "FreshConfirmationGate.java").read_text(encoding="utf-8")
TEST = (ROOT / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/ToolGatewayTest.java").read_text(encoding="utf-8")
TOKEN_SCHEMA = (ROOT / "contracts/capability-token.v1.schema.json").read_text(encoding="utf-8")
CALL_SCHEMA = (ROOT / "contracts/tool-call.v1.schema.json").read_text(encoding="utf-8")
UI = "\n".join((JAVA / name).read_text(encoding="utf-8") for name in (
    "MainActivity.java", "ConversationActivity.java", "AgentActivity.java",
))


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL loopback tool gateway boundary: {message}")


require("MIN_TOKEN_TTL_MS = 1000L" in GATEWAY and "MAX_TOKEN_TTL_MS = 30000L" in GATEWAY,
        "one-to-thirty-second capability window")
require("MAX_ACTIVE_TOKENS = 16" in GATEWAY, "bounded active capability set")
require("MAX_REPLAY_RECORDS = 256" in GATEWAY
        and "active.size() + spentTokens.size() >= MAX_REPLAY_RECORDS" in GATEWAY,
        "bounded fail-closed replay memory")
require("loopbackLiteral(peer.localAddress)" in GATEWAY
        and "loopbackLiteral(peer.remoteAddress)" in GATEWAY,
        "both accepted-socket endpoints must be numeric loopback")
require("peer.ownerUid != peer.peerUid" in GATEWAY, "same-UID peer enforcement")
require('"localhost"' not in GATEWAY, "hostname is not trusted as loopback evidence")
require("new byte[16]" in GATEWAY and "SecureRandom" in GATEWAY,
        "default opaque token id has 128 random bits")
require("takeCapabilityReceipt()" in CONFIRMATION and "takeCapabilityReceipt()" in GATEWAY,
        "one successful confirmation result can be exchanged once")
for binding in ("taskId", "requestId", "planId", "callId", "toolVersion", "risk", "audience"):
    require(binding in GATEWAY, f"capability binding {binding}")
require("MessageDigest.getInstance(\"SHA-256\")" in GATEWAY
        and "new TreeMap<>(call.arguments)" in GATEWAY,
        "deterministic salted full-call digest")
require("call.requestedAtElapsedMs > nowMs" in GATEWAY
        and "nowMs - call.requestedAtElapsedMs > 60000L" in GATEWAY,
        "future and stale calls rejected before token issue")
require('this.persistence = "memory_only"' in GATEWAY and "this.publicLogSafe = false" in GATEWAY,
        "runtime token mirrors private memory-only contract")
require("nowMs < token.issuedAtElapsedMs" in GATEWAY
        and "nowMs >= token.expiresAtElapsedMs" in GATEWAY,
        "clock rollback and exact expiry fail closed")
require("spentTokens" in GATEWAY and "spentIdempotencyKeys" in GATEWAY,
        "token and per-task idempotency replay memory")
require("revokeAll()" in GATEWAY and "active.clear()" in GATEWAY,
        "global in-memory capability revocation")
require("this.modelCalls = 0" in GATEWAY and "this.actionCalls = 0" in GATEWAY
        and "this.executionCalls = 0" in GATEWAY,
        "authorization core performs no downstream calls")

for forbidden in (
    "ActionDispatcher", "PhoneAgentClient", "ConversationClient", "startActivity",
    "startService", "sendBroadcast", "ProcessBuilder", "Runtime.getRuntime",
    "ServerSocket", "Socket(", "SharedPreferences", "FileOutputStream", "SQLite", "Log.",
):
    require(forbidden not in GATEWAY, f"no adapter/listener/platform/persistence path: {forbidden}")

require("ToolGateway" not in UI, "TOOL-002 adds no UI/executor wiring")
require("allow_once=10 non_local=10 confirmation=5 scope_change=7 catalog_change=3" in TEST,
        "deterministic authorization matrix")
require("invalid_call=5 expiry=5 replay=5 token_ttl=1..30s secure_default=128bit" in TEST,
        "time/replay/random-source matrix")
require("model_calls=0 action_calls=0 execution_paths=0" in TEST,
        "declared zero-execution acceptance")
for field in ("confirmation_id", "request_id", "plan_id", "call_id", "call_digest",
              "issued_at_elapsed_ms", "expires_at_elapsed_ms", "ttl_ms"):
    require(f'"{field}"' in TOKEN_SCHEMA, f"public token binding {field}")
require('"persistence": {"const": "memory_only"}' in TOKEN_SCHEMA
        and '"public_log_safe": {"const": false}' in TOKEN_SCHEMA,
        "public token is memory-only and non-public")
require('"request_id"' in CALL_SCHEMA and '"plan_id"' in CALL_SCHEMA
        and '"audience"' in CALL_SCHEMA and '"public_log_safe": {"const": false}' in CALL_SCHEMA,
        "public call carries full private scope")

print("PASS loopback tool gateway cases=50 allow=10 non_local=10 confirmation=5 changed=10 invalid_call=5 expiry=5 replay=5 ttl=1..30s execution_paths=0 ui_wired=0")
