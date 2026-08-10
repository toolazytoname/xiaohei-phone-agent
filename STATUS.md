# 小黑状态 / Xiaohei Status

更新于 / Updated: 2026-08-10  
当前版本 / Current version: `0.2.0-alpha.3` public release candidate; `alpha.4` remains a private pre-power candidate.

[中文总纲](docs/sovereign-mobile-agent-master-plan.zh-CN.md) · [English master plan](docs/sovereign-mobile-agent-master-plan.md) · [中文任务账本](docs/execution-backlog.zh-CN.md) · [English backlog](docs/execution-backlog.md) · [证据矩阵](docs/delivery-evidence-matrix.zh-CN.md)

本页是人类和执行模型共同使用的单页看板。它只回答三件事：现在真实能做什么、下一项做什么、什么仍然缺少证据。详细状态必须回到稳定任务 ID 和证据矩阵，不使用主观百分比。

This is the one-page dashboard shared by people and execution agents. It answers what works, what comes next, and which claims still lack evidence. Stable task IDs and the evidence matrix are authoritative; subjective percentages are not.

## 北极星 / North star

把一台可移动、可 root、可运行 OpenCode 的 Android 手机变成能对话、能规划、能真正操作手机、能解释、能停止和能回滚的个人智能体；模型提出计划，但不能绕过 Schema、策略、确认和工具网关。

Turn a mobile, rooted, OpenCode-capable Android device into a personal agent that can converse, plan, operate the phone, explain, stop, and roll back. Models may propose plans but may not bypass schemas, policy, confirmation, or the tool gateway.

## 当前真实状态 / Current reality

| 工作流 / Workstream | 状态 / State | 已有证据 / Evidence now | 下一任务 / Next task |
|---|---|---|---|
| 通用产品基线 / Generic baseline | `DONE` in stated scope | 通用 Android 入口、能力探测、源码发布边界、卸载回滚、长期文档集成和会话边界契约 | `VOICE-001` + `CHAT-006` |
| DSP 与短命令 / DSP and short commands | `VERIFY` | OnePlus 8T DSP 声学 callback、离线命令路由和 12 个确定性动作已验证 | 完成 `REL-001`、`REL-002` 后再宣传低功耗 |
| 可见 Phone Agent / Visible Phone Agent | `DONE` in M5 scope; `ROUTE-002` done | AOSP 十 App 10/10、全局停止、包绑定、内存视觉恢复；100 条惰性三路分类通过 | `ROUTE-003`, then unconfirmed request/planning/tool gates |
| 开放对话 / Open conversation | `VERIFY`; automated `CHAT-012` passed | 前述对话基础、确定性离线 FAQ、20/5/5/5 自动矩阵、五类请求前隐私拒绝及 AOSP 零录音残留已验证 | Human Mandarin playback/intelligibility/interruption remains; proceed `ROUTE-002` |
| OpenCode 复杂任务 / OpenCode complex tasks | `FOUNDATION` | 手机上的 OpenCode TUI/Web 和独立模型 profile 已有集成证据 | `OC-002`; do not grant Android/root directly |
| 受控 root / Controlled root | `READY` | 设备已经 root；产品级 capability broker 尚未实现 | `ROOT-001`; generic `su -c` remains forbidden |
| 公开 Release / Public release | `VERIFY` | 可复现构建、SBOM、provenance 和扫描流水线已有候选证据 | 物理、真人和离线介质门禁完成后从最终 revision 重建 |
| 交付治理 / Delivery governance | `DONE` through `PROGRESS-004`; Project UI blocked | PR 门禁、Issue 表单、10 标签及文本/JSON 只读摘要已完成；五列 Project 缺授权面 | Resume `PROGRESS-003` after Project authorization |

## 现在真正可用 / What works now

- 手动、系统助手、实验性 CPU KWS 或已验证 OnePlus DSP profile 可进入短命令会话；具体能力取决于设备层级。
- 固定短命令优先在本地解析，可执行打开 App/相册/设置等确定性低风险动作。
- 通知只读汇总、确认式消息草稿和可见 Phone Agent 已有明确的停止与隐私边界。
- 手机 AI Runtime 已把 OpenCode 与 Claude/Happy 的服务和模型配置拆开；小黑 Conversation 的多轮正文仍与这些 Agent 渠道隔离，且没有动作权限。
- 远端 Conversation 未成功时，五类/25 条精确帮助短语可由本地固定 FAQ 回答；未知、动作和注入文本不猜，公开 APK 不内置 0.6B 权重。
- 当前已能独立选择 Off/System/Relay TTS 配置，但尚未接通 Relay 播放或完成真人播报；开放聊天 ASR、模型规划 Schema、通用工具网关和 root capability broker 也未完成。

## 唯一执行队列 / Ordered queue

1. `ROUTE-003` → `ROUTE-004` → `PLAN-001` → `TOOL-001` — 从低置信追问和未确认请求开始，把聊天升级为受策略约束的可执行 Agent。
2. `OC-002` → `ROOT-001` — 接入受控 OpenCode 和 root 能力，不把原始 shell 暴露给模型。
3. `CHAT-002` — 在独立设备验证 Conversation Keystore 保存/清除/恢复（实现已完成，待验）。
4. `VOICE-001` → `CHAT-005` → `CHAT-012` — 注册可用 TTS 后完成真人语音闭环和最终听感/打断门禁。

完整的 102 个稳定任务、依赖和证据要求见[执行任务账本](docs/execution-backlog.zh-CN.md)。执行模型一次只能领取一个 `READY` 任务。

## 当前阻断 / Current blockers

| ID | 失败指纹 / Failure fingerprint | 已完成 / Completed | 恢复条件 / Resume condition |
|---|---|---|---|
| `PROGRESS-003` | CLI Token 缺 `read:project/project`；内置 Project 页超时；外部浏览器未连接 | 10 个标签已真实创建并回读，五列/六字段 manifest 和漂移检查已发布 | 所有者明确提供 Project 授权面后按[双语看板说明](docs/github-progress-board.zh-CN.md)创建并做手机可见验收 |

## 必须由人或物理环境完成 / Human and physical gates

| ID | 门禁 / Gate | 为什么不能由代码替代 / Why code cannot substitute |
|---|---|---|
| `REL-001` | OnePlus 8T 物理拔线 DSP OFF 静置基线 | ADB、亮屏、供电或通话会污染待机样本 |
| `REL-002` | 同条件 DSP ARMED A/B | 必须与合格 OFF 样本可比，不能用一次演示代替 |
| `REL-003` | 真人、多说话人、噪声和距离声学验收 | 合成语音不能证明真人可用性 |
| `RELEASE-004` | 签名恢复材料移到独立离线介质并真实恢复 | 同一台 Mac 上的加密副本不等于离线恢复 |
| `VOICE-001` / `CHAT-012` | 真人确认中文 TTS 可懂、打断自然且无录音残留 | 自动测试只能验证生命周期，不能判断体验 |

这些项目在完成前只能是 `VERIFY` 或 `HUMAN`，不得写成 `DONE`。

## 最近证据 / Recent evidence

- `ROUTE-002`：纯三路分类器精确通过 100 条合成非私人文本（40 确定性命令、35 聊天、25 复杂任务），结果固定零模型/动作调用；“回复消息是什么意思”不再被旧关键词误当草稿，“打开相册和相机”保持惰性，高风险复杂请求只分类不授权。分类器尚未接入主页，等待 ROUTE-003/004。
- `CHAT-012` 自动部分：20 问、5 中断、5 超时、5 隐私拒绝精确矩阵通过；通知/联系人/位置/私人媒体/凭据五类请求在会话与模型之前本地拒绝。全新 AOSP 页面显示双语零调用标签，保持 Conversation 前台，零 Fatal/ANR、零 Active Record Client。因 Relay 播放未实现、真人中文听感/自然打断未验，准确保持 `VERIFY`。
- `CHAT-011`：远端失败后只匹配五类/25 条精确中英文 FAQ，固定显示“本地固定 FAQ｜不是远端模型”；10 条未知/动作/注入和超长输入拒绝，兜底固定零新增模型/动作/上下文使用。全新 AOSP 未启用远端渠道时，命中项显示本地标签，动作型未知项只报渠道未启用且前台仍为 Conversation；零 Fatal/ANR，未使用网络模型、语音或实体机。
- `CHAT-010`：Off/System/Relay 使用独立配置键与 TTS Relay Keystore 槽，v3 无 Token 备份兼容 v2 恢复。全新 AOSP 正常入口先保存非空 Relay 配置，再只切 System，Conversation/Phone Agent 六字段逐字不变；零 TTS 初始化、服务启动、Fatal/ANR。Relay 播放与真人听感未据此虚报。
- `CHAT-009`：23 条精确中英文控制短语与停止/重说/清空/继续/结束按钮共用本地幂等状态机，五类结果固定 `modelCalls=0`。全新 AOSP 用户路径先产生一次 `CONTROL_BASE_OK`，再依次操作四个按钮；Mock 计数始终为 1，最终 UI 正文为空且无 Fatal/ANR。真人声学/TTS 未据此虚报。
- `CHAT-008`：Conversation 客户端只能序列化 `xiaohei-conversation-system.v1` 静态 Envelope；20 条 Prompt 注入、10 条助手工具伪造、5 类用户敏感形态及非法边界测试通过，静态门禁确认 Prompt 不采集动态设备隐私且回复路径没有动作解释器。零模型调用；不把 Prompt 测试冒充模型服从保证。
- `CHAT-007`：6 轮/2048 token/5 分钟半双工页面已接入内存会话；11 条确定性用例覆盖指代、结束、超时、切模型、锁屏、后台、busy、失败回滚和无效回复。全新 AOSP 用户路径两轮流式 mock 中，服务端验证精确历史后返回 `REFERENCE_CONTEXT_OK`；点击结束后 UI 正文消失，无 Fatal/ANR，未访问实体 OnePlus。
- `CHAT-006`：新增与 `conversation-session.v1` 六项上下限一致的纯 Java 内存会话；12 条用例覆盖 turn/token/总时长、并发、取消、失败回滚、新实例空状态与中文保守计数，静态门禁确认没有持久化、正文日志或进程全局正文集合。
- `CHAT-004`：新增双语零动作权限单轮聊天页面、显式状态/取消、竞态安全生命周期及静态动作边界门禁；全新 AOSP 模拟器用户路径收到一次流式 mock 回复 `XIAOHEI_UI_MOCK_OK`，无崩溃/ANR，验收后测试环境已清理，未访问实体 OnePlus。
- `CHAT-003`：Conversation 已切换为有界 SSE 优先客户端；11 条确定性用例覆盖成功/断流/429/重定向/超时/取消/上限/不安全配置和 IPv6 loopback，未调用真实模型。
- `PROGRESS-004`：只读状态摘要支持文本和 JSON；102 项计数闭合并显示当前/下一项、最近 PR、阻断、人工门禁和公开证据，未暴露未跟踪文章目录。
- `PROGRESS-003`：仓库侧 10 个状态/门禁标签已通过 API 创建并回读无漂移；Project 本体因无授权面保持 `BLOCKED`，未把标签完成冒充五列看板完成。
- `PROGRESS-002`：新增中英双语 GitHub Issue Form；10 个执行字段全部必填，公开空白 Issue 关闭，状态/安全入口和本地结构校验均已接入仓库门禁。
- `PROGRESS-001`：新增双语 PR 交付模板和 CI 门禁；自动正反例证明缺少状态页、缺少任一账本镜像、多个任务 ID 或未知 ID 均被拒绝。
- `BASE-005`：`conversation-session.v1` 明确拒绝未知字段、越界 turn/token/timeout、非 memory-only 持久化和任何动作权限；2 个有效及 3 个拒绝夹具均通过 stdlib-only 校验。
- `BASE-004`：长期总纲、102 项双语任务账本、状态页、双语 README 产品总览图和干净 AOSP 基础模式界面证据已接入仓库；本地 `scripts/verify.sh` 全部通过。
- `xiaohei-phone-agent` PR #12：修正 OnePlus 拔线静置预检说明，检查通过并合并。
- `xiaohei-phone-agent` PR #11：release 构建自动强制 non-debuggable 门禁，检查通过并合并。
- `VOICE-006`：真人中文评测协议、匿名分层样本模板和删除规则已双语发布；实际采样仍是 `REL-003` 人类门禁。
- `CHAT-001`：Conversation 配置与 Phone Agent 配置的交叉修改回归通过；AOSP 从公开主界面进入的配置页显示三条独立渠道。测试 APK 已卸载，OnePlus 上的含模型包未覆盖。
- 2026-08-10 检查时，黑客手机主题六个仓库没有待处理 PR；该事实不代表所有长期任务已经完成。
- `cpu-off-dsp-off-8h-r2` 因未进入合格拔线静置基线而失败，记录保留为失败；R3 只能在完整原始样本通过脚本判定后更新。

## 看板维护规则 / Dashboard update rules

1. 每个 PR 只推进一个稳定任务 ID，并在标题或正文写明该 ID。
2. 开工时把唯一任务设为 `IN_PROGRESS`；代码完成但真机/真人/功耗未验时设为 `VERIFY`。
3. 每次更新本页的“当前真实状态”“唯一执行队列”和“最近证据”，再更新任务账本与证据矩阵。
4. 不把进程存在、HTTP 200、单张截图、一次模型回答或一次语音命中当作端到端完成。
5. 相同失败指纹不连续重试；记录恢复条件，只有设备、配置、代码或外部状态改变后再试一次。
6. GitHub 上建议使用 Project 表格，字段固定为 `Task ID`、`State`、`Owner`、`Dependency`、`Evidence`、`Gate`、`Next`；本页仍是仓库内可离线读取的权威摘要。
