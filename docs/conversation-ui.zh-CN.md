# 单轮 Conversation 界面

状态日期：2026-08-10。本页记录历史上的 `CHAT-004` 单轮实现与有边界的 AOSP 模拟器验收。当前页面已在 `CHAT-007` 下扩展为[有边界的半双工 Conversation](conversation-half-duplex.zh-CN.md)，但仍然是零动作权限；本页不证明语音识别、TTS 听感、实体机凭据存储或模型驱动动作。

## 产品边界

这个页面只接收一条文字并显示一条模型回复。它有意不连接 Android 动作、通知、文件、root、OpenCode、Phone Agent 或任何其他工具入口。

用户始终可以看到：

- 中英双语的“零动作权限”提示；
- 空闲、请求中、取消中、已完成或失败的明确状态；
- 独立的发送与取消按钮；
- 可选择复制、但只能显示的回复文本。

该 Activity 不导出。以下六个稳定的无障碍描述可用于不截图的确定性检查：

`conversation-authority-notice`、`conversation-state`、`conversation-input`、`conversation-send`、`conversation-cancel`、`conversation-output`。

## 生命周期与竞态安全

同一时刻只能有一个请求拥有页面。发起新请求会取消旧请求。带 generation 的 pending-call 对象覆盖传输绑定前后取消、同步完成、迟到回调和 Activity 销毁；旧回调不能覆盖新回复。

请求期间输入框和发送按钮禁用。开始取消后，取消按钮立即禁用。关闭页面会取消仍在工作的请求。

## 可复现检查

仓库门禁做两类相互独立的检查：

1. 纯 Java 生命周期测试覆盖正常完成、绑定前后取消、回调先于绑定的同步完成，以及旧请求取消清理。
2. 静态边界检查确认六个 UI 标识、`exported=false`、`action_authority=none`，并确认 Conversation 页面/客户端没有连接 Android 动作分发、服务、广播、Phone Agent 或工具网关。

运行：

```bash
bash apps/android/xiaohei-android/test.sh
python3 scripts/verify-conversation-ui-boundary.py
bash scripts/verify.sh
```

## AOSP 模拟器验收记录

2026-08-10，当前 debug 候选包被全新安装到专用 Android 14 ARM64 模拟器。验收从正常 onboarding 和模型配置页面启用 Conversation，并通过显式 ADB reverse 把它指向宿主机 loopback mock；Phone Agent 保持关闭。

验收人员从公开主页进入“**小黑聊天：单轮文字（无动作权限）**”，输入 `hello` 后点击发送。Mock 服务收到一个流式请求并返回有界 SSE 回复。无需截图的 UI 树检查观察到：

- 状态：`回复完成（仅显示）`；
- 输出：`XIAOHEI_UI_MOCK_OK`；
- 完成后发送可用、取消禁用；
- 没有 fatal exception 或应用 ANR；
- 发布源码中的 UI/客户端没有动作权限路径。

随后已移除 reverse 映射、测试 APK、应用数据和 mock 进程。本次验收没有访问实体 OnePlus 手机。

## 仍然开放的任务

- `CHAT-002`：实体机 Keystore 保存、清除和恢复验收；
- `CHAT-005`：真人语音 → 文本 → 模型 → 可懂且可打断的 TTS；
- `CHAT-006`：有预算的内存多轮会话；
- `ROUTE-*`、`PLAN-*`、`TOOL-*`：分别受治理的路由、规划和动作权限。

模型文本即使长得像 JSON 或工具调用，也绝不能因此变成动作。未来只能通过版本化 Schema、策略、确认和工具网关链路增加动作权限。
