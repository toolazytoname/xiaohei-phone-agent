# VOICE-005 音频中断证据

日期：2026-08-11

设备：OnePlus 8T（`KB2000`），Android 14

## 已实现策略

`AudioInterruptionPolicy` 将来电、闹钟、媒体和 Activity 中断信号映射为同一个 fail-safe 结果：停止输入和输出、释放进程内音频所有权，且绝不自动恢复。确定性矩阵覆盖四种来源各自的输入活跃与输出活跃情形。

## OnePlus Activity 中断运行

第一轮明确不计入证据：异步本地 ASR worker 尚未打开录音器就退到后台，记录为 `capture_start_cancelled before_audio_start=true`。

随后以已改变的条件执行一次调试包“两分钟中断”入口：只在真实开始录音后离开 Activity。

1. `12:12:04.059`：`session_started audio_focus=exclusive local_asr=true`。
2. `12:12:05.162`：本地 ASR 记录 `capture_started source=6 maximum_ms=8000`，UI 进入 speech-ready。
3. 随后回到桌面；`12:12:06.114` 记录 `session_stopped microphone_released=true`。
4. 停止后 `dumpsys media.audio_flinger` 返回 `No active record clients`，且没有新的 session start 日志。独立 DSP profile 仍为 `ACTIVE(handle=4)`。

这证明已实现的 Activity/后台分支会停止真实的应用自有本地 ASR 录音，且不会自动恢复。它不能替代真实来电、闹钟或媒体焦点丢失，也不证明无关 App 音频所有权、真人听感、路由行为或长期功耗。因此 `VOICE-005` 保持 `VERIFY`。

不提交录音、原始日志、APK、模型、凭据、截图或私有设备内容。

[English](acceptance-voice-005.md)
