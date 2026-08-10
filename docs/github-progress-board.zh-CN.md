# GitHub 交付看板

[English](github-progress-board.md) · [状态页](../STATUS.md) · [执行账本](execution-backlog.zh-CN.md)

这套看板让项目所有者从电脑或手机查看小黑的真实进度。仓库内 `STATUS.md` 与双语执行账本仍是权威来源；GitHub Project 是便于查看和分流的投影，不能反向把无证据事项改成完成。

## 固定模型

- Project：`Xiaohei Delivery / 小黑交付`
- Board 五列：`Inbox → Ready → In progress → Verify → Done`
- 字段：交付状态、任务 ID、依赖、证据、门禁、下一步
- 门禁：`None / Human / Device / Power / Offline media`
- 标签：一个 `delivery`、五个 `state:*`、四个 `gate:*`

精确名称、颜色、顺序和字段记录在 [`manifests/github-progress.v1.json`](../manifests/github-progress.v1.json)。不要在网页中另造同义状态。

## 已验证范围

2026-08-10 已通过仓库 API 创建并回读全部 10 个标签；执行以下命令可只读检查漂移：

```bash
bash scripts/sync-github-progress-labels.sh --check
```

只有显式传入 `--apply` 才会创建或修正这 10 个精确名称的标签，不会删除其他标签。

## 当前 Project 门禁

当前 GitHub CLI Token 只有仓库权限，缺少 `read:project` 和 `project`；内置 GitHub Project 网页会话超时，外部浏览器也未连接。因此五列 Project 尚未创建，`PROGRESS-003` 必须保持 `BLOCKED`，不能因标签已完成而写成 `DONE`。

恢复条件是项目所有者提供任一 Project 授权通道：

```bash
gh auth refresh -s read:project,project
```

授权后先检查是否已存在同名 Project，避免重复创建；然后按 manifest 建立六个字段、五列 Board，关联 `toolazytoname/xiaohei-phone-agent`，最后从手机 GitHub 页面确认能看到 `Ready / Verify / Human gate`。授权动作会扩大本机 GitHub Token 能力，只能由所有者明确完成。

## 映射规则

1. 新 Issue 进入 `Inbox`；核对依赖后才进入 `Ready`。
2. 同一工作流最多一个 `In progress`。
3. 代码完成但设备、真人、功耗或发布证据未完成时进入 `Verify`，并附相应 `gate:*`。
4. 只有账本验收和拒绝/回滚证据齐全时进入 `Done`。
5. Project 与仓库冲突时，以账本为准并修正 Project；不得自动改写仓库证据。
