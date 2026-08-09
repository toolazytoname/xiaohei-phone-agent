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

## 尚未满足的 M6 门禁

当前仍不是公开 Beta。还缺：8–24 小时物理拔线待机/功耗、本地静态门禁之外的外部恶意软件/依赖复核、正式签名治理、模型再分发批准、备份/恢复覆盖、完整诊断包，以及真正正式签名且可复现的 APK。25 页面启动矩阵不能替代另一项 10–15 App Phone Agent 任务矩阵。
