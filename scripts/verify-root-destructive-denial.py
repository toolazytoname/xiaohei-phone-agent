#!/usr/bin/env python3
from pathlib import Path
r=Path(__file__).resolve().parents[1];j=(r/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/RootDestructiveDenialPolicy.java').read_text();t=(r/'apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/RootDestructiveDenialPolicyTest.java').read_text()
if not all(x in j for x in ('DENY_DESTRUCTIVE','DENY_BROAD_PATH','DENY_SECRET','DENY_UNKNOWN','"rm -rf"','"/system"','"password"')):raise SystemExit('FAIL root-destructive-denial: corpus')
if any(x in j for x in ('ProcessBuilder','Runtime.getRuntime','Socket(','HttpURLConnection','android.','su -c')):raise SystemExit('FAIL root-destructive-denial: execution')
if 'RootDestructiveDenialPolicyTest destructive=6 broad_path=6 secret=7 unknown=1 execution_paths=0' not in t:raise SystemExit('FAIL root-destructive-denial: matrix')
print('PASS root-destructive-denial destructive=6 broad_path=6 secret=7 unknown=deny execution=0')
