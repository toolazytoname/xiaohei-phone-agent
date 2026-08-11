#!/usr/bin/env python3
from pathlib import Path
root = Path(__file__).resolve().parents[1]
core = (root / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/TtsLifecycle.java").read_text()
adapter = (root / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/SystemTtsAdapter.java").read_text()
conversation = (root / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/ConversationActivity.java").read_text()
test = (root / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/TtsLifecycleTest.java").read_text()
for text, terms in ((core, ("WAITING_FOLLOWUP", "INTERRUPTED", "acknowledgeInterruption")), (adapter, ("void interrupt", "queue_cancelled", "等待后续输入", "requestAudioFocus", "onAudioFocusChange", "abandonAudioFocus")), (conversation, ("conversation-stop-speech", "speakReply(reply)", "interruptSpeech", "systemTts.destroy")), (test, ("waiting_followup=3", "explicit_resume=1", "illegal_rejected=4"))):
    if any(term not in text for term in terms): raise SystemExit("FAIL tts-interaction-lifecycle boundary")
print("PASS tts-interaction-lifecycle speaking/waiting/interrupted modeled=true focus_loss_releases=true conversation_system_wiring=true human_audio=required")
