#!/usr/bin/env python3
"""Static gate for the bounded DSP-command → Conversation entry boundary."""

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei"
MAIN = (APP / "MainActivity.java").read_text(encoding="utf-8")
ENTRY = (APP / "ConversationEntryPolicy.java").read_text(encoding="utf-8")
BROKER = (APP / "WakewordBroker.java").read_text(encoding="utf-8")
CPU = (APP / "CpuWakewordService.java").read_text(encoding="utf-8")
COMPANION = (ROOT / "apps/android/oneplus8t-dsp-companion/src/io/github/toolazytoname/xiaohei/dsp/SoundTriggerGateway.java").read_text(encoding="utf-8")
ENTRY_TEST = (ROOT / "apps/android/xiaohei-android/tests/io/github/toolazytoname/xiaohei/ConversationEntryPolicyTest.java").read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL DSP Conversation entry: {message}")


def method(source: str, signature: str, end_signature: str) -> str:
    start = source.index(signature)
    end = source.index(end_signature, start + len(signature))
    return source[start:end]


dispatch = method(MAIN, "    private void dispatchTranscript", "    /** Chat is a user-visible draft")
open_voice = method(MAIN, "    private void openConversationVoiceTurn", "    /** Complex text")
on_final = method(MAIN, "    @Override public void onFinalTranscript", "    private void dispatchTranscript")
callback = method(COMPANION, "        @Override public void onRecognition", "        @Override public void onResourcesAvailable")

require('"开始聊天".equals(text)' in ENTRY and '"陪我聊会儿".equals(text)' in ENTRY,
        "entry phrases must be exact local allowlist values")
for rejected in ("question stays draft/chat", "command rejects", "multi-step rejects", "missing rejects"):
    require(rejected in ENTRY_TEST, f"entry policy unit rejection: {rejected}")
require(dispatch.index("ConversationEntryPolicy.startsVoiceConversation(text)")
        < dispatch.index("RouteClarificationPolicy.decide(text)"),
        "exact voice-chat entry must be resolved before command/chat classification")
require("openConversationVoiceTurn();" in dispatch and "EXTRA_START_VOICE_TURN" in open_voice,
        "entry must request exactly one non-exported Conversation listen turn")
require("broker.finishCommand" in open_voice and "短命令录音已释放" in open_voice,
        "command broker returns to ARMED after entry handoff")
require("resumeCpuKwsIfEnabled();" in on_final,
        "existing CPU opt-in lifecycle remains independent of command final")
require("startCpuKws" not in dispatch and "CpuWakewordService" not in open_voice,
        "DSP chat entry must not start persistent CPU keyword spotting")
require("CPU 实验模式" in CPU and "非 DSP" in CPU,
        "CPU wake feature remains visibly distinct from DSP")
require("MAIN.postDelayed(SoundTriggerGateway::rearmAfterCallback, 750)" in callback
        and "setPackage(\"io.github.toolazytoname.xiaohei\")" in callback,
        "device companion emits a package-bound wake event and independently re-arms")
require("ProcessAudioDuplex" not in COMPANION and "AudioRecord" not in COMPANION,
        "DSP companion does not create an Android command-recording path")
require("beginVoiceCommand" in BROKER and "结束后立即释放麦克风" in BROKER,
        "wake broker bounds post-wake command input")

print("PASS DSP Conversation entry phrases=exact handoff=one_turn broker=rearmed "
      "cpu_kws=independent companion_rearm=bounded")
