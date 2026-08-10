# OpenCode 受控任务验收矩阵

[English](opencode-acceptance-matrix.md) · [受限工具](opencode-restricted-tools.zh-CN.md) · [状态](../STATUS.md)

`OC-008` 运行 9 轮合成验收：项目摘要、测试诊断和受控整理各 3 轮。每轮创建类型化 pending 任务、评估受限策略、使用注入的有界适配器、检查结构化成功结果，并释放新建私有临时租约。每种类别还拒绝一条 Git/网络对抗意图。

它证明组合后的本地边界，不是实际 OpenCode 任务：不会打开用户项目、进程、模型连接、网络或真实工具；结果明确为 `real_opencode=0`。
