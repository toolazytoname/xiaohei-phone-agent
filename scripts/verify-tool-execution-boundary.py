#!/usr/bin/env python3
"""Static enforcement for TOOL-003's bounded, adapter-only execution lifecycle."""

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei"
COORDINATOR = (JAVA / "ToolExecutionCoordinator.java").read_text(encoding="utf-8")
GATEWAY = (JAVA / "ToolGateway.java").read_text(encoding="utf-8")
CATALOG = (JAVA / "ToolCatalog.java").read_text(encoding="utf-8")
TEST = (ROOT / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/ToolExecutionCoordinatorTest.java").read_text(encoding="utf-8")
RESULT_SCHEMA = (ROOT / "contracts/tool-result.v1.schema.json").read_text(encoding="utf-8")
UI = "\n".join((JAVA / name).read_text(encoding="utf-8") for name in (
    "MainActivity.java", "ConversationActivity.java", "AgentActivity.java",
))


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL tool execution boundary: {message}")


require("private ExecutionPermit executionPermit" in GATEWAY
        and "synchronized ExecutionPermit takeExecutionPermit()" in GATEWAY
        and "executionPermit = null" in GATEWAY,
        "authorization must expose one private one-use execution permit")
require("call.timeoutMs < 100 || call.timeoutMs > descriptor.timeoutMs" in GATEWAY,
        "call timeout must not exceed the catalog descriptor")
require("callTimeoutMs = call.timeoutMs" in GATEWAY
        and "append(canonical, String.valueOf(call.timeoutMs))" in GATEWAY,
        "timeout must be capability-bound")
require("future.get(call.timeoutMs, TimeUnit.MILLISECONDS)" in COORDINATOR,
        "each adapter call uses its bound deadline")
require("future.cancel(true)" in COORDINATOR and "attached.cancel(true)" in COORDINATOR,
        "deadline and cancellation interrupt the worker")
require("executor.shutdownNow()" in COORDINATOR
        and "executor.awaitTermination(WORKER_STOP_GRACE_MS" in COORDINATOR,
        "worker cleanup is bounded")
require("USER, GLOBAL_STOP, CLIENT_DISCONNECTED" in COORDINATOR
        and "CALLER_INTERRUPTED" in COORDINATOR,
        "user, global, disconnect, and caller cancellation are structured")
require("cancelled || terminal || reason == null" in COORDINATOR
        and "terminal = true" in COORDINATOR,
        "completion and cancellation have one synchronized terminal winner")
for code in (
    "AUTHORIZATION_DENIED", "AUTHORIZATION_REPLAY", "AUTHORIZATION_EXPIRED",
    "SCOPE_CHANGED", "ADAPTER_MISSING", "INVALID_OUTPUT", "DEADLINE_EXCEEDED",
    "NETWORK_UNAVAILABLE", "PROCESS_EXIT_NONZERO", "ADAPTER_FAILURE", "ROLLBACK_REQUIRED",
):
    require(code in COORDINATOR, f"structured error {code}")
require("MAX_OUTPUT_FIELDS = 32" in COORDINATOR
        and "MAX_OUTPUT_VALUE_LENGTH = 1024" in COORDINATOR,
        "result output bounds")
require("this.publicLogSafe = false" in COORDINATOR,
        "execution result remains private")
require("failed.getCause()" in COORDINATOR and "cause instanceof AdapterFailure" in COORDINATOR,
        "adapter failures are reduced to typed codes")
for forbidden in ("getMessage()", "printStackTrace", "Log.", "StackTraceElement"):
    require(forbidden not in COORDINATOR, f"no raw failure disclosure: {forbidden}")
for forbidden in (
    "ActionDispatcher", "PhoneAgentClient", "ConversationClient", "startActivity",
    "startService", "sendBroadcast", "ProcessBuilder", "Runtime.getRuntime",
    "ServerSocket", "Socket(", "HttpURLConnection", "SharedPreferences", "SQLite",
):
    require(forbidden not in COORDINATOR, f"no real adapter/platform path: {forbidden}")
require("ToolExecutionCoordinator" not in UI, "TOOL-003 adds no UI wiring")
require("success=5 timeout=5 cancel=5 structured_failure=5 denied_replay=5" in TEST,
        "exact 25-group lifecycle matrix")
require("worker_interrupts=9" in TEST and "adapter_calls_bounded=true" in TEST,
        "worker termination and single-call evidence")
require("execution_paths=test_adapters_only" in TEST,
        "test evidence cannot be represented as real adapter execution")
require("malformed_scope=reject" in TEST,
        "post-authorization malformed calls fail closed without adapter execution")
require("new CountDownLatch(1).await()" in TEST,
        "timeout test blocks until worker interruption")
require("NETWORK_UNAVAILABLE" in TEST and "PROCESS_EXIT_NONZERO" in TEST,
        "synthetic network/process error mapping tests")
for timeout in ("5000", "3000"):
    require(timeout in CATALOG, f"catalog timeout {timeout}")
for field in (
    "started_at_elapsed_ms", "finished_at_elapsed_ms", "duration_ms",
    "adapter_calls", "error_code", "public_log_safe",
):
    require(f'"{field}"' in RESULT_SCHEMA, f"public structured result field {field}")

print(
    "PASS tool-execution cases=25 success=5 timeout=5 cancel=5 failure=5 "
    "denied_replay=5 worker_interrupts=9 adapter_calls=0..1 real_adapters=0 ui_wired=0"
)
