#!/usr/bin/env python3
from pathlib import Path

root = Path(__file__).resolve().parents[1]
source = (root / 'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/XiaoheiAccessibilityService.java').read_text()
required = (
    'ApplicationStopHub.Registration globalStopRegistration',
    'armGlobalStopRegistration();',
    'releaseGlobalStopRegistration();',
    'GlobalStopRegistry.Resource.PHONE_AGENT',
    'stopInternal("全局停止")',
    'private void complete(String detail)',
    'private void stopInternal(String reason)',
    '@Override public void onDestroy()',
)
missing = [term for term in required if term not in source]
if missing:
    raise SystemExit('FAIL phone-agent global stop missing ' + ', '.join(missing))
if 'startService(' in source or 'startForegroundService(' in source:
    raise SystemExit('FAIL phone-agent global stop must not start a service')
print('PASS phone-agent-global-stop active_task=registered terminal_destroy=unregistered global_stop=existing_stop_internal service_start=0')
