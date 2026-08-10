# 授权层级

[English](authorization-tiers.md) · [Root 边界](root-capability-boundary.zh-CN.md) · [状态](../STATUS.md)

`POLICY-003` 分离 Android、OpenCode 与 root audience。Android 凭据只能授权 Android gateway 元数据；OpenCode 凭据只能授权 OpenCode gateway 元数据；root broker 未实现时所有 root 请求失败。任何低层凭据都不能跨层升级。

它是纯内存策略，不是 token 签发器、传输、UI、root broker 或执行路径。保留此隔离后，`ROOT-002` 才可增加签名绑定固定动作。
