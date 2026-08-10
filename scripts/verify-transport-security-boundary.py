#!/usr/bin/env python3
from pathlib import Path

root = Path(__file__).resolve().parents[1]
for name, terms in {
    "docs/transport-security-boundary.md": ("Public model transport", "On-phone relay", "Redirects", "Tool authorization", "Independent-device TLS interception"),
    "docs/transport-security-boundary.zh-CN.md": ("公网模型传输", "手机本地中转", "重定向", "工具授权", "独立设备上的不可信 CA"),
    "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/BoundedConversationTransport.java": ("setInstanceFollowRedirects(false)", "Proxy.NO_PROXY", "REDIRECT_REJECTED"),
    "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/ToolGateway.java": ("validatePeer", "loopbackLiteral", "PEER_UID_MISMATCH", "IDEMPOTENCY_REPLAY"),
}.items():
    text = (root / name).read_text(encoding="utf-8")
    missing = [term for term in terms if term not in text]
    if missing:
        raise SystemExit(f"FAIL transport-security {name}: {', '.join(missing)}")
network = (root / "apps/android/xiaohei-android/res/xml/network_security_config.xml").read_text(encoding="utf-8")
if 'cleartextTrafficPermitted="false"' not in network:
    raise SystemExit("FAIL transport-security cleartext default")
print("PASS transport-security tls-default=system redirect=deny loopback=no_proxy uid/replay=bounded device_mitm=required")
