# FVC-050：半双工多轮语音代码门禁验收

[English](acceptance-fvc-050.md) · [执行计划](free-voice-chat-delivery-plan.zh-CN.md) · [运行手册](free-voice-chat-executor-runbook.zh-CN.md)

日期：2026-08-11。范围是静态/JVM 的半双工与有界会话门禁；不包含真实语音、远端调用或手机听感。

## 已验证的代码合同

- 文字和语音 final 都使用同一个 `ConversationSessionCoordinator`：6 轮、2048 token、5 分钟，不能出现隐藏的两套上下文。
- 只有 `WAITING_FOLLOWUP` 显示“继续说”；请求在途或已有录音会禁用它。TTS 完成不会调用 `startVoiceTurn()`。
- 停止、重说、清空、继续、结束走精确的本地控制策略，显式 `modelCalls = 0`；控制的单元测试保留 23 条短语覆盖。
- 切换 Conversation profile 时先清除旧上下文，必须重新发送；半双工、超时、轮数上限、指代上下文均有 JVM 用例。

## 可复跑命令

```bash
python3 scripts/verify-conversation-followup.py
bash apps/android/xiaohei-android/test.sh
bash scripts/verify.sh
```

通过后的准确状态：`FVC-050A = VERIFY`。`FVC-050B` 仍须在 OnePlus 做两轮预注册指代问题、精确两次调用、停止播报/结束/切模型资源清理，才能把它作为 L2 多轮体验声明。
