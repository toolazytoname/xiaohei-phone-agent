# OpenCode 脱敏进度卡

[English](opencode-progress-card.md) · [有界 runner](opencode-bounded-runner.zh-CN.md) · [状态](../STATUS.md)

`OC-005` 只把类型化生命周期事件映射到可见只读卡：任务类别、等待/执行中/完成/失败/停止状态，以及受审核步骤上限约束的已完成步骤数。卡片可以安全写入公开日志。

它不能接收或显示任务正文、task/request/plan ID、文件系统路径、token 预算或用量、凭据、模型回复、终端输出或任意错误文本。真实 runner 尚未接线时，页面明确显示“未连接；未执行任务”。

当前卡片不能证明存在 live OpenCode 进程。未来适配器接线只能发出审核过的枚举事件，并必须保留 runner 的停止/清理和隐私边界。

```sh
bash apps/android/xiaohei-android/test.sh
python3 scripts/verify-opencode-progress-projection.py
```
