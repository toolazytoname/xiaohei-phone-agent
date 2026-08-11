# VOICE-002 离线系统 TTS 适配器验收

日期：2026-08-11

设备：OnePlus 8T（`KB2000`），Android 14

小黑：`0.2.0-alpha.3`（`versionCode=4`）

## 真机结果

- Android 默认引擎仍是已通过资格验证的离线 `com.benjaminwan.chinesettstflite`，没有使用微软在线 TTS。
- 小黑可见 Conversation 路径初始化了真实 Android `TextToSpeech` 适配器，并显示 `TTS: READY`。
- 在 Conversation 网络渠道关闭时发送确定性本地 FAQ，回复进入 `TTS: SPEAKING`；全程零模型调用、零动作调用。
- 点击可见的“停止播报（保留聊天）”后，状态变为 `TTS: INTERRUPTED`，按钮随即禁用；Android 同时记录音轨在交付 6,976 帧后停止。
- 中断后 AudioFlinger 没有 Active Record Client；`dumpsys power` 中小黑和离线 TTS 包的唤醒锁引用均为 0。
- 测试输入结束后已恢复设备的 Fcitx5 中文输入法。

## 竞态修复与确定性门禁

第一次真机观察发现停止/完成竞态：`TextToSpeech.stop()` 可能在生命周期进入终态前送达完成回调。适配器现在先原子切换生命周期、使 utterance ID 失效，再请求 Android 停止或关闭引擎。迟到的完成或错误回调不能把已中断、已停止或已销毁的播报改写为完成/失败。生命周期测试现覆盖 24 次合法转换并拒绝 6 次迟到回调；完整 Android 单元测试和签名 APK 构建均通过。

## 功耗与结论边界

离线 TTS 是按需输出路径：只有唤醒/会话请求播报后才短时使用 CPU，完成或中断后立即释放输出所有权。它既不负责监听唤醒词，也不会在空闲时阻止 CPU 休眠。低功耗常驻唤醒仍由 OnePlus DSP 承担；实验性 CPU 唤醒保持关闭。

本次证明小黑适配器的真实离线初始化、播报请求、显式中断、迟到回调拒绝和停止后资源状态；不宣称可听停止时延小于等于 300 ms、真人中文可懂度/自然度、完整“语音→模型→语音”轮次、长期续航或当前 DSP 已处于 ARMED。这些仍属于独立的 `VOICE-004/010/011`、`CHAT-005/012` 和物理功耗门禁。

仓库未提交 APK、模型、原始日志、截图、录音、凭据或设备隐私正文。

[English](acceptance-voice-002.md)
