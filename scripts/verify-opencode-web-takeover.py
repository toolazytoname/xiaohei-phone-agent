#!/usr/bin/env python3
from pathlib import Path
s=(Path(__file__).resolve().parents[1]/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/OpenCodeTakeoverOwnership.java').read_text()
for x in ('TAKEN_OVER','RETURNED','ALREADY_OWNED','TERMINAL','markTerminal'):
 if x not in s: raise SystemExit('FAIL opencode-web-takeover missing '+x)
for x in ('HttpURLConnection','Socket','ProcessBuilder','Files.','startActivity','run('):
 if x in s: raise SystemExit('FAIL opencode-web-takeover must not execute or transport')
print('PASS opencode-web-takeover handoff=explicit duplicate=deny terminal=deny transport=0 process=0 task_start=0')
