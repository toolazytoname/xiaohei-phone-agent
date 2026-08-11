# VOICE-011 TTS 中断边界验收

日期：2026-08-11

设备：OnePlus 8T（`KB2000`），Android 14

小黑：私有本地 ASR 调试构建，`0.2.0-alpha.3`（`versionCode=4`）；已安装 base APK SHA-256：`37f2c1f6f5f3c9d87637dda369a7e732e01ffbb4f8fda4359be8d03ca57fbf8d`

## 已证明的自动化/设备边界

- Conversation 提供独立的 `conversation-stop-speech` 控制。它会中断选中的系统 TTS 适配器，但保留内存聊天；“重说”必须是用户显式本地操作，绝不自动续播。
- 停止、清空、离页、全局停止、超时和销毁都会先使当前 utterance/队列失效，再调用 Android TTS 引擎，因此迟到完成回调不能重新播报队列内容。
- OnePlus 的代际 2 队列中，第 2 句于 `11:54:33.221` 开始；用户于 `11:54:33.799` 点击停止播报；适配器记录 `queue_cancelled reason=interrupt … dropped=3`，第 2 句的 24 kHz AudioFlinger 音轨 405 于 `11:54:33.955` 被移除，且未提交第 3/4 句。因此，引擎可见音轨从队列回调到释放为 156 ms，从用户控制事件到释放为 227 ms。
- `VOICE-004` 另行证明应用自有本地 ASR 与 TTS 不能同时持有进程内音频输入/输出 lease。这是软件半双工边界，不代表任意真实声学回声必然不会发生。

## 必须如实保留的真人门禁

AudioFlinger 的时间不能证明人耳何时听不到声音。仍需真人确认中文可懂度/自然度、可听中断小于等于 300 ms，以及真实唤醒/监听交互不会形成不可接受的扬声器到麦克风回声环。在该受控真人/设备运行完成前，语音抢话继续不在范围内；`VOICE-011` 状态为 `HUMAN`，不能写成产品完整交付。

不提交录音、截图、原始日志、对话正文、凭据、APK 或模型。

[English](acceptance-voice-011.md)
