#!/usr/bin/env python3
"""Static enforcement for OC-002's no-generic-shell task protocol."""

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei"
PROTOCOL = (JAVA / "OpenCodeTaskProtocol.java").read_text(encoding="utf-8")
REQUEST = (JAVA / "UnconfirmedActionRequest.java").read_text(encoding="utf-8")
TEST = (ROOT / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/OpenCodeTaskProtocolTest.java").read_text(encoding="utf-8")
SCHEMA = (ROOT / "contracts/opencode-task.v1.schema.json").read_text(encoding="utf-8")
UI = "\n".join((JAVA / name).read_text(encoding="utf-8") for name in (
    "MainActivity.java", "ConversationActivity.java", "AgentActivity.java",
))


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL opencode task boundary: {message}")


require("enum Kind { PROJECT_SUMMARY, TEST_DIAGNOSIS, CONTROLLED_FILE_ORGANIZATION }" in PROTOCOL,
        "only three reviewed task kinds")
require("SOURCE = \"typed_user\"" in PROTOCOL
        and "request.userTextForPlanner()" in PROTOCOL,
        "proposal must originate from an existing typed user request")
require("request.requiresConfirmation" in PROTOCOL and "request.dryRun" in PROTOCOL
        and "CONFIRMATION_STATE.equals(request.confirmationState)" in PROTOCOL,
        "only pending dry-run confirmation requests can cross the boundary")
require("ToolCatalog.Audience.OPENCODE_GATEWAY" in PROTOCOL,
        "OpenCode has its own audience")
require("this.publicLogSafe = false" in PROTOCOL
        and "Collections.singletonList(\"instruction\")" in PROTOCOL,
        "instruction is private and redacted")
require("instructionForExecutor()" in PROTOCOL and "safeMetadata()" in PROTOCOL,
        "private instruction and public metadata remain separate")
for forbidden in (
    "ProcessBuilder", "Runtime.getRuntime", "startActivity", "startService", "sendBroadcast",
    "ServerSocket", "Socket(", "HttpURLConnection", "SharedPreferences", "SQLite", "su -c",
):
    require(forbidden not in PROTOCOL, f"no execution/platform path: {forbidden}")
require("OpenCodeTaskProtocol" not in UI, "OC-002 adds no UI wiring")
require("OpenCodeTaskProtocolTest kinds=3 instruction_inert=10 invalid=6" in TEST,
        "deterministic protocol matrix")
require("shell_authority=0" in TEST and "execution_paths=0" in TEST,
        "no generic shell or execution evidence")
require("fromConversationMessage" in REQUEST and "Role.USER" in REQUEST,
        "source boundary is enforced by existing typed-user factory")
for field in (
    "task_id", "request_id", "plan_id", "kind", "source", "instruction", "dry_run",
    "requires_confirmation", "confirmation_state", "execution_state", "audience", "redaction",
):
    require(f'"{field}"' in SCHEMA, f"schema field {field}")
for forbidden in ("command", "argv", "environment", "cwd", "workspace", "token", "root"):
    require(f'"{forbidden}"' not in SCHEMA, f"schema must not expose {forbidden}")

print("PASS opencode-task boundary kinds=3 typed_user_only=true pending_only=true "
      "generic_shell=0 execution_paths=0 ui_wired=0")
