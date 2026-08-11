# ROOT-010 独立设备生命周期验收

日期：2026-08-11

独立设备：干净 Android 14 AOSP ARM64 虚拟设备（`emu64a` / `Android SDK built for arm64`），不是 OnePlus 8T，且未安装小黑 DSP Companion 或 root profile。

## 包与签名

从同一 revision 构建两份源码型、不含模型的调试包，仅版本元数据不同：

| 版本号 | 版本名 | SHA-256 |
|---:|---|---|
| 4 | `0.2.0-root010.4` | `c2effca2d74552da49609538dbcf4bffd8cb95e540dbebfe7384c28267132586` |
| 5 | `0.2.0-root010.5` | `72ea64cf13ccaedb61d9401d3b05a2f66d4d970bd4f6be75da5e95973866e701` |

两者均通过 APK Signature Scheme v2 和 v3 验证。

## 设备生命周期

1. 确认主包不存在后，成功安装 code 4。
2. 以 `adb install -r` 安装 code 5；包管理器报告 `versionCode=5`。
3. 尝试普通的 code 5 → code 4 降级；Android 正确以 `INSTALL_FAILED_VERSION_DOWNGRADE` 拒绝。
4. 使用所有者受控的维护降级 `adb install -r -d`；包管理器报告 `versionCode=4`。
5. 运行仓库事务卸载脚本。主包和 Assistant role 被移除；随后 Xiaohei 与 DSP Companion 的 `pm path` 均为空，AudioFlinger 报告无 active record client。

## 范围边界

本项完成独立设备上的安装/升级/回退/卸载前置条件；不安装、启用或执行 root adapter、`su`、shell 命令、profile 事务或 root capability。当前产品仍没有真实 root adapter；未来 root 执行继续受确认、有界 adapter 设计和设备证据的独立门禁约束。

两份临时 APK 与构建日志仅用于本次验收，不是发布资产，也不会提交。

[English](acceptance-root-010.md)
