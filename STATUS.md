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
| 通用产品基线 / Generic baseline | `DONE` in stated scope | 通用 Android 入口、能力探测、源码发布边界、卸载回滚、长期文档集成和会话边界契约 | `VOICE-001` + `CHAT-001` |
| DSP 与短命令 / DSP and short commands | `VERIFY` | OnePlus 8T DSP 声学 callback、离线命令路由和 12 个确定性动作已验证 | 完成 `REL-001`、`REL-002` 后再宣传低功耗 |
| 可见 Phone Agent / Visible Phone Agent | `DONE` in M5 scope | AOSP 十 App 10/10、全局停止、包绑定、内存视觉恢复 | `ROUTE-002`, then expand tools through `TOOL-*` |
| 开放对话 / Open conversation | `FOUNDATION` | 有界会话、独立配置、两个 Keystore 槽位和可停止 TTS 适配器已实现；独立设备凭据/TTS 播报验收待做 | `CHAT-003` after device-gate review |
| OpenCode 复杂任务 / OpenCode complex tasks | `FOUNDATION` | 手机上的 OpenCode TUI/Web 和独立模型 profile 已有集成证据 | `OC-002`; do not grant Android/root directly |
| 受控 root / Controlled root | `READY` | 设备已经 root；产品级 capability broker 尚未实现 | `ROOT-001`; generic `su -c` remains forbidden |
| 公开 Release / Public release | `VERIFY` | 可复现构建、SBOM、provenance 和扫描流水线已有候选证据 | 物理、真人和离线介质门禁完成后从最终 revision 重建 |

## 现在真正可用 / What works now

- 手动、系统助手、实验性 CPU KWS 或已验证 OnePlus DSP profile 可进入短命令会话；具体能力取决于设备层级。
- 固定短命令优先在本地解析，可执行打开 App/相册/设置等确定性低风险动作。
- 通知只读汇总、确认式消息草稿和可见 Phone Agent 已有明确的停止与隐私边界。
- 手机 AI Runtime 已把 OpenCode 与 Claude/Happy 的服务和模型配置拆开；这不等于小黑已经具备开放多轮聊天。
- 当前未完成系统 TTS 对话、开放聊天 ASR、多轮上下文、模型规划 Schema、通用工具网关和 root capability broker。

## 唯一执行队列 / Ordered queue

1. `CHAT-002` — 在独立设备验证 Conversation Keystore 保存/清除/恢复（实现已完成，待验）。
2. `VOICE-001` — 候选包上验证只读 TTS 探针；当前两台测试设备均无注册引擎，绝不自动下载。
3. `VOICE-002` + `CHAT-003` — 实现可停止 TTS 和有界流式模型客户端。
4. `CHAT-004` → `CHAT-005` — 先交付无动作权限的文字单轮，再做“说一句、答一句”。
5. `ROUTE-002` → `PLAN-001` → `POLICY-001` → `TOOL-001` — 再把聊天升级为可执行 Agent。
6. `OC-002` → `ROOT-001` — 最后接入受控 OpenCode 和 root 能力，不把原始 shell 暴露给模型。

完整的 102 个稳定任务、依赖和证据要求见[执行任务账本](docs/execution-backlog.zh-CN.md)。执行模型一次只能领取一个 `READY` 任务。

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

- `xiaohei-phone-agent` PR #11：release 构建自动强制 non-debuggable 门禁，检查通过并合并。
- `xiaohei-phone-agent` PR #12：修正 OnePlus 拔线静置预检说明，检查通过并合并。
- `BASE-004`：长期总纲、102 项双语任务账本、状态页和 README 导航已接入仓库门禁；本地 `scripts/verify.sh` 全部通过。
- `BASE-005`：`conversation-session.v1` 明确拒绝未知字段、越界 turn/token/timeout、非 memory-only 持久化和任何动作权限；2 个有效及 3 个拒绝夹具均通过 stdlib-only 校验。
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
