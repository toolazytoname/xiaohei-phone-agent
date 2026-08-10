# CHAT-012 Conversation 验收

[English](conversation-acceptance-chat-012.md) · [执行账本](execution-backlog.zh-CN.md) · [状态](../STATUS.md)

状态日期：2026-08-10。本页把精确候选版本的自动证据与尚未完成的真人语音/TTS 门禁分开；自动全绿本身不能把 `CHAT-012` 标成完成。

## 自动矩阵

纯 Java 矩阵不使用真实模型或用户数据，精确执行：

| 分组 | 数量 | 验收内容 |
|---|---:|---|
| 普通问题 | 20 | 四个全新五轮会话逐一接受问题、构造版本化 Prompt Envelope，并在轮数/token/时间限制内完成 |
| 中断 | 5 | 锁屏、后台、切 profile、取消、停止分别终止或取消预期状态；控制本身零模型调用 |
| 超时 | 5 | 下一轮前超时、迟到回复、定时到期、单调时钟倒退、直接会话截止都 fail closed 并清除会话持有的上下文 |
| 隐私拒绝 | 5 | 通知、联系人、实时位置、私人媒体/文件、凭据都在创建会话/模型请求之前本地拒绝 |

隐私策略只接受五类共 15 条精确中英文短语。每次拒绝都显示“本地隐私拒绝｜零模型调用”，结果固定零模型/动作调用，并在合适时指向未来按次、可见、受限的能力入口。概念性隐私问题和组合文字不会被过度拦截，而是继续走仍然没有动作权限的普通 Conversation。

静态门禁确认隐私检查位于 `coordinator.begin` 和 `ConversationClient.ask` 之前、Conversation 路径没有录音 API、矩阵计数不能静默缩减，并且 `STATUS.md` 继续保留真人门禁。

## 设备证据与剩余门禁

精确 debug APK 已在全新 Android 14 AOSP 上通过正常 onboarding、主页和 Conversation 路径。输入 `read my contacts` 后，在任何远端配置/请求之前本地拒绝；页面显示两种本地/零调用标签，前台保持 `ConversationActivity`，日志零 Fatal/ANR，`dumpsys media.audio_flinger` 的 Active Record Client 为 0。随后清理 APK、数据和 UI XML；未使用截图、真实模型、语音、网络 Mock 或实体 OnePlus。这只能证明文字 Conversation 候选没有留下录音器，不能证明麦克风/ASR/TTS 体验。

因此 `CHAT-012` 是 `VERIFY`，不是 `DONE`。只有在真正接通语音闭环的精确候选上，由真人确认中文可懂、自然打断和零录音残留后，它才能离开 `VERIFY`。当前 Relay 播放尚未实现，因此不能诚实执行这道人类门禁；届时应复用本自动矩阵。
