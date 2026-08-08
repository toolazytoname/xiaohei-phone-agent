# Security policy / 安全策略

## Reporting / 报告漏洞

Do not open a public issue containing credentials, private endpoints, chat content, device identifiers, or a working exploit against a real user's device. Open a minimal redacted report first and use a private maintainer channel for sensitive reproduction details.

请勿在公开 Issue 中提交凭据、私有 Endpoint、聊天内容、设备标识，或针对真实用户设备的可执行利用。先提交最小脱敏报告，敏感复现细节通过维护者私密渠道提供。

## Action risk model / 动作风险模型

| Risk | Examples | Default policy |
|---|---|---|
| Low | Open an app, navigate to a public screen, report service state | May execute after a clear intent; still visible and cancellable |
| Medium | Read notification summaries, copy text, change a reversible preference | Require relevant permission, unlocked state where appropriate, and a visible preview |
| High | Send a message, delete data, purchase, install, grant permission, change security settings | Require target/content preview and explicit, fresh user confirmation |
| Forbidden by default | Bulk messaging, covert surveillance, bypassing platform security, credential extraction | Not implemented as normal product actions |

高风险确认必须绑定到具体目标、内容和短时有效的 request ID；不能用一次“全部同意”永久授权未来消息、支付、删除或安全设置操作。

## Required adapter properties / 动作适配器要求

- Least privilege and explicit permission checks.
- Dry-run or preview support for medium/high-risk actions.
- Idempotency or duplicate-action protection where practical.
- A bounded timeout, a clear failure state, and a rollback note.
- Redacted logs by default; no raw audio or chat body in public reports.
- No private protocol impersonation or evasion of app abuse controls.

## Unsupported public artifacts / 禁止公开的工件

OEM APKs/models, proprietary libraries, signing keys, keystores, model weights, provider credentials, private URLs, real device serials, unredacted UI dumps, and private conversation data must stay outside Git history and Releases.
