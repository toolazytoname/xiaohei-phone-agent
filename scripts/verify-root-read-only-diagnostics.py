#!/usr/bin/env python3
from pathlib import Path
r=Path(__file__).resolve().parents[1];j=(r/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/RootReadOnlyDiagnostics.java').read_text();t=(r/'apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/RootReadOnlyDiagnosticsTest.java').read_text();s=(r/'contracts/root-diagnostics.v1.schema.json').read_text()
if not all(x in j for x in ('SERVICE, Category.PORT, Category.PACKAGE, Category.PROFILE','Category.BATTERY','Category.AUDIO','State.UNKNOWN','modelCalls = 0, executionCalls = 0')):raise SystemExit('FAIL root-diagnostics: fixed coverage')
if any(x in j for x in ('ProcessBuilder','Runtime.getRuntime','Socket(','HttpURLConnection','su -c','android.','java.io.')):raise SystemExit('FAIL root-diagnostics: execution')
if 'RootReadOnlyDiagnosticsTest categories=6 bounded_entries=4 content_fields=0 execution_paths=0' not in t:raise SystemExit('FAIL root-diagnostics: matrix')
if any(x not in s for x in ('"maxItems":4','"public_log_safe":{"const":true}','"service"','"profile"')):raise SystemExit('FAIL root-diagnostics: schema')
print('PASS root-read-only-diagnostics categories=6 max_entries=4 raw_content=0 execution=0')
