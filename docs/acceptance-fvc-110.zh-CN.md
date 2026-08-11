# FVC-110：自动与静态验收（部分）

日期：2026-08-11 · 范围：确定性代码/构建证据，以及一次独立 AOSP source-only mock 生命周期。

- Android 单元测试覆盖显式 ASR profile/provider、合法语音轮次转换、重复/迟到转换拒绝、Conversation 预算、控制短语、隐私拒绝、音频 lease、焦点中断和失败恢复边界。
- 仓库验证检查 Conversation 零动作边界、公开文件无凭据/私有路径、release 无转写日志边界、TTS 生命周期和独立渠道配置。
- 私有含模型构建可编译并以匹配签名安装；无模型源码构建保持独立，绝不覆盖该私有包。
- 当前 revision 的无模型 `0.2.0-alpha.3 (4)` 在独立 Android 14 ARM64 AVD 上全新安装。经正常 onboarding、独立渠道配置和 `adb reverse` 指向本机固定 SSE mock 后，两轮预注册文本显示 `1/6` 与 `2/6`；第二轮 mock 只在角色顺序为 `system,user,assistant,user` 时返回成功，证明界面真实共享同一有界上下文。
- mock 不记录请求正文、不使用真实模型或用户 Token。第二轮后 AudioFlinger 报告 `No active record clients`，日志未见该包 Fatal/ANR；随后执行 `am force-stop`、移除 reverse、卸载包、删除模拟器内临时 UI XML、停止 mock 并关闭 AVD。此项验证的是 force-stop 后资源归零，不等同于真人点击“停止”或语音/听感验收。

这些证据尚不能关闭：OnePlus 真实 L2 两轮/取消/离线失败/全局停止、完整 L3 DSP 聊天路径，以及上述测试后的真实 profile 恢复。
