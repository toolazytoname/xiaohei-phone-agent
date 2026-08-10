# Root 加密备份核心

[English](root-encrypted-backup.md) · [root 边界](root-capability-boundary.zh-CN.md) · [状态](../STATUS.md)

`ROOT-005` 增加一个内存态、固定范围的 AES-256-GCM 信封，最多容纳 16 KiB 明确提供的备份元数据。它接收注入的 32 字节密钥，生成新的 12 字节 IV，防御性复制加密信封，并拒绝错误密钥、篡改、非法信封和非法密钥长度。核心没有文件、目录、root、Android、Token、网络或模型 API。

它不创建真实备份、不持久化密钥，也不证明设备上明文残留已删除。未来 adapter 必须使用设备 Keystore/不可导出密钥、固定批准的数据选择、原子加密存储、清理核验、恢复事务和独立离线介质恢复验收；本项不能关闭 `RELEASE-004`。
