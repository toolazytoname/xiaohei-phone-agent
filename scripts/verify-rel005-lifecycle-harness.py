#!/usr/bin/env python3
from pathlib import Path

root = Path(__file__).resolve().parents[1]
script = (root / "scripts/test-rel005-emulator-lifecycle.sh").read_text()
required = (
    '[[ "$serial" == emulator-* ]]',
    'trap cleanup EXIT',
    'assert_no_xiaohei force-stop',
    'assert_no_xiaohei cold-start',
    'assert_no_xiaohei reboot',
    'pre-reboot',
    'post-reboot',
    'expected_boot_id',
    '/proc/sys/kernel/random/boot_id',
    'FAIL emulator reboot was not observed',
    'dumpsys activity services',
    'dumpsys media.audio_flinger',
    'shell svc power reboot',
    '"${adb_cmd[@]}" uninstall "$package"',
    'network_model=not_exercised',
)
missing = [term for term in required if term not in script]
if missing:
    raise SystemExit('FAIL rel005 lifecycle harness missing ' + ', '.join(missing))
if '5c9a424d' in script or 'OnePlus' in script:
    raise SystemExit('FAIL rel005 lifecycle harness must not name a physical device')
if ' shell monkey ' in script:
    raise SystemExit('FAIL rel005 lifecycle harness must use an explicit activity, not launcher discovery')
if 'shell am start -W -n "$package/.MainActivity"' not in script:
    raise SystemExit('FAIL rel005 lifecycle harness missing explicit cold-start component')
print('PASS rel005-lifecycle-harness serial=emulator_only phases=pre+post boot_id=bound terminal_checks=3 cleanup=trap network_model=not_exercised')
