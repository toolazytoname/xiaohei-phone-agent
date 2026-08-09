# M3 通用 Android Alpha 验收记录

日期：2026-08-09  
主设备：OnePlus 8T / Android 14；通用模式验收期间主动禁用了设备增强 profile
独立验收设备：干净 Android 14 AOSP ARM64 虚拟设备（`emu64a` / `Android SDK built for arm64`），没有 OnePlus profile 或 root

## 已有真机证据

- 源码基础 APK 为 41,625 字节，SHA-256：`a7422faebf8c26cfaa2c8872ef928e69042705cf15c075c620f86db2646358dd`。
- 禁用 `io.github.toolazytoname.xiaohei.dsp` 后，主 APK 明确显示“此设备未安装增强 profile”，两个 DSP 控件均为禁用状态，没有失败重试。
- 无模型的源码基础包自动选择 Android 系统识别器。本 ROM 的第三方系统识别器返回麦克风权限错误；小黑只报告一次并回到 `ARMED`，未进入循环。
- 在仍禁用 OnePlus profile 时安装可移植离线 ASR 构建，界面显示本地模型已内置，并完成“可见语音→确定性动作”链。声学夹具把目标“打开相册”识别成“打开相机”，因此本次只证明通用执行路径，不作为该句准确率证据。
- 重新启用 OnePlus profile 后，无需重装主产品即恢复 `DSP DETACHED` 和离线 ASR 状态。
- 独立 AOSP 设备全新安装了源码基础 debug APK（`0.2.0-alpha.2`，41,625 字节，SHA-256 `c2702ca914efcc72f9c3469e12246be7ab2f7d13c8120034a6769e0a9d8ad636`）。onboarding 页不请求权限，并如实显示 DSP profile 不可用；运行前主包和 DSP Companion 均不存在。
- 在该 AOSP 设备上，麦克风、相机和通知权限均处于拒绝状态。点击通用按一下说话会显示 Android 麦克风授权；选择“拒绝”后产品回到 `ERROR：未授予麦克风权限；已停止命令会话`，活跃录音客户端为 0。
- WLAN 关闭时，debug 专用固定文本注入把“打开相册”路由为 `OPEN_GALLERY`（`ok=true`），系统 Gallery 成为 resumed activity。它有意只证明无网络下的可移植确定性路由和公开 Intent 执行，**不**把它说成模拟器声学/ASR 准确率。
- force-stop 并卸载后，主包和 DSP Companion 都不在包列表中，Accessibility 仍未设置，音频服务的活跃录音客户端为 0。

## 架构结果

主 APK 不依赖 root 或 OnePlus 运行时，启动时探测可选 Companion。ASR 配置已真实生效：带模型构建默认自有离线 ASR，无模型构建默认系统识别；切换 ASR 不会改变 Phone Agent 或 DSP 服务状态。

## M3 出口门禁

独立 Android 虚拟设备上的全新安装、拒绝权限、离线确定性动作和残留检查现已完成。CPU 唤醒词仍是待功耗测量后才可开放的实验选项；它是 M7 条件，不扩大 M3 通用基础模式的声明。
