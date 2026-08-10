#!/usr/bin/env python3
from pathlib import Path
r=Path(__file__).resolve().parents[1];j=(r/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/RootServiceLifecyclePolicy.java').read_text();t=(r/'apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/RootServiceLifecyclePolicyTest.java').read_text();s=(r/'contracts/root-service-lifecycle.v1.schema.json').read_text()
if not all(x in j for x in ('DENY_PACKAGE','DENY_PROCESS','DENY_PID','DENY_PORT','ALLOW_DRY_RUN','freshConfirmation')):raise SystemExit('FAIL root-lifecycle: exact identity')
if any(x in j for x in ('ProcessBuilder','Runtime.getRuntime','Socket(','HttpURLConnection','android.','kill','su ')):raise SystemExit('FAIL root-lifecycle: execution')
if 'RootServiceLifecyclePolicyTest allow=dry_run deny=7 package_pid_port_process=exact signal_calls=0 execution_paths=0' not in t:raise SystemExit('FAIL root-lifecycle: matrix')
if any(x not in s for x in ('"action":{"const":"stop"}','"fresh_confirmation":{"const":true}','"maximum":65535','"public_log_safe":{"const":false}')):raise SystemExit('FAIL root-lifecycle: schema')
print('PASS root-service-lifecycle stop=dry_run exact_identity=4 confirmation=fresh signals=0 execution=0')
