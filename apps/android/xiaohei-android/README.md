# Xiaohei Android vertical slice / 小黑 Android 纵切

This is the first runnable product slice: a manual wake event, or a signature-gated event from a device-profile Companion, is routed through the user-selected Android Assistant and into a bounded command session. The currently implemented deterministic action is the low-risk public Android intent **Open Gallery**. It requires no network, accessibility, root, or hidden Android permission. Microphone access begins only after a wake event and is released on a final result, error, or Activity destruction.

It is deliberately not a DSP implementation. A verified device-specific Companion sends only a redacted, signature-protected event after its own arm/start lifecycle succeeds. Android 14 does not allow an arbitrary background component to launch UI, so Xiaohei uses its user-selected `VoiceInteractionService` to create the visible Assistant session. A high-priority notification is the explicit fallback when Xiaohei is not the selected Assistant.

`XiaoheiRecognitionService` is a stable, app-owned ASR boundary. `VoiceCommandSession` addresses that component explicitly instead of following Android's global speech-recognizer setting. A source-only build returns an honest unavailable error. A local product build may bundle the official sherpa-onnx arm64 Chinese 14M Zipformer APK as a pinned build input; Xiaohei then performs an at-most-eight-second offline transcription and releases `AudioRecord` on result, error, or cancellation. It is not a runtime dependency on another installed app.

Build locally:

```sh
./build.sh
adb install -r build/xiaohei-debug.apk
```

For the locally verified offline-ASR build, first download the official sherpa-onnx 1.13.4 arm64 Chinese 14M APK, verify SHA-256 `7d5680a287e73c6095105ef79d0e38c070a36c78b961a7f5c2b353fc166f922d`, review the upstream model license, and build without committing the binary:

```sh
XIAOHEI_LOCAL_ASR_APK=/absolute/path/to/sherpa-onnx-1.13.4-arm64-v8a-asr-zh-small_zipformer_14M_2023_02_23.apk ./build.sh
```

The resulting local APK is about 53 MB. The upstream APK is used only as a reproducible build bundle for its model, arm64 runtime, and Java API dex; it does not need to remain installed. Public redistribution is a separate release gate because sherpa-onnx code is Apache-2.0 while each model has its own license.

An opt-in experimental CPU wake-word build can additionally use the official sherpa-onnx 1.13.4 Chinese KWS APK (SHA-256 `1ee827227c1369b55e0aa5e35de93981ddcaa153238bfa21063260413278f07f`). The KWS APK supplies the single shared runtime; the ASR APK supplies only its ASR assets. The build removes the duplicate epoch-99 KWS checkpoint and replaces the demo keyword list with the acoustically verified `小黑小黑` entry:

```sh
XIAOHEI_LOCAL_ASR_APK=/absolute/path/to/asr.apk \
XIAOHEI_LOCAL_KWS_APK=/absolute/path/to/kws.apk \
./build.sh
```

The combined local APK is about 64 MB. **CPU wake word** and **DSP wake word** have independent controls and states. CPU mode holds the microphone in a foreground service and is intentionally off by default; it is a portable functional fallback, not a low-power DSP claim.

On device: select Xiaohei as the Android digital assistant, tap **Enable base mode**, then **Simulate “Xiaobu Xiaobu” hit → Open Gallery**. A profile Companion may deliver the same event under the shared signing identity. Only keyword ID, confidence, and capture availability cross that boundary; no wake audio is included.

本目录提供第一条可运行的产品纵切：手动唤醒或设备 profile Companion 的签名保护事件，经用户选定的 Android 默认助手会话进入短命令流程，再用公开 Intent 执行“打开相册”。Android 14 息屏后台拉起已在 OnePlus 8T 上通过；唤醒事件只包含关键词 ID、置信度和 capture 标志，不包含音频。

源码基础包未附带模型时会明确显示 ASR 未配置。用上面的固定版本和哈希加入 sherpa-onnx 中文 14M 模型后，2026-08-09 已在 OnePlus 8T 真机完成“中文离线识别‘打开相册’→系统图片选择器→回到 ARMED”，并确认 `No active record clients`。模型与上游二进制不提交到本仓库；公开再分发前必须单独完成模型许可证门禁。

如同时提供上面的 KWS 构建输入，页面会额外显示独立的“小黑小黑”CPU 实验唤醒开关。它默认关闭、持续占用麦克风且功耗明显高于 DSP，只用于通用 Android 功能验证；DSP 开关与状态不受它联动。2026-08-09 在 OnePlus 8T 上已两次声学命中“小黑小黑”，并验证命中后释放 KWS 录音、进入 Assistant/离线 ASR、结束后恢复监听，以及停止按钮释放录音。
