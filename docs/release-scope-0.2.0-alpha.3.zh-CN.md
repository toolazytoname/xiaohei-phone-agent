# 0.2.0-alpha.3 发布范围

[English](release-scope-0.2.0-alpha.3.md)

状态：候选范围已冻结；下列门禁完成前仍不执行公开上传。

## 公开源码与通用 APK

可公开候选物是一份非 debuggable 的 `arm64-v8a` 通用 Android APK，由本仓库源码构建，不内嵌 ASR/KWS 模型资产。它提供设备允许时的 App/快捷设置/系统助手主动入口、确定性动作、可选通知汇总与确认式草稿、受本地策略约束的可见 Phone Agent、全局停止、诊断和配置迁移。

如果设备没有兼容的系统识别器，通用 APK 不承诺离线语音识别。它不包含或安装 OnePlus DSP Companion、OEM 模型、root 模块、模型渠道凭据、私有 Endpoint、聊天内容或设备标识。

## 本地验收包

约 64 MB 的 ASR/KWS 组合包是为所有者 OnePlus 8T 和 AOSP profile 本地构建的私有验收工件。在精确模型再分发权利没有解决前，它不会上传 GitHub Releases。其 CPU“小黑小黑”是默认关闭、前台可见的实验模式，不代表 DSP。

## 设备增强

OnePlus 8T DSP Companion/profile 始终是独立源码与本地构建工件。当前证据只覆盖精确 OnePlus 8T / LineageOS 21 profile 和已验证的原厂词；它不打进通用 APK，也不宣称任意 Android 可用。

## 公开门禁

任何 APK 作为公开 Alpha 上传前，精确通用候选包必须同时具备：正式/发布签名溯源、CycloneDX SBOM 与 checksum、独立恶意软件扫描、全新安装与回滚证据，以及当前中英文发布/安全文档。物理待机/功耗仍阻断低功耗和续航宣称，但不会把通用包变成 DSP 工件。若公开内嵌模型，还必须另行取得明确模型权利批准。
