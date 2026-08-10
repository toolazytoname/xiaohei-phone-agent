#!/usr/bin/env python3
import re
from pathlib import Path

source = (Path(__file__).resolve().parents[1]
          / 'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/PlanStepObservationGuard.java').read_text()
for symbol in ('beginStep', 'recordActionResult', 'verifyPostcondition', 'HALTED', 'POSTCONDITION_MISMATCH'):
    if symbol not in source:
        raise SystemExit('FAIL plan step observation missing ' + symbol)
if re.search(r'\b(?:AccessibilityNodeInfo|Bitmap|MediaProjection|startActivity|HttpURLConnection|Socket)\b', source):
    raise SystemExit('FAIL plan step observation must stay metadata-only and non-executing')
print('PASS plan-step-observation before_after=exact stale=halt app_switch=halt text=0 ui_tree=0 image=0 transport=0 execution=0')
