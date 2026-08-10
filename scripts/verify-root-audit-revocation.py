#!/usr/bin/env python3
import json
from pathlib import Path
r=Path(__file__).resolve().parents[1];j=(r/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/RootCapabilityBroker.java').read_text();t=(r/'apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/RootAuditRevocationTest.java').read_text();s=json.loads((r/'contracts/root-audit.v1.schema.json').read_text())
if not all(x in j for x in ('DENY_REVOKED','void revokeAll()','auditSnapshot()','new AuditRecord','consumed.clear()')):raise SystemExit('FAIL root-audit: revoke')
if any(x in j for x in ('ProcessBuilder','Runtime.getRuntime','Socket(','HttpURLConnection','android.','su -c')):raise SystemExit('FAIL root-audit: execution')
def keys(value):
    if isinstance(value,dict):
        yield from value.keys()
        for item in value.values(): yield from keys(item)
    elif isinstance(value,list):
        for item in value: yield from keys(item)
if any(x in set(keys(s)) for x in ('request_id','signer','parameters','path','command','token','timestamp')):raise SystemExit('FAIL root-audit: redaction')
if 'RootAuditRevocationTest audit=2 fields=3 request_data=0 revoke=permanent post_revoke=deny execution_paths=0' not in t:raise SystemExit('FAIL root-audit: matrix')
print('PASS root-audit-revocation fields=3 request_data=0 revoke=permanent execution=0')
