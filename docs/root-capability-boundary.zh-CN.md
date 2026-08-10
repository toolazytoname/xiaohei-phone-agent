# Root 能力边界

[English](root-capability-boundary.md) · [威胁模型](threat-model.zh-CN.md) · [状态](../STATUS.md)

状态：`ROOT-001` 只定义治理，不实现能力。手机可能已 root，但小黑尚无产品级 root broker；通用 `su -c`、shell、任意路径、system 分区写入、boot image、凭据库、支付/OTP/密码、破坏性 Git 与网络外传均保持拒绝。

## 未来允许目录

仅记录三个未来固定 action ID：读取服务状态、读取电池状态、读取音频状态。它们是元数据，不是命令或权限。后续 broker 必须为每个 ID 绑定精确输入/输出 schema、签名身份、新鲜本机确认、有界超时、脱敏审计和撤销路径。

## 威胁与恢复责任

| 威胁 | 必须的边界 | 恢复责任 |
|---|---|---|
| Prompt/工具注入 | 不允许自由 shell；只允许类型化固定 action ID | 产品策略 + 所有者审核 |
| 错目标/系统损坏 | 精确目标 schema、dry-run、可逆方案 | 设备人类所有者 |
| 凭据/隐私泄露 | 永久拒绝；不允许 root 文件浏览 | 设备人类所有者 |
| 启动失败/失去访问 | 改动前离线备份和独立设备验证 | 设备人类所有者 |
| Token/App 被攻破 | 签名绑定、短作用域、全局撤销 | 未来 broker + 所有者 |

任何文档或目录都不能把 Android、OpenCode、模型、中转或无障碍权限提升为 root。仅在 `POLICY-003` 建立独立授权层后，`ROOT-002` 才能开始实现。
