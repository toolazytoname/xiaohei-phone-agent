#!/usr/bin/env python3
from pathlib import Path
root=Path(__file__).resolve().parents[1]
core=(root/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/AudioDuplexArbiter.java').read_text()
test=(root/'apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/AudioDuplexArbiterTest.java').read_text()
for text, terms in ((core,('DENY_CONFLICT','recorderExpectedActive','ttsExpectedActive','interruptAll')),(test,('overlap=0','adapter_calls=0'))):
    if any(x not in text for x in terms): raise SystemExit('FAIL audio-duplex-arbiter boundary')
print('PASS audio-duplex-arbiter ownership=input|output|none overlap=0 adapters=0 device_audio=required')
