# OpenCode 有界 runner

[English](opencode-bounded-runner.md) · [工作区边界](opencode-workspace-boundary.zh-CN.md) · [状态](../STATUS.md)

状态日期：2026-08-10。`OC-004` 提供纯 Java、注入适配器的 runner 边界：只接受 pending OpenCode 任务及其匹配的私有工作区租约，并强制审核后的 profile、agent 标记、100–60,000 ms 超时、1–4,096 token、1–32 步和 1–4,096 代码点脱敏输出预算。

适配器只获得类型任务、租约、有界计量器和取消信号。token、步骤或输出越界都会成为私有结构化预算失败；超时和取消会中断注入 worker。结果只含用量计数，永远不可安全写入公开日志。

它不是实际 `oc run` 集成：不启动进程、不连接模型/网络、不读写任务内容，也不开放命令、路径、凭据、root 或 UI 权限。后续经过审核的适配器必须计量每个模型/工具事件，并补安全打开/清理语义，才可以声称真实执行。

```sh
bash apps/android/xiaohei-android/test.sh
bash scripts/verify.sh
```
