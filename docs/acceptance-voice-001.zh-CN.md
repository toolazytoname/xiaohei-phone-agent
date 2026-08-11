# VOICE-001 离线中文 TTS 验收

日期：2026-08-11

设备：OnePlus 8T（`KB2000`），Android 14

小黑：`0.2.0-alpha.3`（`versionCode=4`），APK SHA-256 `e8a447b1ce459d6ec68d733507c74123b23dea25313c84fb690afe1cc6435167`

## 已通过状态

- 设备所有者明确授权了安装和默认引擎选择。
- `ChineseTtsTflite 0.5.0`（`versionCode=5`）来自 F-Droid 签名仓库索引列出的镜像。已安装 base APK 大小为 70,075,044 字节，SHA-256 为 `bdc8a50c028b4f0eacd2ab2f22cbbefe8ee00262b25e388cf545c54cbacbc76e`，与该签名索引精确一致。
- Android 已注册 `com.benjaminwan.chinesettstflite/.service.TtsService`，`tts_default_synth` 为 `com.benjaminwan.chinesettstflite`。
- 从小黑可见主页点击“检查系统中文 TTS（只读）”，返回 `READY`：引擎 `com.benjaminwan.chinesettstflite`，4 个中文音色全部离线，简体中文可用（`1`），繁体中文不可用（`0`）。探针没有播报、下载或改变设置。
- 使用 FastSpeech2 播放了引擎内置固定中文示例。Android 记录 `AudioTrack ... 81600 frames delivered`；`dumpsys power` 唤醒锁持有者中没有该应用。返回主页后，TTS 服务是 Android 绑定服务，不是前台启动服务。

## 功耗与声明边界

当前引擎完全离线，只在请求播报时短时使用 CPU 推理。它不是常驻唤醒路径：低功耗唤醒仍由 OnePlus DSP 负责。本次不构成续航结论、真人可懂度评分、小黑 Conversation 适配器验收或可听停止时延证据；这些仍属于 `VOICE-002/004/010/011`、`CHAT-005/012` 和物理功耗门禁。

仓库不提交 APK、模型、原始日志、截图、录音、凭据或设备私人内容。

## 回滚

在 Android 中选择其他 TTS 引擎或清除默认值，再卸载 `com.benjaminwan.chinesettstflite`。小黑只读探针会如实显示新状态。

[English](acceptance-voice-001.md)
