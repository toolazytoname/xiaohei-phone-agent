# M3 通用 Android Alpha 验收记录

日期：2026-08-09  
当前设备：OnePlus 8T / Android 14；通用模式验收期间主动禁用了设备增强 profile

## 已有真机证据

- 源码基础 APK 为 41,625 字节，SHA-256：`a7422faebf8c26cfaa2c8872ef928e69042705cf15c075c620f86db2646358dd`。
- 禁用 `io.github.toolazytoname.xiaohei.dsp` 后，主 APK 明确显示“此设备未安装增强 profile”，两个 DSP 控件均为禁用状态，没有失败重试。
- 无模型的源码基础包自动选择 Android 系统识别器。本 ROM 的第三方系统识别器返回麦克风权限错误；小黑只报告一次并回到 `ARMED`，未进入循环。
- 在仍禁用 OnePlus profile 时安装可移植离线 ASR 构建，界面显示本地模型已内置，并完成“可见语音→确定性动作”链。声学夹具把目标“打开相册”识别成“打开相机”，因此本次只证明通用执行路径，不作为该句准确率证据。
- 重新启用 OnePlus profile 后，无需重装主产品即恢复 `DSP DETACHED` 和离线 ASR 状态。

## 架构结果

主 APK 不依赖 root 或 OnePlus 运行时，启动时探测可选 Companion。ASR 配置已真实生效：带模型构建默认自有离线 ASR，无模型构建默认系统识别；切换 ASR 不会改变 Phone Agent 或 DSP 服务状态。

## 尚未满足的 M3 出口门禁

同一手机禁用 profile 不能替代独立非 OnePlus 设备证据。仍需第二台实体设备或 Android 虚拟设备做全新安装、拒绝权限/离线、核心动作回归和残留检查。CPU 唤醒词仍是待功耗测量后才可开放的实验选项。
