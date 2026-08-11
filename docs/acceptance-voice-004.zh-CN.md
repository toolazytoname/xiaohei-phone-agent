# VOICE-004 Android 音频半双工验收

日期：2026-08-11

设备：OnePlus 8T（`KB2000`），Android 14

小黑：`0.2.0-alpha.3`（`versionCode=4`）私有离线 ASR 调试构建；安装/base APK SHA-256 `304aa07c08d5e127116c498e4547477425b4ddcec93b037c4b0f05b937ba7a50`

## 已实现边界

- 进程级协调器每次只签发一个绑定对象身份的音频 lease；输入和输出不能共存，重复获取以及迟到/外来释放均被拒绝。
- 真实系统 TTS 适配器从接受 `speak()` 起持有输出 lease，直到最终完成、错误、停止、中断或销毁。
- App 自有本地 ASR 服务和系统 ASR 命令会话在完整录音期间持有输入 lease；可选 CPU 唤醒录音器也使用同一输入边界。
- OnePlus DSP profile 不进入 Android 录音器所有权，因为已验证模式为 `captureRequested=false`，其低功耗硬件路径不持有 App `AudioRecord`。

## 真机矩阵

| 状态 | 输入证据 | 输出证据 | 终态证据 |
|---|---|---|---|
| 离线 TTS 播报 | Record monitor 没有 Active Client | Conversation 显示 `TTS: SPEAKING`；AudioFlinger 有一条由离线引擎进程持有的 24 kHz Active TTS track | 点击可见停止播报后为 `TTS: INTERRUPTED`；Active TTS track 与 Active Record Client 均变为 0 |
| 离线 ASR 监听 | `RecordActivityMonitor` 显示 `active? true`、包 `io.github.toolazytoname.xiaohei`、来源 `VOICE_RECOGNITION`、单声道 16 kHz PCM16、未静音；AudioFlinger 有一条 Active Input track | 离线引擎 Active TTS track 数为 0 | 全局停止记录 `session_stopped microphone_released=true`；Record monitor 变空，Active TTS track 继续为 0 |

ASR 构建输入是固定的上游 sherpa-onnx 1.13.4 arm64 中文 14M APK，SHA-256 为 `7d5680a287e73c6095105ef79d0e38c070a36c78b961a7f5c2b353fc166f922d`。它只作为经过校验的私有构建输入使用，不进入仓库，也未被批准为公开 Release 资产。

## 由失败推动的修正

以下两条无效路径没有被计为通过：

1. Android 系统识别器在录音前拒绝会话，不能提供输入证据。因此通过可见配置改用固定的小黑离线 ASR，而没有重复相同失败。
2. 修复前，全局停止可能先发生，但延迟启动的本地 ASR worker 仍在约 456 ms 后到达 `capture_started`。现在取消与 `AudioRecord.startRecording()` 共用同一把锁：启动前取消会产生 `capture_start_cancelled before_audio_start=true`，且 record monitor 保持为空；启动后取消会调用 `AudioRecord.stop()`，worker 随后释放 track 和 lease。

## 确定性门禁与边界

纯 lease 矩阵通过 2 次获取、4 次冲突拒绝、2 次迟到释放拒绝和零重叠。完整 Android 单元测试、静态适配器接线门禁、私有签名 APK 构建、安装/构建哈希一致以及上述真机矩阵均通过。

本次证明小黑已接线 Android 音频路径的进程内半双工所有权与停止后资源归零；不证明对无关 App 的跨进程排他、真人听感、口头 barge-in、小于等于 300 ms 的可听中断、蓝牙/耳机路由、长期功耗或公开模型再分发权。这些仍属于独立的 `VOICE-007/010/011/012`、`CHAT-005/012`、Release 和物理门禁。

仓库未提交 APK、模型、原始日志、截图、录音、凭据、转写正文或设备隐私内容。

[English](acceptance-voice-004.md)
