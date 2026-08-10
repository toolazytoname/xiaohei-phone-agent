# TTS 交互生命周期

`VOICE-003` 在纯状态机中区分系统 TTS 的正常完成与中断：正常路径为 `READY → SPEAKING → WAITING_FOLLOWUP`，用户或系统中断为 `SPEAKING → INTERRUPTED → WAITING_FOLLOWUP`。两条路径都不会重启音频或麦克风。

Android 适配器会报告状态并在中断时停止引擎。对话页仅在独立 TTS 设置为 `system` 时接入它，并提供单独的停止播报按钮。这是运行时接线和边界测试证据，不是可听语音、音频焦点或真人打断的验收。
