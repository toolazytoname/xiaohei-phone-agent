# TTS 交互生命周期

`VOICE-003` 在纯状态机中区分系统 TTS 的正常完成与中断：正常路径为 `READY → SPEAKING → WAITING_FOLLOWUP`，用户或系统中断为 `SPEAKING → INTERRUPTED → WAITING_FOLLOWUP`。两条路径都不会重启音频或麦克风。

Android 适配器会报告状态并在中断时停止引擎，但当前产品页面还没有把该生命周期接入真实 Conversation 播放。这是状态机测试证据，不是可听语音、音频焦点或真人打断的验收。
