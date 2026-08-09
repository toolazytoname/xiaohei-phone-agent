# M6 公开 Beta 加固验收记录 — 部分完成

日期：2026-08-09  
候选版本：`0.2.0-alpha.1`（`versionCode=2`）

## 已证明门禁

- debug 与 release 构建已分离。`aapt2 dump badging` 只在 debug APK 中发现 `application-debuggable`。使用 `androiddebugkey` 的 release 构建被拒绝；只有显式设置 `XIAOHEI_ALLOW_TEST_SIGNING=1` 才生成本地非 debuggable 测试包，该包不属于公开 Release。
- release 构建强制要求仓库外 keystore 路径、alias、store password 和 key password；Git 中没有密钥或口令。
- CycloneDX 1.5 SBOM 生成器会记录 App、可选 APK 哈希、可选 sherpa-onnx/模型包哈希，以及模型许可证尚待审核的事实。
- 配置 schema v1 会迁移旧 endpoint/model 字段、删除旧键、不复制 Token 明文，并具备幂等性。真机安装后已生成 `config_schema=1`。
- 在精确的最新离线 ASR debug 候选包上，真机完成 100 次确定性动作：100 成功、0 dispatcher 失败、0 Fatal/ANR 指纹、0 活跃录音客户端。
- 仓库校验通过：21 个必需工件、3 个有效 JSON Schema、本地链接有效、无禁止二进制、无凭据命中、无私有路径或设备标识命中。

## 尚未满足的 M6 门禁

当前仍不是公开 Beta。还缺：8–24 小时物理拔线待机/功耗、20+ App/页面矩阵、精确候选包恶意软件/依赖扫描、正式签名治理、模型再分发批准、升级/降级/备份/恢复矩阵、完整诊断包，以及真正正式签名且可复现的公开 APK。
