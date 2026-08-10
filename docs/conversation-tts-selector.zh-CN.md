# 独立 Conversation TTS 选择器

[English](conversation-tts-selector.md) · [执行账本](execution-backlog.zh-CN.md) · [状态](../STATUS.md)

状态日期：2026-08-10。`CHAT-010` 交付 Conversation 朗读的独立输出渠道选择器与安全配置边界；它不宣称中转音频播放或真人听感已经通过。

## 三种明确状态

| 选择 | 持久化含义 | 选择时的效果 |
|---|---|---|
| 关闭 | 不选择朗读适配器 | 不启动引擎、网络、模型或动作服务 |
| 系统 TTS | 为未来 Conversation 朗读选择 Android 已注册的系统 TTS | 只切配置，不初始化、不朗读 |
| 中转 TTS | 选择单独配置的 HTTPS/本机回环中转、voice ID 和 Keystore Token | 只切配置，不访问中转 |

TTS provider、中转端点、voice ID 与中转 Token 槽都独立于 Conversation 模型配置和 Phone Agent 配置。切换 TTS 不得改变另外两条渠道的开关、端点、模型名或 Token 槽。

## 安全与迁移

- 中转端点只接受 HTTPS；HTTP 仅允许 `localhost`、`127.0.0.1` 或 `::1`。
- TTS Relay Token 使用独立 Android Keystore alias，不复用 Conversation 或 Phone Agent 凭据。
- 非敏感备份 v3 可携带 provider、端点和 voice ID，但永不携带 Token；恢复兼容 v2，并强制关闭 Conversation、TTS、Phone Agent，同时清除三类 Token。
- 选择或保存 TTS 状态不会初始化 Android TTS、请求中转、调用模型或启动动作服务。

## 已验证范围

纯 Java 回归覆盖三种 provider、端点边界，以及 Conversation/Phone Agent 指纹保持不变。静态门禁检查配置键所有权、独立 Keystore 槽、备份不含 Token、稳定 UI 标识和选择时零副作用；APK 通过构建与 v2/v3 签名验证。

在全新 Android 14 AOSP 模拟器上，从正常 onboarding 和主页入口配置了非空 Conversation、Phone Agent 与 Relay TTS。先保存 Relay，再只把 TTS 切成 System 后，六个模型渠道字段逐字不变，只有 `tts_provider` 改变；服务与日志检查确认零 TTS 初始化、零小黑语音/服务启动、零 Fatal/ANR。没有使用截图、真实模型、真人语音、网络中转或实体 OnePlus；测试 APK、数据和 UI XML 随后均已清理。

系统 TTS 真正朗读依赖设备已注册引擎；Relay 的传输和播放不属于本选择器任务。真人可懂度、自然打断、麦克风接入和完整“语音 → 模型 → TTS”一轮仍属于 `VOICE-001`、`CHAT-005` 和 `CHAT-012`。
