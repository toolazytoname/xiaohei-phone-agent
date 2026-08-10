# OpenCode 停止与清理边界

[English](opencode-stop-cleanup.md) · [有界 runner](opencode-bounded-runner.zh-CN.md) · [状态](../STATUS.md)

`OC-006` 提供单个已登记任务的全局停止边界：取消 runner 信号、撤销全部活跃本地网关 Token、要求每个注入的 process/listener/tmux 资源句柄停止，并在不跟随符号链接的前提下递归删除唯一已登记的私有租约目录。

矩阵覆盖 3 个成功的注入资源、错误任务拒绝、重复停止幂等、递归工作区删除，以及链接外部目标仍保持完整。资源停止或私有租约释放失败都会以 `CLEANUP_FAILED` 报告。

该代码不会启动真实 OpenCode 进程、端口、监听器或 tmux 会话。实际适配器必须自行实现终止，并通过独立设备验收后才能声称这些真实资源已清理。

```sh
bash apps/android/xiaohei-android/test.sh
python3 scripts/verify-opencode-stop-cleanup.py
```
