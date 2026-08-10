#!/usr/bin/env python3
from pathlib import Path

source = (Path(__file__).resolve().parents[1]
          / 'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/GlobalStopRegistry.java').read_text()
main = (Path(__file__).resolve().parents[1]
        / 'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/MainActivity.java').read_text()
for symbol in ('VOICE', 'DSP', 'CPU_WAKE', 'CONVERSATION', 'PHONE_AGENT', 'TOOL', 'OPENCODE', 'ROOT',
               'STOP_FAILED', 'allResourcesReleased', 'ALREADY_TERMINAL'):
    if symbol not in source:
        raise SystemExit('FAIL global-stop-registry missing ' + symbol)
for forbidden in ('Runtime.getRuntime', 'ProcessBuilder', 'startActivity', 'HttpURLConnection', 'Socket'):
    if forbidden in source:
        raise SystemExit('FAIL global-stop-registry must not discover or execute platform resources')
for symbol in ('requestHomeGlobalStop', 'GlobalStopRegistry.Resource.VOICE',
               'GlobalStopRegistry.Resource.DSP', 'GlobalStopRegistry.Resource.CPU_WAKE'):
    if symbol not in main:
        raise SystemExit('FAIL home global stop wiring missing ' + symbol)
print('PASS global-stop-registry categories=8 stop_once=true failure=visible repeat=deny home_wired=voice+dsp+cpu discovery=0 transport=0 execution=owners_only')
