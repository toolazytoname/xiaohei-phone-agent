# FVC-060: DSP-to-Conversation code-gate acceptance

[中文](acceptance-fvc-060.zh-CN.md) · [Delivery plan](free-voice-chat-delivery-plan.md) · [Executor runbook](free-voice-chat-executor-runbook.md)

Date: 2026-08-11. This evidence verifies source-level entry, boundary, and Companion re-arm wiring only; it is not a screen-off human L3 pass.

## Verified code contract

- Only exact local start-chat phrases can turn one completed wake command into one Conversation listen turn; questions, commands, and multi-step text cannot use this entry.
- The entry precedes general routing, launches one non-exported Conversation Intent, and returns the command broker to `ARMED` without retaining command recording.
- This path never starts `CpuWakewordService`. CPU “Xiaohei Xiaohei” remains visible, opt-in, default-off, high-power, and explicitly non-DSP.
- The OnePlus Companion callback sends a package-bound wake event and independently re-arms after a bounded delay; its source has no `AudioRecord` or Android command-recording path.

## Reproducible commands

```bash
python3 scripts/verify-dsp-conversation-entry.py
bash apps/android/xiaohei-android/test.sh
bash scripts/verify.sh
```

Passing status is exactly `FVC-060A = VERIFY`. `FVC-060B` still needs a valid unplugged screen-off human sample: OEM word → start chat → open question → one reply/TTS → DSP re-arm, CPU KWS OFF, and zero recorder at the end.
