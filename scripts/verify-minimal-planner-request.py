#!/usr/bin/env python3
import re
from pathlib import Path

source = (Path(__file__).resolve().parents[1]
          / 'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/MinimalPlannerRequest.java').read_text()
fields = set(re.findall(r'^\s*final\s+(?:String|boolean|int)\s+(\w+)\s*;', source, re.M))
expected = {'action', 'dryRun', 'stepBudget', 'timeoutMs', 'catalogVersion'}
if fields != expected:
    raise SystemExit('FAIL minimal planner envelope fields must be exactly ' + ','.join(sorted(expected)))
if re.search(r'\b(?:URL|HttpURLConnection|OkHttp|Socket|execute|startActivity)\b', source):
    raise SystemExit('FAIL minimal planner must not gain transport or execution capability')
print('PASS minimal-planner-request fixed_fields=5 text=0 ui_data=0 paths=0 credentials=0 transport=0 execution=0')
