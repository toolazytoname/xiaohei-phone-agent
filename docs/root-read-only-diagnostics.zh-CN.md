# Root 只读诊断

[English](root-read-only-diagnostics.md) · [broker](root-broker-core.zh-CN.md) · [状态](../STATUS.md)

`ROOT-003` 为现有三个固定 broker action 增加纯投影边界。`read_service_status` 最多返回服务、端口、包和 profile 的可用性；电池和音频 action 各只返回一个对应可用性状态。每项只有固定类别、`available` / `unavailable` / `unknown` 三种状态之一和固定公开标签。

没有命令、路径、PID、端口号、包名、profile 内容、电池数值、音频内容、用户文本、日志文本、Token 或原始适配器输出字段。缺失源状态归为 `unknown`，结果最多四项。这个类不调用 broker、`su`、shell、Android、网络、文件系统或设备 API。未来 root adapter 必须先通过精确 broker action，再私下把原始设备观测映射成这些状态、脱敏自身审计，并在独立设备验收。
