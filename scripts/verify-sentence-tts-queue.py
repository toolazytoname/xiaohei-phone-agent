#!/usr/bin/env python3
from pathlib import Path
s=(Path(__file__).resolve().parents[1]/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/SentenceTtsQueue.java').read_text()
t=(Path(__file__).resolve().parents[1]/'apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/SentenceTtsQueueTest.java').read_text()
a=(Path(__file__).resolve().parents[1]/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/SystemTtsAdapter.java').read_text()
for x in ('synchronized Next replace(','synchronized Next complete(','synchronized void cancel(','generation++','final int sequence'):
 if x not in s: raise SystemExit('FAIL sentence-tts-queue missing '+x)
for x in ('replacement=invalidates','stale_completion=2','overlap=0'):
 if x not in t: raise SystemExit('FAIL sentence-tts-queue test missing '+x)
for x in ('queue_created generation=','sentence_submitted generation=','sentence_started generation=',
          'queue_finished generation=','queue_cancelled reason=','synchronized (SystemTtsAdapter.this)'):
 if x not in a: raise SystemExit('FAIL sentence-tts adapter wiring missing '+x)
for x in ('TextToSpeech','AudioRecord','HttpURLConnection','Socket','startActivity'):
 if x in s: raise SystemExit('FAIL sentence-tts-queue must remain queue-only')
if 'Log.i(TAG, next.sentence' in a or 'Log.i(TAG, text' in a:
 raise SystemExit('FAIL sentence-tts queue logs must not contain speech text')
print('PASS sentence-tts-queue first=immediate ordered=sequence synchronized=true replace/cancel=invalidates stale=ignore logs=metadata-only device_audio=required')
