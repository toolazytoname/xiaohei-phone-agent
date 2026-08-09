# 发布签名治理

状态：公开发布前的内部策略。本页只标识发布证书；绝不保存私钥、口令、Keychain 条目或文件系统位置。

## 当前发布身份

| 字段 | 值 |
|---|---|
| Subject | `CN=Xiaohei Release, O=toolazytoname, C=CN` |
| 密钥 | RSA 4096 / SHA-256 with RSA |
| 证书 SHA-256 | `1c0cf5bf518c3b63037dae70388974551bb1f0f851084328a48af13ebcc12c07` |
| APK 签名方案 | v2、v3；因 `minSdk=26` 有意关闭 v1 |
| 首个内部 release 候选 | `0.2.0-alpha.2 (3)`，SHA-256 `6ad593561125af22fb10161f286c2f15a906af8734db9c065d3b80b2ffd9a26a` |

PKCS#12 keystore 位于公开仓库之外。口令只在本机 macOS Keychain 中，不能进入 shell history、Git、Release Notes 或 APK。

## 发布流程

1. 只把发布口令读入当前进程环境。
2. 使用 `XIAOHEI_BUILD_VARIANT=release`，以及 `build.sh` 要求的仓库外 keystore/alias/口令变量构建。
3. 使用同一模型输入和签名身份运行 `scripts/verify-reproducible-build.sh`；它要求两次构建字节一致，并执行精确 APK 静态扫描。
4. 同时发布 APK SHA-256、SBOM、证书指纹、源码 revision、模型输入哈希与已知限制。
5. 不得把不同签名的 release APK 覆盖安装到 debug 安装。应使用干净验收设备，或先记录好配置/回滚方案后再卸载。

## 首次公开发布前仍必须完成

- 制作发布 keystore 的加密离线恢复副本，并在私有记录中说明恢复控制人；不要写入 Git。
- 确认模型再分发权利，并把审核附到发布证据。
- 完成发布清单，包含物理待机/功耗和独立恶意软件引擎证据。
- 让 release CI 凭据处于最小权限 secret store，禁止写入构建日志。

## 泄露与轮换

如怀疑私钥、Keychain 口令或签名主机泄露，应停止发布、撤销 CI 权限、私下保全审计证据，并创建替换身份。公开 APK 存在后，需要 Android signing lineage/rotation 计划让受支持设备验证过渡；安全公告中记录旧、新证书指纹。

相关：[发布清单](release-checklist.zh-CN.md) · [威胁模型](threat-model.zh-CN.md) · [M6 验收](acceptance-m6.zh-CN.md)
