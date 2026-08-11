#!/usr/bin/env python3
from pathlib import Path
root=Path(__file__).resolve().parents[1]
core=(root/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/AudioDuplexArbiter.java').read_text()
test=(root/'apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/AudioDuplexArbiterTest.java').read_text()
process=(root/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/ProcessAudioDuplex.java').read_text()
process_test=(root/'apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/ProcessAudioDuplexTest.java').read_text()
tts=(root/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/SystemTtsAdapter.java').read_text()
local_asr=(root/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/XiaoheiRecognitionService.java').read_text()
session=(root/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/VoiceCommandSession.java').read_text()
cpu_kws=(root/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/CpuWakewordService.java').read_text()
for text, terms in (
    (core,('DENY_CONFLICT','recorderExpectedActive','ttsExpectedActive','interruptAll')),
    (test,('overlap=0','adapter_calls=0')),
    (process,('class Lease','acquireInput()','acquireOutput()','lease != active','arbiter.releaseInput()','arbiter.releaseOutput()')),
    (process_test,('leases=2','conflicts=4','stale_release=2','overlap=0')),
    (tts,('ProcessAudioDuplex.Lease outputLease','acquireOutput()','releaseOutput()')),
    (local_asr,('ProcessAudioDuplex.Lease inputLease','acquireInput()','release(inputLease)')),
    (session,('systemInputLease','acquireInput()','releaseSystemInput()','tts_output_active','catch (RuntimeException unavailable)')),
    (cpu_kws,('ProcessAudioDuplex.Lease inputLease','acquireInput()','release(lease)','capture_start_cancelled before_audio_start=true')),
):
    if any(x not in text for x in terms): raise SystemExit('FAIL audio-duplex-arbiter boundary')
print('PASS audio-duplex-arbiter ownership=input|output|none overlap=0 leases=identity-bound adapters=tts+local_asr+system_asr+cpu_kws device_audio=required')
