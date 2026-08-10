# Root 服务生命周期预检

[English](root-service-lifecycle.md) · [诊断](root-read-only-diagnostics.zh-CN.md) · [状态](../STATUS.md)

`ROOT-004` 只接受已新鲜确认的 `stop` dry-run，且预期与实际观测的包名、进程名、PID、端口必须全部精确匹配。缺数据、`start`、过期/缺失确认或任一不匹配都会拒绝。预检不会向 PID 发信号、不会启停服务、打开端口、调用 root 或读取设备。

契约刻意不是 public-log-safe，因为真实 adapter 会携带目标元数据。未来真正执行还需要独立审批的签名请求、私有审计、有界超时、停止后的缺席检查、回滚方案和独立设备验收；此策略本身不授予生命周期权限。
