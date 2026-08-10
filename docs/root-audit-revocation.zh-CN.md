# Root 审计与撤销

[English](root-audit-revocation.md) · [broker](root-broker-core.zh-CN.md) · [状态](../STATUS.md)

`ROOT-008` 将每个内存 broker 决定记录为脱敏事件：只有序号、固定 action 标识（或 `unknown`）和固定决定。它刻意不记录 request ID、signer、参数、路径、命令、时间戳、Token、设备输出或用户内容；公开契约也仅包含这些安全字段。

`revokeAll()` 清空一次性 request 集合，并永久关闭该 broker 实例：之后每个请求都会记录为 `deny_revoked`。没有重新启用路径、Token 持久化、传输、root 调用、shell 或设备操作。未来全局停止接线只能在接入真实 broker 后调用它，并需独立设备验收。
