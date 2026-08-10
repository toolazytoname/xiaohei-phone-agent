# OpenCode 任务工作区边界

[English](opencode-workspace-boundary.md) · [任务协议](opencode-task-protocol.zh-CN.md) · [状态](../STATUS.md)

状态日期：2026-08-10。`OC-003` 为未来 OpenCode runner 新增任务私有工作区租约。它只会在可信 App 私有根目录下创建空的 `input`、`output` 目录；不会启动 OpenCode、读写任务内容、创建网络连接，也不会接受用户或模型提供的文件系统根目录。

## 隔离规则

```text
可信 App 私有根目录
  └── xiaohei-opencode-tasks/
        └── <task-id>/
              ├── input/     只接受该租约 INPUT 的相对路径
              └── output/    只接受该租约 OUTPUT 的相对路径
```

运行时租约私下保存真实路径。公开 `opencode-workspace-lease.v1` 元数据只暴露任务身份、两个允许区域、`private_app_storage`、`path_exposure=none` 和 `public_log_safe=false`，绝不序列化真实路径。

解析只接受非空、最多 512 字符的相对路径。它拒绝绝对路径、`.`/`..` 段、逃出所选区域的路径、任一已存在的符号链接组件（包括根）、重复 task ID、非法任务协议状态，以及试图使用另一任务绝对/穿越路径的请求。成功结果只是给后续受限 runner 的私有候选路径，不会打开任何内容。

## 证据边界

Java 测试创建新的临时根目录并在结束后删除，证明两份独立租约、4 条安全路径、7 条穿越/绝对路径拒绝、3 条符号链接拒绝和 2 条跨任务拒绝，同时报告零内容读取、写入和进程调用。它不测试真实 OpenCode runner，也不能解决后续打开时的 TOCTOU；`OC-004`/`OC-006` 必须采用安全的 runner、打开和清理语义。

```sh
bash apps/android/xiaohei-android/test.sh
python3 scripts/verify-opencode-workspace-lease-contract.py
python3 scripts/verify-opencode-workspace-boundary.py
bash scripts/verify.sh
```
