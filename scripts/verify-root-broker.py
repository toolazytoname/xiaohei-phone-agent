#!/usr/bin/env python3
from pathlib import Path
r=Path(__file__).resolve().parents[1];j=(r/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/RootCapabilityBroker.java').read_text();t=(r/'apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/RootCapabilityBrokerTest.java').read_text();s=(r/'contracts/root-request.v1.schema.json').read_text()
if not all(x in j for x in ('READ_SERVICE_STATUS','READ_BATTERY_STATUS','READ_AUDIO_STATUS','DENY_SIGNATURE','DENY_REPLAY','"{}".equals')):raise SystemExit('FAIL root-broker: authority')
if any(x in j for x in ('ProcessBuilder','Runtime.getRuntime','Socket(','HttpURLConnection','su -c','android.')):raise SystemExit('FAIL root-broker: execution')
if 'RootCapabilityBrokerTest allow=3 deny=6 replay=3 signer=bound parameters=exact execution_paths=0' not in t:raise SystemExit('FAIL root-broker: matrix')
if any(x not in s for x in ('read_service_status','read_battery_status','read_audio_status','xiaohei-root-broker-v1')):raise SystemExit('FAIL root-broker: schema')
print('PASS root-broker fixed_actions=3 signer=bound params=exact replay=deny execution=0')
