# 版本化工具目录 v1

[English](versioned-tool-catalog.md) · [架构](architecture.zh-CN.md) · [任务计划](rules-first-task-plan.zh-CN.md) · [状态](../STATUS.md)

状态日期：2026-08-10。`TOOL-001` 在任务计划提案和后续工具网关之间建立一个经过审核、不可变的元数据边界。查找或校验 descriptor 不会调用 Android、模型、OpenCode、root 或网络服务。目录里存在一个工具，不等于获得了执行权限。

## 目录

| 工具标识 | 风险 | 输入 → 输出 | 回滚声明 | 受众 | 超时 |
|---|---|---|---|---|---|
| `android.open_settings@1` | `low` | `empty.v1` → `activity.v1` | `none` | Android 网关 | 5 秒 |
| `android.open_gallery@1` | `low` | `empty.v1` → `activity.v1` | `none` | Android 网关 | 5 秒 |
| `android.open_dialer@1` | `low` | `empty.v1` → `activity.v1` | `none` | Android 网关 | 5 秒 |
| `android.adjust_volume@1` | `reversible` | `volume.v1` → `volume.v1` | 通过同一版本工具恢复已捕获快照 | Android 网关 | 3 秒 |
| `android.observe@1` | `observe` | `observe.v1` → `observation.v1` | `none` | Android 网关 | 3 秒 |

六个被引用的 Schema 都是 `contracts/` 下的真实文件。无参数启动工具不接受额外字段；音量只允许有界、非零的相对变化；观察被限制为前台包名元数据，明确禁止屏幕文字、无障碍树、截图和原始媒体。所有输出均为 `public_log_safe=false`，调用方不能把它们复制到公开日志或 fixture。

## 回滚语义

回滚只是供后续网关执行策略使用的元数据，不是已经实现恢复的证据。`none` 明确表示没有声明自动回滚。`restore_snapshot` 要求未来执行器先捕获 before-state，并且回滚工具必须是同一版本目录中的受审标识。`TOOL-001` 既不捕获也不恢复。

目录会拒绝重复名称/版本、v1 以外版本、缺少风险/Schema/受众、非法超时，以及同目录中不存在的回滚目标。内建列表和查找表在类初始化后不可修改。

## 权限边界

规则优先规划器可以使用目录风险元数据拒绝未知工具或风险不匹配，但它生成的仍只是 dry-run 提案。`TOOL-002` 现已提供独立的 loopback/同 UID、一次性、短时授权核心，但仍未接线且不执行适配器；目录查找本身依然不授予任何权限。

OpenCode 与 root 工具被有意排除。它们需要独立受众和策略层级，不能靠名称继承 Android 权限。新增工具必须同时提供经过审核的 descriptor、真实输入/输出 Schema、公开合成正反 fixture、语义校验；不兼容行为必须升级版本。

## 复现证据

```sh
bash apps/android/xiaohei-android/test.sh
python3 scripts/verify-tool-catalog-contract.py
python3 scripts/verify-tool-catalog-boundary.py
bash scripts/verify.sh
```

公开 fixture 包含一份精确五工具目录，以及重复标识、未知版本、缺失 Schema、无法解析回滚四个拒绝样例。这些是静态合成记录，不代表已经完成真机动作或回滚验收。
