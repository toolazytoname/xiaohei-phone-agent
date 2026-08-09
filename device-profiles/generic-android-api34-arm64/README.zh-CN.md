# 通用 Android 14 ARM64 profile

[English](README.md)

状态：**已在独立 AOSP 虚拟设备验证基础产品和 CPU KWS 生命周期；不声称支持 DSP**。

这是小黑的可迁移参考 profile。它刻意不依赖 OnePlus、Qualcomm、root、Magisk、OEM 模型或私有库，用于证明设备专属增强不会反向成为可下载基础产品的前置条件。

## 已验证环境

| 字段 | 值 |
|---|---|
| 运行环境 | Android Emulator，干净 AOSP API 34 ARM64 AVD `xiaohei-m3-api34` |
| 型号 | `Android SDK built for arm64` |
| Android | 14 |
| ABI | `arm64-v8a` |
| Build fingerprint | `Android/sdk_phone64_arm64/emu64a:14/UE1A.230829.036.A1/11228894:userdebug/test-keys` |
| 小黑候选版本 | `0.2.0-alpha.2`、`versionCode=3`、内部 debug 签名 |
| 组合候选包 SHA-256 | `70e23a097c2c82ba06d7c989a274b20be3f1da5f2874724e6c6f1647d99d1008` |

候选包由固定版本的外部 ASR/KWS 输入在本机构建。上游二进制和模型权重没有进入仓库，也尚未获准公开再分发。

## 已验证行为

- 全新安装后如实显示“DSP：此设备未安装增强 profile”。
- 手动入口、快捷设置、系统助手、离线 ASR、确定性动作、可选通知权限、确认式草稿和可见 Phone Agent 均走通用产品路径；独立 AOSP 的详细证据由 M3–M5 验收记录索引。
- CPU“小黑小黑”默认关闭。用户在可见页面主动启动后，麦克风前台服务进入 `LISTENING`，并展示只有一个停止动作的私密常驻通知。
- 可见停止动作把 CPU KWS 变为 `OFF`、销毁 `CpuWakewordService`，活跃录音客户端为 0；DSP 状态没有改变。
- 卸载后没有小黑包、App 进程、活跃服务、通知读取授权、无障碍授权或录音客户端。`dumpsys` 中系统保留的死亡 Binder 历史不是活跃小黑服务，会随框架生命周期正常清理。

该 AVD 启动时没有接入宿主音频。因此本轮证明的是可安装性、如实能力显示、生命周期隔离、停止和回滚；**不证明**声学命中、物理真机功耗、距离、噪声、温度表现或第二款实体硬件。

## 构建与复测

按 [`apps/android/xiaohei-android/README.md`](../../apps/android/xiaohei-android/README.md) 的固定输入说明构建。纯源码包仍可通过主动入口完整使用，并如实报告离线模型不可用；本地模型包在任何公开二进制分发前必须单独完成上游模型权利审核。

关键词回归可用 `scripts/generate-synthetic-kws-corpus.py` 生成一次性 macOS 合成音频，再用 `scripts/evaluate-kws-wavs.py` 评测固定模型，不把音频提交进仓库。合成音色只是一项可重复的工程探针，不能代替真人和物理距离测试。

## 对外承诺边界

可以说：“基础产品和可选前台 CPU 唤醒服务的生命周期已在干净 Android 14 ARM64 AOSP profile 验证。”

不能说：“这个 AVD 证明所有 Android 硬件都支持小黑”，也不能说：“‘小黑小黑’是通用 DSP 唤醒词”。
