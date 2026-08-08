# Contributing / 参与贡献

Xiaohei is evidence-driven and bilingual by default.

小黑默认要求真实证据和中英文文档。

1. Keep each change inside one product responsibility and state the user-visible outcome.
2. Add or update the English and Simplified Chinese document pair for public behavior.
3. Do not commit OEM binaries, model weights, credentials, private endpoints, device IDs, chats, or raw device evidence.
4. For device behavior, record the exact profile, action, expected result, observed result, rollback, and model-call/screenshot budget.
5. A service process, port, HTTP 200, or rendered screen is not sufficient proof of an end-to-end action.
6. Run `./scripts/verify.sh` before opening a pull request.

提交时请保持改动职责单一，说明用户可见结果；公开行为同步更新中英文；真机 case 记录设备 profile、动作、预期、实测、回滚和模型/截图预算。进程、端口、HTTP 200 或页面可渲染都不能单独证明端到端完成。
