# Xiaohei Android vertical slice / 小黑 Android 纵切

This is the first runnable product slice: a manual wake event, or a signature-gated event from a device-profile Companion, is routed through the user-selected Android Assistant and into a bounded command session. The currently implemented deterministic action is the low-risk public Android intent **Open Gallery**. It requires no network, accessibility, root, or hidden Android permission. Microphone access begins only after a wake event and is released on a final result, error, or Activity destruction.

It is deliberately not a DSP implementation. A verified device-specific Companion sends only a redacted, signature-protected event after its own arm/start lifecycle succeeds. Android 14 does not allow an arbitrary background component to launch UI, so Xiaohei uses its user-selected `VoiceInteractionService` to create the visible Assistant session. A high-priority notification is the explicit fallback when Xiaohei is not the selected Assistant.

`XiaoheiRecognitionService` currently defines a stable, app-owned ASR boundary but intentionally returns an unavailable error. It is not a hidden dependency on another installed app. Independent Chinese ASR must pass its own privacy, timeout, and real-device acceptance gates before this slice is called voice-complete.

Build locally:

```sh
./build.sh
adb install -r build/xiaohei-debug.apk
```

On device: select Xiaohei as the Android digital assistant, tap **Enable base mode**, then **Simulate “Xiaobu Xiaobu” hit → Open Gallery**. A profile Companion may deliver the same event under the shared signing identity. Only keyword ID, confidence, and capture availability cross that boundary; no wake audio is included.

本目录提供第一条可运行的产品纵切：手动唤醒或设备 profile Companion 的签名保护事件，经用户选定的 Android 默认助手会话进入短命令流程，再用公开 Intent 执行“打开相册”。Android 14 息屏后台拉起已在 OnePlus 8T 上通过；唤醒事件只包含关键词 ID、置信度和 capture 标志，不包含音频。当前自有中文 ASR 仍是明确的未配置状态，不能把这条纵切描述成完整语音闭环。
