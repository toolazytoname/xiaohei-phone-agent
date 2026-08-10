# OpenCode 受限工具

[English](opencode-restricted-tools.md) · [停止/清理](opencode-stop-cleanup.zh-CN.md) · [状态](../STATUS.md)

`OC-007` 是未来适配器的 fail-closed 意图策略。仅项目摘要、测试诊断和受控文件整理可被分类为允许；root、敏感路径、破坏性 Git/删除、网络传输、shell 链接/转义和未知文本都会在执行前拒绝。

它不是 shell 解析器或命令执行器。未来真实适配器只能接收策略批准的类型操作，不能接受自由命令字符串。
