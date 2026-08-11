# FVC-000 脱敏基线验收

[English](acceptance-fvc-000.md) · [自由语音聊天计划](free-voice-chat-delivery-plan.zh-CN.md) · [当前状态](../STATUS.md)

验收日期：2026-08-11。此记录只证明开始自由语音聊天实施前的只读状态；没有写入 Conversation 配置、没有发送模型请求、没有安装 APK。

| 项目 | 观察结果 |
|---|---|
| 工作树 | `main` 位于 `7715d3f`；仅有本次计划/状态文档修改及原有未跟踪 `docs/articles/`，后者未触碰 |
| 设备 | OnePlus 8T 已通过 USB 连接；不记录设备序列号 |
| 小黑包 | `0.2.0-alpha.3` / versionCode 4 / APK Signature Scheme v4；已安装 base APK SHA-256 `37f2c1f6f5f3c9d87637dda369a7e732e01ffbb4f8fda4359be8d03ca57fbf8d` |
| 模型包出处 | 上述哈希与 `acceptance-voice-010/011` 的私有 local-ASR debug 验收一致；构建使用的上游 ASR/KWS 输入仍不在 Git，也没有在此机器常见下载目录发现可重用副本 |
| ASR/TTS | UI 显示“小黑离线中文 ASR（已内置）”；Android 默认 TTS 为离线 `com.benjaminwan.chinesettstflite`；Conversation TTS profile 为 `system` |
| Conversation | `conversation_enabled=false`；endpoint/model 字段为空；未读取 Conversation Keystore Token |
| CPU KWS | 私有状态为 `OFF` |
| DSP | `io.github.toolazytoname.xiaohei.dsp/.DspControlService` 正以 `ARM` intent 的前台服务运行；shell 无签名权限读取其精确 provider state，因此不把该观察夸大为声学 callback 或功耗证据 |
| 音频 | 基线查询没有活跃小黑录音客户端；未开始语音会话 |

## 结论

`FVC-000` 完成。下一项为 `FVC-010`：只通过独立 Conversation 配置页安全写入用户选择的中转 profile，并验证配置隔离。所有 endpoint、token、模型调用正文、原始 UI XML 和私有构建输入继续禁止进入 Git 或日志。
