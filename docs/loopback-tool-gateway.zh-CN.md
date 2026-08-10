# Loopback 工具网关授权核心

[English](loopback-tool-gateway.md) · [工具目录](versioned-tool-catalog.zh-CN.md) · [新鲜确认](fresh-confirmation.zh-CN.md) · [状态](../STATUS.md)

状态日期：2026-08-10。`TOOL-002` 在一次已消费的本机确认和一次精确工具调用之间加入纯进程内授权核心。它不打开 socket、不解析模型文字、不调用 Android 适配器，也不把授权结果报告为动作成功。后续传输层必须从已接受的本机连接产生 peer 证据，绝不能从请求 JSON 复制 peer 字段。

## 两次一次性交换

1. `FreshConfirmationGate` 只接受前台、解锁且可交互状态下的本机用户手势；精确比较 task/request/plan/target/content 后，以私有 capability receipt 返回 `ALLOW_ONCE`。
2. 只有网关能消费该 receipt。它先校验 peer 与调用元数据，再把 receipt 一次性换成不透明内存 Token；复用同一个结果会得到 `CONFIRMATION_REPLAY`。
3. Token 只授权与其加盐 SHA-256 调用摘要完全一致的一次调用。成功、过期、时钟回退、本机范围/目录失败、幂等重放或显式全局撤销都会移除活动 Token。

receipt 只带 confirmation/task/request/plan ID，不带目标、内容、UI 文字、截图、无障碍树或摘要。确认与调用的 task/request/plan 不同时，receipt 会被消费并拒绝。

## Peer 与 Token 边界

本地与远端端点证据都必须是数值型 IPv4 loopback `127.0.0.0/8` 或 IPv6 `::1`；通配地址、局域网/公网地址、`localhost`、缺失值及其他 IPv6 地址全部拒绝。传输层报告的 peer UID 必须与网关 owner UID 相同，且两者不能为负数。

默认签发器使用 `SecureRandom` 生成 128-bit Token ID。Token：

- 只存内存、一次性、不可写入公开日志，并使用调用方提供的单调时钟，仅有效 1–30 秒；
- 绑定 confirmation、task、ActionRequest、plan、call、工具、版本、风险、受众、参数和幂等键；
- 只存在于签发它的网关 registry，最多 16 个活动 Token 和 256 条 fail-closed 重放记录；
- 不能跨网关实例使用，在精确到期、时钟回退、任一本机范围变化或 `revokeAll()` 后也不能使用。

JSON capability Schema 是可审计元数据，不是自行认证的 bearer 凭据。重建或修改 JSON 不能制造私有运行时 `Token` 或 registry 记录。调用摘要用于发现范围漂移，它不是签名，也不能替代内存 registry 或 peer 检查。

## 契约与验收

`tool-call.v1` 现在包含 task/request/plan/call 身份、受审目录范围、有界字符串参数、16–128 字符幂等键、单调请求时间与 `public_log_safe=false`。`capability-token.v1` 增加对应身份、确认 ID、精确调用摘要、1–30 秒单调时窗、`single_use=true` 与 `persistence=memory_only`。工具结果默认也不可写入公开日志。

纯 Java 矩阵覆盖 50 组：10 次精确单次授权、10 个非 loopback/跨 UID peer、5 个缺失/伪造/重放/错范围确认、7 个调用范围变化、3 个目录变化、5 个非法/未来/陈旧/私有元数据调用、5 个过期/时钟及 5 个重放/撤销/跨网关场景。每个结果的模型、动作和执行调用均为 0。七个公开 fixture 文件独立覆盖一组合法 call/token、四类非法 Token 和六组 peer。

```sh
bash apps/android/xiaohei-android/test.sh
python3 scripts/verify-tool-gateway-contract.py
python3 scripts/verify-loopback-tool-gateway-boundary.py
bash scripts/verify.sh
```

## 尚未完成的执行工作

当前 Android UI 与 Activity 不引用 `ToolGateway`，没有 socket 监听器或适配器调用。`TOOL-003` 必须加入结构化超时/取消/幂等执行结果；后续集成还必须提供由可信传输层产生的 peer 证据与可访问确认 UI。OpenCode 与 root 保持独立受众，不能使用 Android capability。
