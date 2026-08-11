# REL-005 通用生命周期恢复子验收

[English](acceptance-rel-005.md) · [执行账本](execution-backlog.zh-CN.md) · [证据矩阵](delivery-evidence-matrix.zh-CN.md)

## 范围

本验收严格限定为独立 Android 14 ARM64 AOSP 模拟器上的通用无模型 APK。它验证应用进程生命周期事件后没有小黑进程、Android 服务或 AudioFlinger 残留；它不是弱网、真实中转/模型超时、OnePlus DSP 或实体设备资格。

## 可复现步骤

1. 构建源码型通用 debug APK；不打包 ASR/KWS 模型或凭据。
2. 启动一次性 Android 14 ARM64 AOSP 模拟器。
3. 运行 `scripts/test-rel005-emulator-lifecycle.sh emulator-5554 path/to/xiaohei-debug.apk`。

脚本拒绝除 `emulator-*` 外的所有序列号：安装精确 APK，启动后强制结束，再冷启动/强制结束，重启模拟器并检查 `sys.boot_completed`。每个终态后都检查小黑 PID、`ServiceRecord` 和 AudioFlinger 包名残留。成功时卸载；失败退出时也由 trap 卸载。

## 必须出现的结果

唯一的通过结果是：

```text
PASS rel005-emulator-lifecycle force_stop=clean cold_start=clean reboot=clean uninstall=clean network_model=not_exercised
```

当前交付记录尚未产生该结果，所以 `REL-005` 仍为 `BACKLOG`。产生后它也只会把通用生命周期子项推进到 `VERIFY`。真实中转/模型请求、刻意降级的网络、设备进程杀死行为、OnePlus DSP 重 arm 和用户可见恢复仍需要独立的非合成证据。相同网络/模型失败后不得自动重试。
