#!/usr/bin/env python3
"""Static enforcement for OC-003 task-private workspace isolation."""

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei"
BOUNDARY = (JAVA / "OpenCodeWorkspaceBoundary.java").read_text(encoding="utf-8")
TEST = (ROOT / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/OpenCodeWorkspaceBoundaryTest.java").read_text(encoding="utf-8")
SCHEMA = (ROOT / "contracts/opencode-workspace-lease.v1.schema.json").read_text(encoding="utf-8")
UI = "\n".join((JAVA / name).read_text(encoding="utf-8") for name in (
    "MainActivity.java", "ConversationActivity.java", "AgentActivity.java",
))


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL opencode workspace boundary: {message}")


require('WORKSPACES_DIR = "xiaohei-opencode-tasks"' in BOUNDARY
        and 'INPUT_DIR = "input"' in BOUNDARY and 'OUTPUT_DIR = "output"' in BOUNDARY,
        "fixed task-private input/output roots")
require("validTask(task)" in BOUNDARY and "OPENCODE_GATEWAY" in BOUNDARY,
        "only a valid OpenCode task proposal allocates a lease")
require("parsed.isAbsolute()" in BOUNDARY and '"..".equals(name)' in BOUNDARY,
        "absolute and traversal paths fail closed")
require("Files.isSymbolicLink" in BOUNDARY and "containsSymbolicLink" in BOUNDARY,
        "existing symlink components fail closed")
require("candidate.startsWith(root)" in BOUNDARY and "normalizedTarget.startsWith(normalizedRoot)" in BOUNDARY,
        "resolved paths must remain below the leased root")
require("PERSISTENCE = \"private_app_storage\"" in BOUNDARY
        and "PATH_EXPOSURE = \"none\"" in BOUNDARY,
        "paths stay private to app storage")
for forbidden in (
    "ProcessBuilder", "Runtime.getRuntime", "startActivity", "startService", "sendBroadcast",
    "Socket(", "HttpURLConnection", "Files.read", "Files.write", "newBufferedReader", "newBufferedWriter",
):
    require(forbidden not in BOUNDARY, f"no runner/content/platform path: {forbidden}")
require("OpenCodeWorkspaceBoundary" not in UI, "OC-003 adds no UI wiring")
require("OpenCodeWorkspaceBoundaryTest leases=2 safe_paths=4 traversal=7 " in TEST
        and "symlink=3 cross_task=2" in TEST,
        "real temporary-filesystem isolation matrix")
require("content_reads=0 content_writes=0 process_calls=0" in TEST,
        "no content or process execution")
for field in ("task_id", "allowed_areas", "persistence", "path_exposure", "public_log_safe"):
    require(f'"{field}"' in SCHEMA, f"lease schema field {field}")
for forbidden in ("path", "workspace", "root", "command", "token"):
    require(f'"{forbidden}"' not in SCHEMA, f"lease schema must not expose {forbidden}")

print("PASS opencode-workspace boundary leases=2 safe=4 traversal=7 symlink=3 cross_task=2 "
      "content_paths=0 process_paths=0 ui_wired=0")
