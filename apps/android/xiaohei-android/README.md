# Xiaohei Android vertical slice / 小黑 Android 纵切

This is the first runnable product slice: an explicit manual wake event is routed to the low-risk public Android intent **Open Gallery**. The deterministic action requires no network, accessibility, root, notification, or hidden Android permission. The optional bounded command session requests microphone access only after a wake event; it releases the recognizer on a final result, error, or Activity destruction.

It is deliberately not a DSP implementation. A verified device-specific DSP adapter must call `WakewordBroker.dispatchDspHit()` only after its own arm/start lifecycle succeeds. On the current device, the Android framework exposes no usable general `RecognitionService`; the UI reports that absence rather than treating another app's service as a dependency.

Build locally:

```sh
./build.sh
adb install -r build/xiaohei-debug.apk
```

On device: tap **Enable base mode**, then **Simulate “Xiaobu Xiaobu” hit → Open Gallery**. The event remains redacted and in memory; the app does not record audio.

本目录提供第一条可运行的产品纵切：明确的手动唤醒事件经过安全策略边界后，只用公开 Android Intent 打开相册。它不申请联网、麦克风、无障碍、Root、通知或隐藏 Android 权限。
