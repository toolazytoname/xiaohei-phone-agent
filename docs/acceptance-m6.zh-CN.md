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
- 模型渠道备份/恢复新增版本化、有长度上限的文本格式，并通过纯 Java 往返与畸形输入测试。它只包含 ASR 模式、Agent 启用偏好、Endpoint 和模型名，绝不序列化 Token。真机演练已粘贴固定备份，观察到“Token 已清除、Phone Agent 保持关闭且未启动服务”，随后恢复测试前的非敏感渠道配置并清除私有验收快照。演练前设备没有已配置 Token；含 Token 迁移有意不支持，必须重新输入。
- v2/v3 签名前已规范构建输入：生成的 DEX 固定 ZIP 时间戳、移除 ZIP 额外元数据，并关闭 API 26 不需要的 v1 JAR 签名。同一 RSA key 和同一 ASR/KWS 输入的两次完整 debug 构建得到字节一致 SHA-256 `e4132ec99f7fc5846e2aa8977d69c27274c20e4f3474529df484ffdb87fff0dc`，并验证签名。`scripts/verify-reproducible-build.sh` 可对指定 variant 重复该门禁。
- 已创建仓库外的 RSA-4096 小黑 release 身份，口令只保存在本机 Keychain。使用该身份的内部 non-debug Alpha release 候选连续构建两次字节一致，并通过精确扫描：`6ad593561125af22fb10161f286c2f15a906af8734db9c065d3b80b2ffd9a26a`。证书指纹以及保管/轮换策略已公开记录，但不暴露私有材料。
- USB 物理拔除后，Android 14 TLS 无线调试成为唯一 ADB transport。通过该拔线连接，精确 alpha.2 debug 包再次完成 100 次压力 harness（100 成功、0 失败、0 Fatal/ANR、0 活跃录音客户端）及 25 页面启动 harness（25/25、0 小黑 Fatal/ANR）。这只是短时交互回归，不是待机/功耗证据。
- Alpha.3 冻结了不内嵌模型资产的可公开通用范围。仓库外 RSA-4096 release 身份连续两次生成字节一致的非 debug 候选包，SHA-256 `f4ce5eb9dcf6695a3f0cf63d4f9ae427c197170580c0bb894514d551a84c98b3`。本地发布包记录干净源码 revision，并把它与证书指纹、单组件 SBOM、checksum 和中英文说明绑定；尚未上传。
- 精确 release 签名的通用 APK 已在干净 Android 14 ARM64 AOSP profile 全新安装。onboarding 没有请求权限，基础页如实显示无 DSP profile、未内嵌 KWS；随后卸载确认包不存在。该轮补充 M3–M5 更广的 debug 验收，不取代后者。
- ClamAV 1.5.4 使用 3,627,998 条签名独立扫描精确通用 release 候选与精确私有组合模型 debug 候选（`5b5077f9fe12413ee268457a981c863057e546995c675d851343177dc6bc6e17`）：扫描 2 个文件、感染 0 个。该结果是有边界的恶意软件引擎证据，不是“绝无漏洞”保证。
- 仓库外已生成 `age`/X25519 签名恢复归档，identity 存于本机 Keychain；完整解密/哈希演练与 release keystore 及证书 `1c0cf5bf518c3b63037dae70388974551bb1f0f851084328a48af13ebcc12c07` 一致。它目前仍是签名 Mac 上权限收紧的**待移出暂存**；没有独立控制的离线介质，不能关闭离线恢复门禁。
- 完整本地 ASR/KWS debug alpha.3 已通过 TLS 无线 ADB 覆盖升级到 OnePlus 8T。当前为 `versionCode=4`，精确静态扫描通过，通知/无障碍访问保持关闭，也没有启动录音服务。模型权重仍为私有本地输入，不进入 Git 或公开 Release。
- v2 设备端待机监控已在干净 Android 14 ARM64 模拟器完成一次零时长、息屏独立演练。完成的原始 TSV 含有 `requested_at`、`sampling_started_at` 与 `preflight_wait_s`；采集结果为 1 个非交互、未供电、无通话、无录音、无小黑 wakelock 样本并通过。它只验证监控的证据格式，不是 OnePlus 的物理功耗证据。
- 2026-08-11：由 `33cdeea` 构建的 debug APK（SHA-256 `e8a447b1ce459d6ec68d733507c74123b23dea25313c84fb690afe1cc6435167`）在 OnePlus 8T 上原地升级，Assistant 角色和麦克风权限均保留。确定性压力脚本再次完成 100/100 个安全动作，dispatcher 失败、小黑 Fatal/ANR 和活跃录音客户端均为 0。该 Android 14/Lineage 表面在熄屏/通知栏前景时省略 `topResumedActivity`；启动矩阵仅以只读 `mFocusedApp` 作为回退，随后 14 个 Settings 和 11 个系统 App 目标均通过（25/25），小黑 Fatal/ANR 为 0。这只是短时交互证据，不代表语音、隐私、待机功耗或混合工作负载验收。

## 尚未满足的 M6 门禁

当前仍不是公开 Beta。还缺：8–24 小时物理拔线待机/功耗、把已验证的加密签名恢复包转移到独立控制的离线介质，以及最终公开上传批准。通用 release 范围不内嵌模型，因此尚未解决的模型再分发权利不再阻断这份精确 APK；它仍阻断 ASR/KWS 组合包公开。M5 已另有十 App 任务矩阵。
