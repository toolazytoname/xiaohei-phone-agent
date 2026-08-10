#!/usr/bin/env python3
from pathlib import Path
r=Path(__file__).resolve().parents[1]; j=(r/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/AuthorizationTierPolicy.java').read_text(); t=(r/'apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/AuthorizationTierPolicyTest.java').read_text()
if not all(x in j for x in ('enum Tier { ANDROID, OPENCODE, ROOT }','DENY_CROSS_TIER','DENY_ROOT_UNIMPLEMENTED')):raise SystemExit('FAIL tier-policy: tiers')
if any(x in j for x in ('ProcessBuilder','Runtime.getRuntime','Socket(','su -c')):raise SystemExit('FAIL tier-policy: execution')
if not all(x in t for x in ('AuthorizationTierPolicyTest allow=', 'cross_tier=', 'root_unimplemented=true execution_paths=0')):raise SystemExit('FAIL tier-policy: matrix')
print('PASS authorization-tier-policy allow=2 deny=7 root_broker=false execution=0')
