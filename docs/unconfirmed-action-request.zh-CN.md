# 未确认 ActionRequest 边界

[English](unconfirmed-action-request.md) · [低置信澄清](intent-routing-clarification.zh-CN.md) · [长期总纲](sovereign-mobile-agent-master-plan.zh-CN.md) · [状态](../STATUS.md)

状态日期：2026-08-10。`ROUTE-004` 为明确复杂任务创建版本化提案，但不授予执行权限。它是有类型的用户对话轮次到后续规划/策略的桥梁，不是模型回复直达 Android 动作的捷径。

## 创建流程

```text
有类型的会话消息
  ├─ ASSISTANT → 解释文字前直接拒绝
  └─ USER
       ├─ 歧义或不完整 → 回到 ROUTE-003 追问
       ├─ 聊天或确定性短命令 → 不升级
       └─ 明确复杂任务 → ActionRequest v1
                         risk=high
                         confirmation_state=pending
                         requires_confirmation=true
                         dry_run=true
                         → 停止
```

工厂接收 `MemoryConversationSession.Message`，而不是无类型字符串。长得像 JSON、工具调用、策略结果或成功回执的助手回复在路由前直接拒绝。用户正文即使包含 `"confirmation_state":"confirmed"`，也只是不可信、敏感的参数文字，不能改变本地固定字段。

## 固定权限边界

新提案固定使用 `target=local_service`、`action=plan_complex_task`。此时策略尚未审核目标或工具，因此风险保守定为 `high`；始终需要确认、状态始终为 `pending`、始终是 dry-run。不可变请求没有确认转换方法，也没有执行方法。

用户正文上限为 2048 字符，标记 `public_log_safe=false`，并在脱敏元数据中列为 `parameters.user_text`。可公开元数据只包含 schema/action/risk/state/dry-run 与文字长度，不含正文和请求 ID。

公开 `action-request.v1` Schema 现在强制 pending 请求必须等待确认且保持 dry-run；`not_required` 必须与 `requires_confirmation=false` 一致。四个公开合成夹具覆盖一个合法 pending 请求，以及 live-pending、免确认 pending、未知字段三种拒绝。

## 验收与剩余工作

确定性矩阵共 39 条：10 条复杂用户请求只创建 pending dry-run，10 条助手确认伪造被拒绝，10 条聊天/短命令不升级，5 条歧义回到追问，4 条非法元数据拒绝。全部路径零模型调用、零动作调用。

本基础目前有意不接入 `MainActivity` 或 `ConversationActivity`。`PLAN-001` 必须先定义有边界的规划输出，`POLICY-002` 必须定义绑定任务、目标、内容、有效期与设备状态的新鲜确认转换。在这些门禁完成前，请求只能停在 `pending`。
