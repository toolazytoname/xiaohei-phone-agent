# FVC-040：Conversation 单轮语音代码门禁验收

[English](acceptance-fvc-040.md) · [执行计划](free-voice-chat-delivery-plan.zh-CN.md) · [运行手册](free-voice-chat-executor-runbook.zh-CN.md)

日期：2026-08-11。范围仅为不访问手机、不访问真实中转的代码/JVM/静态门禁；它不证明真人中文识别、模型回复、可听 TTS 或真机资源归零。

## 已验证的代码合同

- “说话”先打断输出，再创建 `CONVERSATION` ASR 会话；在已有请求时拒绝开启。
- partial 仅更新界面；final 经唯一状态转换后才可发送。识别器在回调 final 前停止并释放会话；本地 ASR 在 `finally` 释放 recorder 与 input lease。
- 状态机禁止跳过 listening、thinking 时再听、迟到 partial 和失败后的自动重启；TTS 完成只进入 `WAITING_FOLLOWUP`，不会自动开麦。
- `onStop`、`onDestroy` 和全局停止都会停止当前语音会话。

## 可复跑命令

```bash
python3 scripts/verify-conversation-voice-turn.py
bash apps/android/xiaohei-android/test.sh
bash scripts/verify.sh
```

通过后的准确状态：`FVC-040A = VERIFY`。仍需 `FVC-040B` 在 OnePlus 上完成一次非隐私 spoken final → 精确一次模型回复 → 离线 TTS → `WAITING_FOLLOWUP`，以及一次取消零新增调用，才可声称 L2 的一轮真机闭环。
