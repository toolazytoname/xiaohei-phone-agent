# Conversation 有界传输

[English](conversation-transport.md) · [长期总纲](sovereign-mobile-agent-master-plan.zh-CN.md) · [状态](../STATUS.md)

`CHAT-003` 定义开放对话的网络边界。它只返回文字，不含工具、动作请求、手机上下文或 root/OpenCode 权限。

## 请求边界

- 单轮输入最多 4096 个 UTF-16 code unit；请求体最大 64 KiB。
- OpenAI 兼容路径固定为 `<base>/chat/completions`，请求 `stream=true`、最多 512 个输出 token。
- 公网地址必须是 HTTPS；明文 HTTP 只允许 `127.0.0.1`、`::1` 或 `localhost`。
- loopback 明确使用 `NO_PROXY`，避免 Clash/VPN 把手机内部服务转到外网；公网 HTTPS 仍遵循系统网络路径。
- 禁止 URL 用户信息、query、fragment、自动重定向和 Token 换行；Conversation Token 只从独立 Keystore 槽读取并仅进入 Authorization header。

## 响应边界

- 首选 `text/event-stream`，只有收到 `[DONE]` 才采用累计文字；连接提前结束会丢弃全部半截回复。
- 兼容服务忽略流式请求时，可解析标准 `application/json` 单次回复。
- 原始响应最多 64 KiB；默认连接超时 7 秒、读取超时 15 秒。
- 429、其他 HTTP、重定向、超时、响应过大、断流、空回复、格式错误和网络失败各自返回固定、无凭据的错误类别。
- 本层不自动重试。调用者取消会断开当前连接，并且一次请求最多回调一次。

## 自动验收

纯 Java 套件覆盖 11 条用例：SSE 成功、JSON 回退、断流、429、302 不跟随、读取超时、阻塞读取取消、响应上限、外部明文地址拒绝、header 注入配置拒绝和 IPv6 loopback 策略。测试不调用真实模型、不消耗模型 Token，也不记录用户文本或密钥。

```bash
bash apps/android/xiaohei-android/test.sh
bash apps/android/xiaohei-android/build.sh
```

这组证据完成 `CHAT-003` 的 mock 网络边界，不替代 `CHAT-004` 的用户界面路径、`CHAT-005` 的真人语音闭环或 `CHAT-012` 的最终对话验收。
