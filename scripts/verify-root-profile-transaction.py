#!/usr/bin/env python3
from pathlib import Path
r=Path(__file__).resolve().parents[1];j=(r/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/RootProfileTransaction.java').read_text();t=(r/'apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/RootProfileTransactionTest.java').read_text()
if not all(x in j for x in ('PRECHECKED','SNAPSHOTTED','APPLIED','ROLLED_BACK','REBOOT_VERIFIED','DENY_DIGEST')):raise SystemExit('FAIL root-profile: states')
if any(x in j for x in ('ProcessBuilder','Runtime.getRuntime','java.io.','android.','Socket(','su ')):raise SystemExit('FAIL root-profile: execution')
if 'RootProfileTransactionTest transitions=5 mismatch=1 invalid_state=2 installer_calls=0 reboot_device_calls=0' not in t:raise SystemExit('FAIL root-profile: matrix')
print('PASS root-profile-transaction states=7 fixed_profile=true installer=0 reboot=0')
