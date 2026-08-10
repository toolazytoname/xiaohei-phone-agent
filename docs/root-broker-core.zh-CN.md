# Root broker 核心

[English](root-broker-core.md) · [Root 边界](root-capability-boundary.zh-CN.md) · [状态](../STATUS.md)

`ROOT-002` 只增加内存中的固定动作授权核心。它识别 3 个只读 action ID，要求精确 broker signer 和空参数对象，并且每个 request ID 只能消费一次。缺字段、未知 signer、畸形请求、非精确参数和重放都会拒绝。

它不调用 root：没有 `su`、shell、Android API、传输、token 持久化、root 进程或设备改动。未来适配器还必须绑定真实签名实现、新鲜确认、schema 和独立设备验收。
