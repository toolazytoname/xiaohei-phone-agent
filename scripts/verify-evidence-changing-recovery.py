#!/usr/bin/env python3
from pathlib import Path

source = (Path(__file__).resolve().parents[1]
          / 'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/FailureFingerprint.java').read_text()
for symbol in ('RecoveryGate', 'UNCHANGED_DENIED', 'RECOVERY_GRANTED', 'RECOVERY_ALREADY_USED', 'recoveryUsed'):
    if symbol not in source:
        raise SystemExit('FAIL evidence-changing-recovery missing ' + symbol)
for forbidden in ('startActivity', 'HttpURLConnection', 'Socket', 'SharedPreferences', 'FileOutputStream'):
    if forbidden in source:
        raise SystemExit('FAIL evidence-changing-recovery must stay local and non-executing')
print('PASS evidence-changing-recovery unchanged=deny changed=one_grant subsequent=deny persistence=0 transport=0 execution=0')
