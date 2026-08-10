# OpenCode 任务提案协议

[English](opencode-task-protocol.md) · [总纲](sovereign-mobile-agent-master-plan.zh-CN.md) · [工具网关](loopback-tool-gateway.zh-CN.md) · [状态](../STATUS.md)

状态日期：2026-08-10。`OC-002` 定义小黑“当前用户的复杂任务请求”和未来 OpenCode 执行器之间的私有、待确认、dry-run 提案边界。它不会启动 OpenCode、创建工作区、发送 Prompt、打开网络、调用 Android/root，也不会执行命令。

## 收窄后的协议

只有三类审核过的任务能够跨越该边界：

| 类型 | 当前含义 | 不授予的能力 |
|---|---|---|
| `project_summary` | 提议总结项目结构 | 文件访问或模型调用 |
| `test_diagnosis` | 提议诊断已选测试证据 | 运行测试或终端访问 |
| `controlled_file_organization` | 提议未来整理明确范围内的项目文件 | 创建工作区、修改文件或访问路径 |

提案只能来自现有 `UnconfirmedActionRequest`：它必须由当前输入的用户文字派生，并保持 `high`、`pending`、`dry_run=true`、`requires_confirmation=true` 与不可公开日志。协议重新绑定新的 `task_id`、`plan_id`，把 audience 固定为 `opencode_gateway`，并固定 `execution_state=not_started`。

```text
用户输入的复杂任务
        │ 既有路由 + pending ActionRequest 校验
        ▼
OpenCode 任务提案（私有、dry-run、待确认）
        │ 后续：确认 + 工作区 + 有界 runner
        ▼
OpenCode 执行 —— OC-002 尚未实现
```

`opencode-task.v1` 是封闭契约。它没有 `command`、`argv`、环境变量、当前目录、工作区、URL、Token、root、进程或执行结果字段。JSON 只是可审计的提案元数据，绝不是执行 capability。私有 instruction 在公开安全元数据中会被脱敏；可展示的只有代码点数。

## 验收证据

纯 Java 矩阵覆盖三类审核任务、十条指令形攻击、六条非法来源/类型/身份用例、私有元数据不可修改和零模型/动作/执行调用。五个公开 fixture 覆盖两个合法提案，以及未知 command 字段注入、伪造来源与 live 状态拒绝。

```sh
bash apps/android/xiaohei-android/test.sh
python3 scripts/verify-opencode-task-contract.py
python3 scripts/verify-opencode-task-boundary.py
bash scripts/verify.sh
```

## 仍需完成的工作

`OC-003` 要为每个任务分配受限工作区，并拒绝路径穿越、符号链接逃逸和跨任务读取；`OC-004` 再增加有界 runner；`OC-005` 至 `OC-007` 再补脱敏进度、停止/清理和受限工具。本协议不能被当作 OpenCode 已运行、文件已读写或确认 UI 已存在的证据。
