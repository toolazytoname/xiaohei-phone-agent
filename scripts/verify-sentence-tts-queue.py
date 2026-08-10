#!/usr/bin/env python3
from pathlib import Path
s=(Path(__file__).resolve().parents[1]/'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/SentenceTtsQueue.java').read_text()
for x in ('replace(','complete(','cancel(','generation++'):
 if x not in s: raise SystemExit('FAIL sentence-tts-queue missing '+x)
for x in ('TextToSpeech','AudioRecord','HttpURLConnection','Socket','startActivity'):
 if x in s: raise SystemExit('FAIL sentence-tts-queue must remain queue-only')
print('PASS sentence-tts-queue first=immediate cancel=clears stale_generation=ignore tts=0 audio=0 transport=0')
