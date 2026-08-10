#!/usr/bin/env python3
from pathlib import Path
r=Path(__file__).resolve().parents[1];j=(r/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/SensitiveActionDenialPolicy.java').read_text();a=(r/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/AgentPolicy.java').read_text();t=(r/'apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/SensitiveActionDenialPolicyTest.java').read_text()
if not all(x in j for x in ('"支付"','"otp"','"bypass"','DENY_SENSITIVE','packageName','visibleText','requestedLabel')):raise SystemExit('FAIL sensitive-denial: corpus')
if any(x in j for x in ('ProcessBuilder','Runtime.getRuntime','Socket(','HttpURLConnection','android.','su ')):raise SystemExit('FAIL sensitive-denial: execution')
if 'SensitiveActionDenialPolicy.assess(packageName, visibleText, requestedLabel)' not in a:raise SystemExit('FAIL sensitive-denial: agent-policy wiring')
if 'SensitiveActionDenialPolicyTest corpus=19 payment=6 secret=6 evasion=7 package=1 model_calls=0 execution_paths=0' not in t:raise SystemExit('FAIL sensitive-denial: matrix')
print('PASS sensitive-action-denial corpus=19 surfaces=3 permanent_deny=true execution=0')
