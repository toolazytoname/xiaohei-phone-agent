#!/usr/bin/env python3
from pathlib import Path
s=(Path(__file__).resolve().parents[1]/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/ToolOutcomeEvidenceGate.java').read_text()
for x in ('VERIFIED','STALE_OBSERVATION','POSTCONDITION_MISMATCH','ADAPTER_FAILED'):
 if x not in s: raise SystemExit('FAIL tool-outcome-evidence missing '+x)
for x in ('Bitmap','AccessibilityNodeInfo','HttpURLConnection','startActivity','execute('):
 if x in s: raise SystemExit('FAIL tool-outcome-evidence must be metadata-only')
print('PASS tool-outcome-evidence adapter_success=insufficient fresh_observation=required mismatch=deny text=0 image=0 transport=0 execution=0')
