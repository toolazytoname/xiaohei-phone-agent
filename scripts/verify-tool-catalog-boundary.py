#!/usr/bin/env python3
"""Static enforcement for TOOL-001 versioned catalog and zero-execution boundary."""

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei"
CATALOG = (JAVA / "ToolCatalog.java").read_text(encoding="utf-8")
TEST = (ROOT / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/ToolCatalogTest.java").read_text(encoding="utf-8")
PLAN = (JAVA / "TaskPlanValidator.java").read_text(encoding="utf-8")
UI = "\n".join((JAVA / name).read_text(encoding="utf-8") for name in (
    "MainActivity.java", "ConversationActivity.java", "AgentActivity.java",
))


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL tool catalog boundary: {message}")


require("SCHEMA_VERSION = 1" in CATALOG and "TOOL_VERSION = 1" in CATALOG
        and "MAX_TOOLS = 64" in CATALOG, "catalog version and size cap")
for value in ("OBSERVE", "LOW", "REVERSIBLE", "HIGH"):
    require(value in CATALOG, f"risk {value}")
for value in ("ANDROID_GATEWAY", "OPENCODE_GATEWAY", "ROOT_BROKER"):
    require(value in CATALOG, f"audience {value}")
for value in ("NONE", "RESTORE_SNAPSHOT", "REVERSE_TOOL", "MANUAL"):
    require(value in CATALOG, f"rollback mode {value}")
for field in ("inputSchema", "outputSchema", "rollbackMode", "rollbackTool", "audience", "timeoutMs"):
    require(field in CATALOG, f"descriptor field {field}")

expected_tools = {
    "android.open_settings", "android.open_gallery", "android.open_dialer",
    "android.adjust_volume", "android.observe", "android.media_test_collection", "android.calendar_test_account",
}
for tool in expected_tools:
    require(f'"{tool}"' in CATALOG, f"reviewed tool {tool}")
require(CATALOG.count("descriptors.add(descriptor(") == 7, "exact seven-entry built-in catalog")
require("Collections.unmodifiableList" in CATALOG and "Collections.unmodifiableMap" in CATALOG,
        "immutable built-in views")
require("identities.contains(key(descriptor.rollbackTool, descriptor.version))" in CATALOG,
        "rollback target must resolve in the same versioned catalog")
require('validSchema(descriptor.inputSchema, "input")' in CATALOG
        and 'validSchema(descriptor.outputSchema, "output")' in CATALOG,
        "input and output schema directions cannot be swapped")
require("descriptor.timeoutMs < 100 || descriptor.timeoutMs > 60000" in CATALOG,
        "bounded per-tool timeout metadata")
require("ToolCatalog.risk(step.tool)" in PLAN, "plan validator consumes reviewed risk metadata")

for forbidden in (
    "PhoneAgentClient", "ConversationClient", "ActionDispatcher", "ToolGateway",
    "startActivity", "startService", "sendBroadcast", "ProcessBuilder",
    "Runtime.getRuntime", "java.net.", "import android.", "System.out", "Log.",
    "SharedPreferences", "FileOutputStream", "SQLite",
):
    require(forbidden not in CATALOG, f"no execution/platform/persistence path: {forbidden}")

require("ToolCatalog" not in UI, "catalog metadata adds no new UI execution wiring")
require("descriptors=7 lookup=7 duplicate=3 unknown_version=3 missing=5 rollback=4" in TEST,
        "catalog Java acceptance matrix")
require(TEST.count("expectDescriptor(") == 8,
        "seven exact descriptor assertions plus helper declaration")
require("unknown_tool=5 risk_mismatch=4 immutable=true execution_paths=0" in TEST,
        "unknown/risk/immutability/zero execution acceptance")

print("PASS tool catalog boundary descriptors=7 schemas=10 rollback=bound immutable=true execution_paths=0 ui_wired=0")
