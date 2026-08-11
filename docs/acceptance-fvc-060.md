# FVC-060: DSP-to-Conversation code-gate acceptance

[中文](acceptance-fvc-060.zh-CN.md) · [Delivery plan](free-voice-chat-delivery-plan.md) · [Executor runbook](free-voice-chat-executor-runbook.md)

Date: 2026-08-11. This evidence verifies source-level entry, boundary, and Companion re-arm wiring only; it is not a screen-off human L3 pass.

## Verified code contract

- Only exact local start-chat phrases can turn one completed wake command into one Conversation listen turn; questions, commands, and multi-step text cannot use this entry.
- The entry precedes general routing, launches one non-exported Conversation Intent, and returns the command broker to `ARMED` without retaining command recording.
- This path never starts `CpuWakewordService`. CPU “Xiaohei Xiaohei” remains visible, opt-in, default-off, high-power, and explicitly non-DSP.
- The OnePlus Companion callback sends a package-bound wake event and independently re-arms after a bounded delay; its source has no `AudioRecord` or Android command-recording path.

## Preserved limited device evidence

- The private model-bearing `0.2.0-alpha.4-private (5)` build previously completed one token-free foreground debug route: `开始聊天` opened the non-exported Conversation page and showed listening. No second utterance was provided, so no model call occurred.
- After returning home, DSP was `ACTIVE(handle=5)`, CPU wake was `OFF`, and AudioFlinger reported `No active record clients`. This proves only that the foreground route left no persistent CPU KWS or recorder residue.

## Reproducible commands

```bash
python3 scripts/verify-dsp-conversation-entry.py
bash apps/android/xiaohei-android/test.sh
bash scripts/verify.sh
```

Passing status is exactly `FVC-060A = VERIFY`; the foreground debug evidence above is not a screen-off L3 pass. `FVC-060B` still needs a valid unplugged screen-off human sample: OEM word → start chat → open question → one reply/TTS → DSP re-arm, CPU KWS OFF, and zero recorder at the end.
