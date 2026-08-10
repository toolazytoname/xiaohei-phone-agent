# 有界工具执行生命周期

[English](tool-execution-lifecycle.md) · [授权边界](loopback-tool-gateway.zh-CN.md) · [工具目录](versioned-tool-catalog.zh-CN.md) · [状态](../STATUS.md)

状态日期：2026-08-10。`TOOL-003` 新增一个纯 Java 协调器，只负责一次已经授权的适配器调用。它强制目录审核过的逐工具超时、取消、一次性授权/幂等、受限结构化输出和私有错误收敛。它有意尚未接入 Android Activity、UI、socket 监听器、OpenCode、root、进程启动器或网络客户端。

## 权限与生命周期

```text
本机用户确认
       │ 只消费一次
       ▼
loopback/同 UID 网关 ── 完整调用摘要 + 幂等键 + 超时
       │ 只授权一次
       ▼
私有执行许可
       │ 消费一次，并匹配未变化的调用
       ▼
执行协调器 ── 最多提交一个注入式适配器
       ├─ 成功 / 有界输出
       ├─ 类型化适配器失败 / 需要回滚
       ├─ 用户、全局停止、断连或调用线程取消
       └─ 到期 / 中断 worker 并关闭 executor
```

公开 JSON 调用或 Token 本身不是可执行权限。协调器只接受成功网关授权产生的私有运行时许可；取出即清空，复用返回 `authorization_replay`。授权后调用字段发生任何变化，会在适配器运行前返回 `scope_changed`。

调用的 `timeout_ms` 写入 capability 摘要，范围为 100 ms 到该工具不可变目录超时上限。协调器用它执行单调时钟 `Future.get`。完成与取消共享一个同步终态点：先到者决定结果，完成后的迟到取消返回 false。超时、运行中取消或调用线程中断都会请求 `Future.cancel(true)`；所有终态都会解绑取消信号、调用 `shutdownNow`，并最多等待测试 worker 100 ms。

## 结构化结果

`tool-result.v1` 只包含稳定枚举和有界数据：

| 状态 | 典型错误码 | 适配器调用数 |
|---|---|---:|
| `success` | `none` | 1 |
| `denied` | 授权拒绝/重放/过期、范围变化 | 0 |
| `cancelled` | 用户、全局停止、客户端断连、调用线程中断 | 0–1 |
| `timeout` | 超过截止时间 | 1 |
| `failed` | 缺适配器、非法输出、网络不可用、进程非零退出、适配器失败 | 0–1 |
| `rollback_required` | 需要回滚 | 1 |

输出是不可修改的字符串 Map：最多 32 项，字段名只能是小写 snake case，值最多 1024 字符。结果使用单调开始/结束/耗时字段，并固定 `public_log_safe=false`。原始异常文本、堆栈、模型正文、截图、无障碍树和 Android 私人数据都不属于该契约。

## 验收证据

确定性 Java 矩阵共 25 组：

- 五个目录工具各通过注入式成功适配器执行一次；
- 五个截止时间测试中断阻塞 worker；
- 五个取消路径覆盖启动前用户取消、运行中用户取消、全局停止、客户端断连和调用线程中断；
- 五个结构化失败路径覆盖模拟网络不可用、模拟进程非零退出、未分类适配器异常、需要回滚和非法输出；
- 五个授权路径覆盖缺少/仅签发权限、执行许可重放、范围变化及幂等重放。

九个运行中的 worker 明确认收中断，每个结果的适配器调用数都是 0 或 1；另有五个公开结果 fixture 独立验证封闭契约。

```sh
bash apps/android/xiaohei-android/test.sh
python3 scripts/verify-tool-gateway-contract.py
python3 scripts/verify-tool-execution-contract.py
python3 scripts/verify-tool-execution-boundary.py
bash scripts/verify.sh
```

## 证据边界与剩余工作

`TOOL-003` 测试里的适配器全部是内存注入的 test double。`network_unavailable` 和 `process_exit_nonzero` 只证明错误码映射；它们**不能**证明真实 socket、子进程、Android 组件、麦克风或 root 资源已经打开或被杀掉。worker 中断测试证明协调器会请求中断并关闭自己的 executor；未来真实适配器还必须协作关闭自身的进程/网络/平台句柄，并另做真实 kill、断网和设备验收。

Android 工具、OpenCode 工具和 root 工具继续使用相互独立的审核目录与 audience。后续任务必须补真实适配器、传输层自行产生的可信 peer 证据、可见确认、前后观察、回滚与全局停止接线，不能把本协调器扩张成通用 shell 或自动点击批准按钮的路径。
