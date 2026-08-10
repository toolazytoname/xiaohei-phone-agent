# Root Profile 事务

[English](root-profile-transaction.md) · [备份](root-encrypted-backup.zh-CN.md) · [状态](../STATUS.md)

`ROOT-006` 增加固定 profile 的事务账本：预检精确 profile 标识和摘要、保存有效摘要快照、标记外部已完成的 apply、拒绝 rollback 摘要漂移，再记录重启后核验状态。错误顺序、标识或摘要都会拒绝。该类不安装/卸载 profile、不访问文件、不重启，也不调用 root。

状态机只是未来有界 adapter 的准备。真实 profile 安装需要加密快照持久化、已验证包/镜像来源、明确用户确认、设备重启观测、rollback 执行和独立设备验收。
