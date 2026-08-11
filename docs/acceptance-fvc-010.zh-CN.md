# FVC-010 Conversation 私有配置隔离验收

[English](acceptance-fvc-010.md) · [自由语音聊天计划](free-voice-chat-delivery-plan.zh-CN.md) · [当前状态](../STATUS.md)

验收日期：2026-08-11。已通过小黑的公开“独立模型与语音渠道”页面保存用户选择的 CC Switch profile。URL、Token、模型名和任何 UI XML 均不记录在本文或 Git 中。

| 通道/字段 | 脱敏结果 |
|---|---|
| Conversation | enabled；HTTPS endpoint 与 model 均为非空受限长度；Conversation 专用 Android Keystore 槽已配置 |
| Conversation TTS | `system`；默认离线中文系统引擎未改变 |
| Phone Agent | disabled；endpoint/model 长度均为 0；Token 槽未配置 |
| TTS Relay | endpoint/voice 长度均为 0；Token 槽未配置 |
| ASR | 保持既有离线小黑 ASR 选择 |
| 音频 | 保存配置不启动录音或 TTS；没有活跃小黑录音客户端 |

客户端将 endpoint 作为 OpenAI-compatible base，并只在请求时追加 `/chat/completions`。本配置尚未发送模型请求，因此不构成连通性或回复质量证据。

## 结论

`FVC-010` 完成。下一项 `FVC-020` 只执行预先声明的最小真实文字对话序列，验证一次模型调用、一次引用追问、本地结束命令和取消行为；相同网络失败不会重复烧 token。
