# FVC-060：DSP 进入对话的代码门禁验收

[English](acceptance-fvc-060.md) · [执行计划](free-voice-chat-delivery-plan.zh-CN.md) · [运行手册](free-voice-chat-executor-runbook.zh-CN.md)

日期：2026-08-11。此证据只验证源码中的入口、边界和 Companion re-arm 接线；不将其写成息屏真人 L3 通过。

## 已验证的代码合同

- 只有精确的“开始聊天”等本地短语可以从一条已完成的唤醒短命令进入一次 Conversation 听取；问句、命令和多步骤文本均拒绝该入口。
- 入口先于一般聊天/命令分类，启动的是非导出的 `ConversationActivity` 的单轮 Intent；短命令 broker 随即回到 `ARMED`，不保留命令录音。
- 该路径不启动 `CpuWakewordService`。CPU “小黑小黑”仍是可见、默认关闭、明确标注非 DSP 的独立高功耗实验功能。
- OnePlus Companion 回调只向目标包发送唤醒事件，并以有界延迟独立 re-arm；Companion 源码没有 `AudioRecord` 或 Android command-recording 路径。

## 可复跑命令

```bash
python3 scripts/verify-dsp-conversation-entry.py
bash apps/android/xiaohei-android/test.sh
bash scripts/verify.sh
```

通过后的准确状态：`FVC-060A = VERIFY`。`FVC-060B` 仍需要合格的拔线、息屏真人样本：厂商词 → 开始聊天 → 开放问句 → 一次回复/TTS → DSP re-arm，CPU KWS OFF、终态零 Recorder。
