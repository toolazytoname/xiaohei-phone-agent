# 规则优先 Task Plan v1

[English](rules-first-task-plan.md) · [未确认请求](unconfirmed-action-request.zh-CN.md) · [长期总纲](sovereign-mobile-agent-master-plan.zh-CN.md) · [状态](../STATUS.md)

状态日期：2026-08-10。`PLAN-001` 用版本化、有边界的 DAG 契约和纯本地校验器替换旧的“只看出现顺序”步骤检查。计划仍只是提案：校验不会调用模型、执行工具、触碰 Android 或授予确认状态。

## 契约

`task-plan.v1` 把 `plan_id` 绑定到原始 `request_id`，并强制：

- `dry_run=true`、`public_log_safe=false`；
- 步骤预算为 1–8，实际步骤不超过 8；
- 总超时为 1–60 秒；
- 每步工具版本为 1、风险与目录精确一致、字符串参数有界，并带唯一的 16–128 字符幂等键；
- 步骤 ID 唯一，依赖只能引用同一计划中的步骤；
- 依赖图无环。

前向依赖在结构上合法：列表顺序只是展示顺序，不是授权或执行顺序。真正的 visiting/visited 图遍历检测自环和多节点环。计划与步骤会防御性复制列表/映射，并只暴露不可修改视图。

## 规则优先边界

校验器使用当前五项已审核本地目录：打开设置、打开相册、打开拨号盘、调整音量、观察。`root.shell`、`android.tap`、`opencode.run` 等未知名称直接拒绝；已知工具的风险不匹配也拒绝。这不代表五项工具已经获准执行，只证明计划使用已知名称与声明风险。

源码不含工具执行、Android 服务、模型客户端或网络代码。`TaskPlanValidator` 未被主页、Conversation 或现有 Phone Agent 页面引用。`POLICY-002` 已提供独立新鲜确认基础；`PLAN-002` 仍须定义最小远端适配，能力令牌与工具网关仍是独立后续门禁。

## 验收

纯 Java 矩阵共 34 条：10 个合法 DAG（含前向引用分支与 8 步边界）、5 个未知工具、2 个空/超预算计划、5 个真实循环，以及 12 个非法结构/风险/版本/依赖/参数用例；每个结果固定零模型、零动作调用。

五个公开合成 JSON 夹具独立覆盖两个合法 DAG，以及未知工具、9 步和循环拒绝；Schema 与语义夹具校验不需要下载第三方 JSON Schema 库。
