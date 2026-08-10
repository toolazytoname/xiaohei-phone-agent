#!/usr/bin/env python3
from pathlib import Path

root = Path(__file__).resolve().parents[1]
source = (root / 'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/AudioInterruptionPolicy.java').read_text()
main = (root / 'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/MainActivity.java').read_text()
for symbol in ('CALL', 'ALARM', 'MEDIA', 'ACTIVITY', 'STOP_AND_RELEASE', 'autoResume = false'):
    if symbol not in source:
        raise SystemExit('FAIL audio-interruption-policy missing ' + symbol)
for forbidden in ('TelephonyManager', 'READ_PHONE_STATE', 'MediaSession', 'AudioRecord', 'TextToSpeech'):
    if forbidden in source:
        raise SystemExit('FAIL audio-interruption-policy must stay signal-only')
if 'AudioInterruptionPolicy.Source.ACTIVITY' not in main:
    raise SystemExit('FAIL home lifecycle must use interruption policy')
print('PASS audio-interruption-policy sources=4 stop_input=true stop_output=true auto_resume=0 home_pause_wired=true phone_metadata=0')
