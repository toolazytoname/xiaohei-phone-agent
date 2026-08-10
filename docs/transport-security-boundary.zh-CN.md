# 传输与能力令牌安全边界

[English](transport-security-boundary.md) · [Conversation 传输](conversation-transport.zh-CN.md) · [Loopback 网关](loopback-tool-gateway.zh-CN.md)

`SEC-003` 记录当前边界，不把它写成已经完成的网络渗透测试。

| 平面 | 本地强制规则 | 自动证据 | 仍需门禁 |
|---|---|---|---|
| 公网模型传输 | 只允许 HTTPS；Android 网络配置默认禁用明文。客户端不会安装宽松 trust manager 或 hostname verifier。 | 外部 HTTP、URL user info/query/fragment、header 换行注入、溢出和超时用例均拒绝。 | 独立设备上的不可信 CA、用户信任 CA、过期证书、主机名不匹配和重定向拦截演练。 |
| 手机本地中转 | HTTP 只接受 `127.0.0.1`、`::1` 或 `localhost`；loopback 使用 `NO_PROXY`，Clash/VPN 不会转发手机内部请求。 | IPv6 loopback 和外部明文拒绝传输用例通过。 | 在目标手机确认真实绑定地址/端口和代理行为。 |
| 重定向 | 禁止 `HttpURLConnection` 跟随重定向；任何 3xx 都丢弃且不重试。 | 精确 302 映射为 `REDIRECT_REJECTED`。 | 真实 HTTPS→HTTP 与跨主机重定向演练。 |
| 工具授权 | 网关只接受数值 loopback peer、相等的非负 UID，以及绑定单次精确 call 的私有内存 receipt/token。 | 跨 UID/非 loopback、范围漂移、过期、时钟回退、重放、撤销和外部 gateway 矩阵通过。 | 真实 same-UID listener/adapter 必须从连接派生 peer 证据，不能相信请求 JSON。 |

当前传输没有证书钉扎，故意保留 Android 标准 TLS 校验；启用钉扎前必须有证书轮换和独立设备恢复证据。任意传输失败后都不允许自动重试。
