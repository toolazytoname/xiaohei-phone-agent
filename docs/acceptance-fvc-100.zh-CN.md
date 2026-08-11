# FVC-100：聊天到动作安全交接验收

日期：2026-08-11 · 范围：本地边界与自动测试；不代表模型可靠性或真实工具执行。

- `ConversationPromptPolicyTest` 将 20 条注入和 10 条模型伪造的 JSON/工具/成功文本保留为非可信消息文本；Conversation 固定 `action_authority=none`。
- `verify-conversation-ui-boundary.py` 拒绝 Conversation 页面或客户端中出现 `ActionDispatcher`、`ToolGateway`、shell、Accessibility 等动作路径；模型回复只有显示/播报出口。
- `UnconfirmedActionRequestTest` 的 10 条助手伪造确认全部被拒绝；复杂任务只能由原始用户文本变为高风险、pending、dry-run 的可编辑 Phone Agent 请求，仍需可见确认。
- 本地控制短语不含“确认”到动作的升级路径；19 条支付、转账、OTP、密码和规避输入由永久拒绝语料保护。

因此聊天模型不具备手机动作或 root 权限。未来任何真实工具必须继续经过独立的计划、确认、能力令牌和适配器门禁。
