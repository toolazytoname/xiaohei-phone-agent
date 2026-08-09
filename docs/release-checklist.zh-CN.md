# 公开发布门禁清单

公开工件必须全部满足以下条件。本地测试签名 APK 不能称为公开 Release。

- Release variant 不含 `application-debuggable`，使用仓库外保管的非 debug 正式签名密钥，并遵循[签名治理](signing-governance.zh-CN.md)。
- versionCode/versionName 单调递增，APK SHA-256 与 CycloneDX SBOM 同时发布。
- 若候选包内嵌离线 ASR/KWS 资产，必须单独审核并记录其精确再分发权利，见[模型再分发审查](model-redistribution-review.zh-CN.md)；无内嵌模型的通用候选包则必须在溯源中记录这一范围。仅上游代码许可证通过不足以发布含模型二进制。
- 通用 APK 与 OnePlus 设备增强为独立工件。私有 OEM 资产没有进入 Git、SBOM、APK、日志或 Release，除非已证明再分发权利。
- 全新安装、保留配置升级、事务式回滚、降级行为和完整卸载都有当前版本证据。
- 通知与 Accessibility 默认关闭，打开 Android 设置前先解释用途。
- 双语隐私说明、兼容矩阵、Release Notes、漏洞报告渠道和已知限制保持最新。
- 精确候选包通过 100 次任务、20+ 页面回归、8–24 小时待机/功耗和恶意软件/依赖扫描。
