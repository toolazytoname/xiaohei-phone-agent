# M6 公开 Beta 加固验收记录 — 部分完成

日期：2026-08-09  
压力基线：`0.2.0-alpha.1`（`versionCode=2`）；当前增量候选：`0.2.0-alpha.2`（`versionCode=3`）

## 已证明门禁

- debug 与 release 构建已分离。`aapt2 dump badging` 只在 debug APK 中发现 `application-debuggable`。使用 `androiddebugkey` 的 release 构建被拒绝；只有显式设置 `XIAOHEI_ALLOW_TEST_SIGNING=1` 才生成本地非 debuggable 测试包，该包不属于公开 Release。
- release 构建强制要求仓库外 keystore 路径、alias、store password 和 key password；Git 中没有密钥或口令。
- CycloneDX 1.5 SBOM 生成器会记录 App、可选 APK 哈希、可选 sherpa-onnx/模型包哈希，以及模型许可证尚待审核的事实。
- 配置 schema v1 会迁移旧 endpoint/model 字段、删除旧键、不复制 Token 明文，并具备幂等性。真机安装后已生成 `config_schema=1`。
- 在精确的最新离线 ASR debug 候选包上，真机完成 100 次确定性动作：100 成功、0 dispatcher 失败、0 Fatal/ANR 指纹、0 活跃录音客户端。
- 仓库校验通过：21 个必需工件、3 个有效 JSON Schema、本地链接有效、无禁止二进制、无凭据命中、无私有路径或设备标识命中。
- Alpha.2（`versionCode=3`）新增双语威胁模型和精确 APK 静态门禁。组合候选包通过签名、ZIP 安全路径、凭据特征、权限允许列表、导出组件权限门及 native 库清单；已记录 SHA-256。CycloneDX 现覆盖 App 及固定的 ASR/KWS 输入。
- 真机迁移通过：code 2→3 正常升级保留 schema v1；普通 3→2 降级被 Android 拒绝；显式维护降级保留配置；再次升级恢复 code 3 并保留 Assistant Role。
- code 3 事务式卸载先验证 DSP `DETACHED`，移除主 App 和 Assistant Role，保留 Companion；全新安装恢复 code 3 与 Assistant Role，Accessibility 默认关闭。
- 可复用页面 harness 在真机依次打开 14 个 Android 设置页面和 11 个系统 App：25/25 落到预期包或已记录的系统权限/Safety Center 中转页，0 启动失败、0 小黑 Fatal/ANR；没有为联系人或时钟自动授予权限。
- alpha.2 全新安装暴露并修复“声明 CAMERA 后打开外部相机仍需运行时权限”的首装问题；拒绝权限时 0 动作并返回可恢复状态。压力 harness 临时授权后在精确 alpha.2 完成 100/100、0 失败、0 Fatal/ANR、0 录音残留，并恢复 CAMERA 为未授权。
- 真机 `diagnostics.v1` 导出版本化 JSON，包含 Assistant/DSP/CPU 状态、能力/权限布尔值和配置 schema；不含序列号、Endpoint、Token、通知正文、UI 树或私有路径，并标记 `public_log_safe=true`。
- 已清点精确 native 版本和哈希。可复现 OSV 查询对工程推断的 PyPI 坐标 `onnxruntime@1.27.0` 与 `sherpa-onnx@1.13.4` 返回 0 条已知记录；文档明确该映射局限，不能据此声称绝对安全。
- 模型渠道备份/恢复新增版本化、有长度上限的文本格式，并通过纯 Java 往返与畸形输入测试。它只包含 ASR 模式、Agent 启用偏好、Endpoint 和模型名，绝不序列化 Token。可见恢复路径会清除 Keystore Token 并强制 Agent 关闭，因此恢复配置不会启动服务或发起付费请求。真机已确认该 UI 可见；真实用户配置备份/恢复演练仍是发布证据项。
- v2/v3 签名前已规范构建输入：生成的 DEX 固定 ZIP 时间戳、移除 ZIP 额外元数据，并关闭 API 26 不需要的 v1 JAR 签名。同一 RSA key 和同一 ASR/KWS 输入的两次完整 debug 构建得到字节一致 SHA-256 `e4132ec99f7fc5846e2aa8977d69c27274c20e4f3474529df484ffdb87fff0dc`，并验证签名。`scripts/verify-reproducible-build.sh` 可对指定 variant 重复该门禁。

## 尚未满足的 M6 门禁

当前仍不是公开 Beta。还缺：8–24 小时物理拔线待机/功耗、静态/OSV 之外的独立恶意软件引擎复核、正式签名治理、模型再分发批准、真实用户配置备份/恢复演练，以及用生产密钥重跑可复现门禁后的正式签名 APK。25 页面启动矩阵不能替代另一项 10–15 App Phone Agent 任务矩阵。
