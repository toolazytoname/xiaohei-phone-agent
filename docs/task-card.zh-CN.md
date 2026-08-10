# 只读任务卡

[English](task-card.md) · [任务计划](rules-first-task-plan.zh-CN.md) · [OpenCode 进度](opencode-progress-card.zh-CN.md)

UX-003 新增公开、只读的任务卡投影。它只展示已批准的目标摘要、已审核步骤数/当前步骤、时间和步骤预算、固定结果类别与接管状态；不能接收任务正文、task/request/plan 标识、路径、Token、模型回复、终端输出或推理过程。

默认卡说明没有已审核任务，且不启动任何内容。未来 adapter 只能在既有任务计划/策略/确认边界批准后提供任务卡；停止或接管都不得触发自动重试。当前 UI 不宣称存在真实 planner、OpenCode 进程或工具执行。
