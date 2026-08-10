# 小黑状态 / Xiaohei Status

更新于 / Updated: 2026-08-11
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
| 可见 Phone Agent / Visible Phone Agent | `DONE` in M5 scope; `TOOL-003` coordinator at `VERIFY` | AOSP 十 App 10/10、全局停止、包绑定、内存视觉恢复；路由、计划、确认、目录、loopback/same-UID 授权及仅测试有界执行协调器通过 | Real adapter kill/network/device gate remains; proceed with `OC-004` independently |
| 开放对话 / Open conversation | `VERIFY`; automated `CHAT-012` passed | 前述对话基础、确定性离线 FAQ、20/5/5/5 自动矩阵、五类请求前隐私拒绝及 AOSP 零录音残留已验证 | Human Mandarin playback/intelligibility/interruption remains; agent path proceeds with `OC-004` |
| OpenCode 复杂任务 / OpenCode complex tasks | `FOUNDATION`; `OC-002–008` done | 手机上的 OpenCode TUI/Web、独立模型 profile、三类型私有任务协议、任务私有路径隔离、有界 runner、脱敏进度卡、停止/清理、受限工具策略与九轮组合回归 | Real OpenCode adapter/device acceptance remains required |
| 受控 root / Controlled root | `FOUNDATION`; `ROOT-001–009` done | root 治理、固定只读 action 目录、内存 signer/一次性 broker、脱敏诊断投影、服务停止 dry-run 精确核验、加密备份、profile 事务、系统修改预览、脱敏审计/撤销与破坏性请求拒绝库已完成；真实 root adapter 尚未实现 | `ROOT-010`; independent-device lifecycle gate |
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

1. `OC-009` / `OC-010` — 可选 Web 接管与本地小模型建议，均不得绕过受控边界。
2. `ROOT-001` → `ROOT-010` — 建立独立 root capability broker；普通 Android/OpenCode 令牌不能升级权限。
3. `TOOL-003` real-adapter gate — 在具体适配器存在后验证真实进程 kill、断网与句柄归零；合成故障不能关闭该门禁。
4. `CHAT-002` — 在独立设备验证 Conversation Keystore 保存/清除/恢复（实现已完成，待验）。
5. `VOICE-001` → `CHAT-005` → `CHAT-012` — 注册可用 TTS 后完成真人语音闭环和最终听感/打断门禁。

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

- `UX-003`：只读任务卡可显示经批准目标摘要、审核步骤/当前步骤、时间/步骤预算、固定结果和人工接管；默认没有已审核任务，且不接受任务正文、路径、Token、模型回复或推理过程，更不产生执行接线。
- `UX-001`：主页的独立能力状态卡只显示唤醒、ASR、Conversation、Phone Agent、OpenCode 和 root 的非敏感元数据；它不启动运行时、不读取 Token，并明确 OpenCode/root 未接线。
- `OC-010`：模型渠道页现明确展示本地小模型建议边界；公开 APK 不内置生成式权重，未来小模型不得自动启用/切换、规划或调用工具，真实运行时和设备资源验收仍独立保留。
- `SEC-004`：注入、工作区穿越、越权和隐私外传的既有 fail-closed 语料现由双语聚合测试集持续校验；它证明本地边界而非真实 adapter、远端模型或设备行为。
- `SEC-003`：传输/能力边界现明确记录系统 TLS、拒绝重定向、loopback `NO_PROXY`、same-UID 与一次性重放拒绝；独立设备 MITM 与真实 listener 演练仍保持 `VERIFY`。
- `SEC-002`：新增双语、代码对应的数据流与保留规则，逐项说明音频、文本、截图、通知、位置、文件、轨迹和凭据的收集、内存、设备保留、外发和删除/撤权；静态验证检查六类及本地边界，远端、OEM 和独立设备证据没有被虚报。
- `SEC-001`：威胁模型现覆盖聊天/通知注入、工具/Schema 投毒、OpenCode 工作区穿越、root 提权以及破坏/外传请求；所有真实 adapter、独立设备和设备级停止验收仍是明确门禁。
- `ROOT-007`：系统修改只允许固定 profile 的 dry-run 预览，前后摘要必须不同且精确匹配、新鲜确认且未过期；只返回固定差异摘要，apply/root/shell/文件/设备调用均为零。
- `ROOT-005`：固定范围内存 AES-256-GCM 备份信封使用新 IV、32 字节密钥和 16 KiB 上限；错误密钥和篡改都拒绝，代码没有磁盘路径。真实加密持久化、清理和独立离线恢复尚未验证。
- `ROOT-008`：root broker 的每个决定只留下序号、固定 action、固定决定；`revokeAll()` 后该实例永久关闭，后续请求为 `deny_revoked`。审计不含 request ID、签名、参数、路径、命令、Token、时间或内容，且没有全局停止/真实设备接线。
- `ROOT-009`：破坏命令、宽泛/系统/穿越/通配路径与凭据、支付或规避材料均由本地 fail-closed 语料库拒绝；其余 root 形态输入同样未知拒绝，且不存在命令执行路径。
- `POLICY-004`：支付/转账、验证码/密码以及验证、风控或安全保护规避，在包名、可见文本和请求标签三个面都由本地永久拒绝语料库拦截；未匹配不授予权限，模型与执行调用为零。
- `ROOT-004`：服务生命周期目前只有 fail-closed 的停止 dry-run 预检；包名、进程名、PID、端口与新鲜确认均必须精确匹配。它不发 PID 信号、不启停服务、不打开端口，也没有 root 或设备执行路径。
- `ROOT-003`：六类只读诊断只投影固定类别、三态可用性和固定标签；服务 action 最多四项，电池/音频各一项。不存在命令、路径、PID、端口号、包名、profile/音频内容、用户文本、日志、Token 或原始设备输出；当前没有 root adapter 或设备执行。
- `ROOT-002`：内存 root broker 仅接受精确 signer、3 个固定只读 action ID、空参数与一次性 request ID；缺字段、签名错误、畸形参数和重放均拒绝。它不含 `su`、shell、Android、传输、root 进程或设备改动。
- `POLICY-003`：Android、OpenCode、root 三层 audience 已以纯内存策略隔离。Android 与 OpenCode 只能调用各自 gateway 元数据，所有跨层请求及未实现 broker 的 root 请求均拒绝；没有 token 签发、传输、UI、root 或执行路径。
- `ROOT-001`：新增 root 专属双语威胁模型和机器可检查目录，只记录三项未来固定只读 action ID，永久拒绝 generic `su`、任意命令/路径、凭据、支付/OTP/密码、破坏性 Git、系统分区/boot image 写入和网络外传；目录明确不授予 root 权限，恢复责任属于设备人类所有者。
- `OC-008`：项目摘要、测试诊断和受控整理各 3 轮临时工作区组合回归，逐轮经过协议、策略、有界注入适配器、结构化结果和私有清理；每类还拒绝 Git/网络意图。结果明确 `real_opencode=0`，因此不能表示真实项目、模型或 OpenCode 已运行。
- `OC-007`：受限 OpenCode 意图策略只允许项目摘要、测试诊断和受控文件整理；root、敏感路径、破坏性 Git/删除、网络、shell 转义和未知文本都 fail-closed。对抗矩阵覆盖三种允许类别及上述全部拒绝类别，模型/执行调用为 0；它不是命令执行器，未来适配器仍只能接收策略批准的类型操作。
- `OC-006`：单个已登记 OpenCode 任务的停止协调器会取消 worker、撤销活跃本地 Token、请求注入的 process/listener/tmux 句柄停止，并无跟随递归释放唯一私有租约。临时文件系统验证工作区内文件删除、链接外部目标不受影响、错误任务拒绝和重复停止幂等；真实 OS 子进程、端口、监听器与 tmux 仍需要独立设备验收。
- `OC-005`：OpenCode 进度卡只接受 6 个类型化生命周期事件，展示任务类别、状态和有界步骤计数；已接入 Agent 页面默认只读“未连接；未执行任务”卡。投影不含任务正文、ID、路径、计费量、凭据、模型/终端输出或任意错误文本；当前没有 live OpenCode runner，不能把卡片当作实际执行证据。
- `OC-004`：注入适配器的 runner 只接受合法 pending 任务与匹配私有租约，预算固定为 profile/agent、100–60,000 ms、1–4,096 token、1–32 步、1–4,096 代码点脱敏输出。矩阵通过 4 成功、3 预算越界、5 拒绝、1 超时与 2 取消，真实进程、网络和内容读写均为 0；它不是实际 `oc run`。
- `OC-003`：为合法 pending OpenCode 任务在可信 App 私有根目录下创建空的 `input`/`output` 租约，公开 lease 从不包含真实路径。真实临时文件系统验证 2 份租约、4 条安全路径、7 条绝对/穿越拒绝、3 条符号链接拒绝、2 条跨任务拒绝及重复任务拒绝，内容读取/写入和进程调用均为 0；它只是路径边界，不是 runner。
- `OC-002`：新增封闭的私有 `opencode-task.v1` 提案，只能由既有当前用户复杂任务生成，固定 pending、dry-run、需确认、`opencode_gateway` 与 `not_started`；仅允许项目总结、测试诊断、受控文件整理三类。3 个合法类型、10 条指令形攻击、6 条来源/类型/身份拒绝、2 合法/3 拒绝 fixture 及静态门禁均零模型/动作/执行；没有 shell、工作区、网络、root、凭据或 UI 路径。它不是 runner，`OC-003` 先做工作区边界。
- `TOOL-003`：目录上限内超时写入调用摘要，成功授权只产生一个私有、一次性执行许可；协调器最多提交一个注入式适配器，统一返回私有有界结果。25 组 Java 精确通过 5 成功、5 到期、5 取消、5 类型化失败与 5 拒绝/重放，9 个运行 worker 明确认收中断；3 个合法/2 个拒绝结果 fixture 及无 UI/平台接线门禁通过。全部适配器仍是 test double，模拟断网/进程退出不能冒充真实 kill 或资源关闭，因此准确保持 `VERIFY`。
- `TOOL-002`：一次成功确认的私有 receipt 只能换取一个纯内存、一次性、默认 `SecureRandom` 128-bit、1–30 秒 capability；它绑定 confirmation/task/request/plan/call/工具/版本/风险/受众/参数/幂等/时间/超时/隐私摘要。50 组 Java 与 7 个公开 fixture 文件拒绝 10 个非 loopback/跨 UID、确认复用/错范围、7 种调用漂移、3 种目录漂移、5 种非法/未来/陈旧/私有元数据调用、精确到期/时钟回退、重放/撤销及跨网关；活动/重放状态有界，模型/动作/执行调用均为 0。授权核心仍无监听器/UI；`TOOL-003` 只在测试中消费私有执行许可。
- `TOOL-001`：不可变五工具 v1 Android 目录为每项固定版本、风险、真实输入/输出 Schema、回滚声明、受众和 100–60000 ms 超时；六个封闭 Schema 禁止额外字段，观察明确不含正文/无障碍树/截图/原始媒体，所有输出均不可写入公开日志。Java 测试与五个公开 fixture 拒绝重名、未知版本、缺失 Schema、无法解析回滚、未知工具及风险错配；静态门禁确认零模型/平台/执行/持久化路径且无新增 UI 接线。目录不授予权限，授权核心已由 `TOOL-002` 补齐。
- `POLICY-002`：纯内存一次性确认门只接受前台、可交互、已解锁状态下的本机用户手势，并绑定 task/request/plan、加盐目标/内容摘要及 1–60 秒单调时窗；50 条矩阵覆盖 10 次精确单次放行、15 种范围变化、过期/时钟、设备、10 条助手伪造和重放/取消，全部零模型/动作调用。4 个公开夹具拒绝助手来源、非法时窗与原始内容；尚未接 UI/执行器。
- `PLAN-001`：版本化 `task-plan.v1` 强制绑定 request、dry-run、1–8 步与 1–60 秒边界；34 条 Java 矩阵和 5 个公开夹具覆盖前向/线性 DAG、8 步边界、5 个未知工具、9 步拒绝、风险/版本/幂等/依赖/参数失败及 5 个真实循环，全部零模型/动作调用。确认、目录、授权和仅测试执行协调器已存在；校验器仍未接 UI、模型或真实适配器。
- `ROUTE-004`：只有有类型的用户复杂任务才能生成不可变 schema-v1 请求，字段固定为 high-risk、requires-confirmation、pending、dry-run；39 条矩阵中 10 条助手确认伪造、10 条聊天/短命令、5 条歧义与 4 条非法边界均零请求或回到追问，固定零模型/动作调用。公开 Schema 额外拒绝 pending/live、pending/免确认与未知字段；尚未接入页面，等待 PLAN-002、UI 与工具门禁。
- `ROUTE-003`：纯本地澄清策略精确通过 50 条合成非私人文本：10 条多目标、10 条动作意图、10 条范围缺失全部变成聊天加未知命令的追问，20 条明确输入正常放行；固定零猜测动作、零模型/动作调用。静态门禁确认尚未接入主页，等待规划、策略与确认门禁。
- `ROUTE-002`：纯三路分类器精确通过 100 条合成非私人文本（40 确定性命令、35 聊天、25 复杂任务），结果固定零模型/动作调用；“回复消息是什么意思”不再被旧关键词误当草稿，“打开相册和相机”保持惰性，高风险复杂请求只分类不授权。分类器尚未接入主页，等待规划、策略与确认门禁。
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
- `UX-004`：Phone Agent 确认前固定展示 App、目标、内容、权限和停止/回滚；取消是默认安全路径，预览不申请权限、不签发能力、不调用模型/工具/root 或执行器。
- `UX-007`：主页只读权限中心展示麦克风、相机、通知、通知读取、无障碍和未接线 root 的用途、状态、最近使用可见性及用户撤销路径；系统页跳转不申请或改变权限。
- `VOICE-003`：系统 TTS 生命周期已区分播报、等待后续输入和中断；完成或中断均不自动恢复音频/麦克风。真实发声、焦点和真人打断仍需独立设备证据。
- `VOICE-004`：已加入输入/输出互斥所有权协调器，回归中无录音/TTS 重叠；尚未接入 Android 适配器，真实音频、焦点和资源释放仍为真机门禁。
- `UX-008`：任务卡失败现以类型化恢复卡展示原因、影响和单一恢复路径，不显示原始异常/路径/凭据；其他失败页面尚未统一接入，保持 VERIFY。
- `PLAN-002`：固定五字段最小规划信封仅含动作、干运行、步骤/时间预算与目录版本；字段级回归与静态门禁证明没有用户正文、界面数据、路径、图片、请求标识、凭据、传输或执行能力。尚未连接真实远端规划适配器，保持 VERIFY。
- `PLAN-003`：新增仅前台包名/单调序号的计划步骤观察守卫；每步成功后必须以新观察精确满足后置条件，抢切 App、过期/无效观察和动作失败都会终止后续步骤且不重试。尚未接 Android 观察器或工具适配器，保持 VERIFY。
- `PLAN-004`：失败指纹现包含内存恢复门：相同证据拒绝，只有证据变化才可精确放行一次恢复，之后持续拒绝；不持久化、不公开指纹，也不执行或重试工具。尚未接入真实规划器/执行器和用户恢复页面，保持 VERIFY。
- `UX-005`：新增八类别全局停止注册表；每个显式登记的资源所有者只收到一次停止请求，失败明确显示为未完全释放，停止后不允许再登记或重复停止。尚未接入所有入口或证明平台资源归零，保持 VERIFY。
- `UX-005` 接线：主页“全部停止”和已有状态通知 `global_stop` Intent 已通过该注册表停止主页持有的语音、DSP 与 CPU 唤醒；Phone Agent、Conversation、OpenCode、工具、root、语音/微件入口仍独立，未宣称全设备归零。
- `VOICE-005`：新增来电/闹钟/媒体/Activity 的统一信号中断策略，固定停止输入/输出、释放所有权且不自动恢复；主页暂停已通过它停止 ASR。尚未将真实来源和 TTS 全部接线，保持 VERIFY。
- `TOOL-012`：工具结果证据门要求适配器成功后出现更新且预期包名匹配的观察；旧快照、抢切包名、适配器失败和重复验证都拒绝。尚未接真实适配器/观察器，保持 VERIFY。
- `OC-009`：新增本地/Web 接管所有权状态机，只在已验证 Web 会话间转移控制权；接管/归还不启动、恢复或复制任务，重复、错误和终态请求均拒绝。尚未接真实 Web，保持 VERIFY。
- `VOICE-010`：新增代际化句子队列，首句立即可用，取消/替换会清空队列并忽略旧完成事件，避免过期文本进入播报。尚未接真实 TTS，保持 VERIFY。
