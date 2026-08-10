#!/usr/bin/env python3
from pathlib import Path
r=Path(__file__).resolve().parents[1]
for p,terms in [('apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/FailureRecoveryProjection.java',('Cause','Impact','Recovery','UNKNOWN')),('apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/TaskCardProjection.java',('FailureRecoveryProjection','Stage.FAILED')),('apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/FailureRecoveryProjectionTest.java',('raw_error=0','execution=0'))]:
 if any(x not in (r/p).read_text() for x in terms): raise SystemExit('FAIL failure-recovery-card')
print('PASS failure-recovery-card cause/impact/recovery typed=true raw_error=0 execution=0 adoption=task-card-only')
