# FVC-070：中断与音频资源代码门禁验收

[English](acceptance-fvc-070.md) · [执行计划](free-voice-chat-delivery-plan.zh-CN.md) · [运行手册](free-voice-chat-executor-runbook.zh-CN.md)

日期：2026-08-11。范围仅为源码/JVM/静态门禁，不能替代真实来电、闹钟、媒体或蓝牙路由测试。

私有 OnePlus 候选已在 2026-08-11 升级到 `0.2.0-alpha.5-private (6)`，Android 增量安装成功。该记录仅证明匹配包可升级；未在本次安装中发起模型/语音调用，也不证明中断体验。

## 已验证的代码合同

- TTS 在用户停止、失败、销毁和系统 audio-focus loss 时取消队列、释放 output lease 并归还 audio focus；不会自动恢复播报或录音。
- `VoiceCommandSession` 在独占焦点丢失时停止 recognizer、归还焦点和 input lease，并只报告安全错误。
- Conversation 收到息屏时立即停止活跃 ASR，再清除内存会话和在途请求；`onStop`、`onDestroy`、全局停止也有同一停止路径。
- 新旧音频 route 切换会显式 Stop 当前 turn；不把旧录音或旧播报续接到新设备。

## 可复跑命令

```bash
python3 scripts/verify-tts-interaction-lifecycle.py
python3 scripts/verify-conversation-voice-turn.py
bash apps/android/xiaohei-android/test.sh
bash scripts/verify.sh
```

通过后的准确状态：`FVC-070A = VERIFY`。真实电话必须分别在 LISTENING、THINKING、SPEAKING 中验证；闹钟/媒体焦点和每种可支持 route 也仍为 HUMAN 门禁，且不得因为自动绿灯声明完成。
