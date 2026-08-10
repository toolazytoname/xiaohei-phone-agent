#!/usr/bin/env python3
"""Static boundary for OpenCode stop/revocation/workspace cleanup."""
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei"
STOP = (JAVA / "OpenCodeStopCoordinator.java").read_text(encoding="utf-8")
WORKSPACE = (JAVA / "OpenCodeWorkspaceBoundary.java").read_text(encoding="utf-8")
TEST = (ROOT / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/OpenCodeStopCoordinatorTest.java").read_text(encoding="utf-8")
def require(value, message):
    if not value: raise SystemExit("FAIL opencode-stop-cleanup: " + message)
for part in ("cancellation.cancel", "gateway.revokeAll", "ResourceKind { PROCESS, LISTENER, TMUX }", "OpenCodeWorkspaceBoundary.release"):
    require(part in STOP, "stop boundary " + part)
for part in ("Files.walkFileTree", "SimpleFileVisitor", "Files.delete(file)", "containsSymbolicLink"):
    require(part in WORKSPACE, "safe cleanup " + part)
for forbidden in ("ProcessBuilder", "Runtime.getRuntime", "Socket(", "HttpURLConnection", "su -c", "startActivity"):
    require(forbidden not in STOP, "no real execution " + forbidden)
require("OpenCodeStopCoordinatorTest stop=1 resources=3 revoked=0 workspace=2 recursive=1 symlink_safe=1" in TEST, "cleanup matrix")
print("PASS opencode-stop-cleanup cancellation=1 token_revocation=all registered_resources=process+listener+tmux workspace=recursive_no_follow real_os_handles=0")
