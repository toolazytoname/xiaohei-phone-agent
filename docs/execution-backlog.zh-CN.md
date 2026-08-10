# 小黑长期执行任务账本

[English](execution-backlog.md) · [产品总纲](sovereign-mobile-agent-master-plan.zh-CN.md) · [人类状态页](../STATUS.md)

本页是给执行模型使用的有序任务列表。它不是愿望清单：模型一次只能领取一个 `READY` 任务，并且必须满足依赖、验收和证据要求后才能标为 `DONE`。

## 1. 状态定义

| 状态 | 含义 |
|---|---|
| `DONE` | 真实用户路径、拒绝/回滚和要求的自动/设备门禁均有证据 |
| `VERIFY` | 实现完成，等待独立测试、真人、功耗或发布门禁 |
| `IN_PROGRESS` | 当前工作流唯一正在修改的任务 |
| `READY` | 依赖满足，可以领取 |
| `BACKLOG` | 依赖尚未满足 |
| `BLOCKED` | 已记录外部阻断和恢复条件，不能靠重复尝试推进 |
| `HUMAN` | 只能由真人、物理设备动作或独立介质完成 |

## 2. 较弱模型必须遵循的执行协议

1. 完整阅读 `AGENTS.md`、`STATUS.md`、产品总纲、本页和目标任务引用的源文件。
2. 执行 `git status -sb`；用户未跟踪的 `docs/articles/`、模型、APK、私钥和原始证据不属于任务范围。
3. 只把一个 `READY` 任务改为 `IN_PROGRESS`，不得顺手重构相邻模块。
4. 先写或补验收用例，再做最小实现；一个失败指纹只保留首次证据。
5. 模型调用、截图和 ADB 都要有预算。零调用检查通过前不能发付费请求；功耗采样窗口内禁止 ADB。
6. 代码通过不等于完成。没有真实用户路径时只能标为 `VERIFY`。
7. 发现需要 root、发送、删除、安装、授权、支付、隐私数据或外部写入时，停止并检查任务是否明确授权。
8. 完成时更新本页、`STATUS.md`、对应 acceptance 文档和必要的中英文页面；不得改写历史失败。
9. 运行 `bash scripts/verify.sh`、相关单元/构建/设备检查和 `git diff --check`。
10. PR 只包含该任务；列出根因、影响、验证、回滚和未完成门禁。CI/评审通过后才合并。

任务交接必须使用以下格式：

```text
TASK: <稳定 ID>
STATUS: READY | IN_PROGRESS | VERIFY | BLOCKED | DONE
SCOPE: 允许修改的文件/组件
DEPENDENCIES: 已满足的任务 ID
CHANGED: 实际改动
VALIDATION: 命令 + 结果 + 设备/模拟器
EVIDENCE: acceptance 文档/PR/精确工件哈希
ROLLBACK: 如何恢复
REMAINS: 尚未满足的门禁
NEXT: 唯一下一任务 ID
```

## 3. 基线、契约与进度

| ID | 状态 | 依赖 | 交付物 | 完成证据 |
|---|---|---|---|---|
| BASE-001 | DONE | — | M0–M7 证据矩阵与“代码不等于验收”规则 | `delivery-evidence-matrix*` |
| BASE-002 | DONE | — | 唤醒事件、动作请求、Agent 结果和诊断 Schema | `contracts/*.json` 校验通过 |
| BASE-003 | DONE | — | 通用 Android、OnePlus DSP、CPU KWS 能力分层 | 兼容矩阵与双设备生命周期证据 |
| BASE-004 | DONE | BASE-001 | 将本账本、`STATUS.md`、双语产品总览图与通用基础模式界面证据加入 README 导航 | 仓库校验找不到链接/缺失时失败 |
| BASE-005 | DONE | BASE-004 | 新增 `conversation-session.v1` Schema | 2 个有效边界、3 个拒绝 fixture 与跨字段校验通过 |
| BASE-006 | VERIFY | BASE-005 | 新增 `tool-call.v1`、`tool-result.v1` 与 capability token 契约 | 3 个 Schema 与正反例/重放/跨任务/过期拒绝夹具通过；待真实网关互操作 |
| BASE-007 | VERIFY | BASE-006 | 定义统一错误指纹、取消原因和恢复条件 | 单元回归证明相同条件拒绝重试、条件变化仅一次恢复；待接入真实网关 |

## 4. 语音与对话基础

| ID | 状态 | 依赖 | 交付物 | 完成证据 |
|---|---|---|---|---|
| VOICE-001 | VERIFY | BASE-005 | 真机探测已安装中文 Android TTS、音色、离线能力和初始化失败 | 只读探针已实现并构建；OnePlus/AOSP 初始 package 探测均无引擎，待候选包上独立设备 UI 闭环 |
| VOICE-002 | VERIFY | VOICE-001 | 系统 TTS 适配器与显式生命周期 | 10 个状态机转换与 APK 构建通过；无注册引擎设备上的真实播报/停止待验 |
| VOICE-003 | DONE | VOICE-002 | 纯 TTS 生命周期建模 `SPEAKING`、`WAITING_FOLLOWUP` 和 `INTERRUPTED` 合法转换；适配器标注完成/中断且不自动恢复；真实音频仍受门禁 | 非法跳转拒绝；真实资源归零仍需设备证据 |
| VOICE-004 | VERIFY | VOICE-003 | 同步输入/输出所有权协调器拒绝重叠，并在终态中断时归零；仍需 Android 适配器接线和真实音频证据 | 真机证明录音与 TTS 不重叠 |
| VOICE-005 | BACKLOG | VOICE-004 | 来电、闹钟、媒体和 Activity 中断处理 | 每种中断停止 TTS/ASR、状态可解释 |
| VOICE-006 | DONE | BASE-005 | 30–50 条真人开放中文问句语料协议，原始音频不入 Git | 双语分层模板、脱敏结果格式和重复失败限制已发布 |
| VOICE-007 | BACKLOG | VOICE-006 | 当前命令 ASR 与至少一个开放对话 ASR A/B | WER/语义成功率/延迟/内存，不能只报单句 |
| VOICE-008 | BACKLOG | VOICE-007 | 命令 ASR 和对话 ASR 独立 profile | 命令热词不会改写聊天文本 |
| VOICE-009 | BACKLOG | VOICE-008 | 流式 partial/final 转写与 UI | 乱序、重复 final、取消和超时测试 |
| VOICE-010 | BACKLOG | VOICE-004 | TTS 按句流式队列与首句低延迟 | 队列取消不继续播报过期文本 |
| VOICE-011 | BACKLOG | VOICE-010 | 播报中按钮打断；语音 barge-in 只作后续实验 | 打断 300ms 内停止；无自识别回声循环 |
| VOICE-012 | BACKLOG | VOICE-011 | 蓝牙/耳机/扬声器路由和音频焦点矩阵 | 连接切换、拔出、来电后恢复正确 |

## 5. 对话引擎

| ID | 状态 | 依赖 | 交付物 | 完成证据 |
|---|---|---|---|---|
| CHAT-001 | DONE | BASE-005 | 独立 Conversation provider 配置，不联动 Phone Agent/OpenCode/Claude/Happy | 纯配置隔离回归 + AOSP 用户入口可见验证通过；Token/调用留待 CHAT-002/003 |
| CHAT-002 | VERIFY | CHAT-001 | Android Keystore 凭据与备份规则；备份永不含 Token | 构建、配置隔离和备份回归通过；待独立设备 Keystore 保存/清除/恢复闭环 |
| CHAT-003 | DONE | CHAT-002 | 有界 HTTP/SSE 模型客户端、取消和重定向拒绝 | 11 条确定性传输用例 + 单元、APK 构建和仓库门禁通过；零模型调用 |
| CHAT-004 | DONE | CHAT-003 | 单轮文字聊天 UI，不带动作权限 | 生命周期/静态门禁及全新 AOSP 模拟器用户路径 SSE 回复通过；模型不可调用工具 |
| CHAT-005 | BACKLOG | CHAT-004,VOICE-002 | 单轮“说一句、答一句”闭环 | 真人提问 → 文本 → TTS；全局停止有效 |
| CHAT-006 | DONE | CHAT-004 | 内存会话：turn、时间和 token 三重预算 | 纯 Java 内核与契约上下限一致；超限/超时/取消释放正文引用，失败 turn 可回滚，无持久化或恢复路径 |
| CHAT-007 | DONE | CHAT-006 | 3–8 轮半双工追问窗口 | 6 轮 UI 接入有界上下文；11 条指代/结束/超时/切模型/锁屏/后台等确定性用例及全新 AOSP 流式 mock 用户路径通过 |
| CHAT-008 | DONE | CHAT-006 | 系统 Prompt 与隐私上下文最小化 | 版本化静态 Envelope；20 注入、10 工具伪造、5 敏感形态、非法边界及零动作/隐私静态门禁通过 |
| CHAT-009 | DONE | CHAT-007 | “停止、重说、清空、继续聊”识别文字/按钮控制 | 23 条精确短语与独立按钮共用幂等本地状态；单元/静态门禁及全新 AOSP 按钮路径中 Mock 始终只有 1 次基线调用 |
| CHAT-010 | DONE | CHAT-007 | 可选系统 TTS/中转 TTS 适配器选择 | 三态独立配置/Keystore/备份/静态门禁通过；AOSP 从 Relay 切 System 时 Conversation/Phone Agent 六字段逐字不变，零 TTS 初始化/服务启动 |
| CHAT-011 | DONE | CHAT-007 | 断网时本地固定 FAQ/0.6B 有界兜底 | 远端失败后只匹配五类/25 条精确 FAQ；双语非模型标签、10 条未知/动作/注入拒绝、零新增模型/动作/上下文使用及 AOSP 命中/拒绝路径通过 |
| CHAT-012 | VERIFY | CHAT-009 | 对话验收：20 问、5 中断、5 超时、5 隐私拒绝 | 自动精确候选 20/5/5/5 矩阵、静态零录音路径、签名 APK 和干净 AOSP 隐私 UI 均通过，零 Fatal/ANR/Active Record；仍待真人中文 TTS 可懂度/自然打断 |

## 6. 意图路由、计划与授权

| ID | 状态 | 依赖 | 交付物 | 完成证据 |
|---|---|---|---|---|
| ROUTE-001 | DONE | — | 固定短命令优先的本地 `CommandRouter` | 路由与歧义单元测试 |
| ROUTE-002 | DONE | CHAT-004 | 三路分类：聊天 / 确定性命令 / 复杂任务 | 惰性分类器精确通过 100 条：40 命令、35 聊天、25 复杂任务，零模型/动作调用；概念关键词与歧义动作保持非动作 |
| ROUTE-003 | DONE | ROUTE-002 | 低置信度只追问，不猜测执行 | 纯本地精确通过 50 条矩阵：10 目标、10 意图、10 范围追问及 20 明确对照；所有追问固定聊天/未知命令，零猜测动作、模型或动作调用，尚未接入主页 |
| ROUTE-004 | DONE | BASE-006,ROUTE-002 | 聊天中的“帮我做”转成 `ActionRequest`，不直接执行 | 有类型的用户复杂任务只生成不可变 schema-v1 高风险 pending dry-run；39 条矩阵拒绝助手确认伪造、聊天/短命令、歧义和非法元数据，零模型/动作调用，尚未接入页面 |
| PLAN-001 | DONE | BASE-006 | 规则优先规划器与版本化 Plan Schema | 版本化 dry-run DAG 与规则优先校验器；34 条 Java 矩阵和 5 个公开夹具覆盖合法前向/线性计划、精确 8 步/60 秒边界、未知工具、风险/版本/幂等/依赖/参数失败及 5 个真实循环，零模型/动作调用，尚未接线 |
| PLAN-002 | VERIFY | PLAN-001 | 固定五字段规划信封仅允许动作、干运行、受边界约束的预算和目录版本；不携带用户正文/界面数据/路径/图片/请求标识/凭据，且无传输或执行能力；仍需真实适配器证据 | 真实适配器仅传输该信封的范围审计 |
| PLAN-003 | VERIFY | PLAN-001 | 仅元数据的 fail-closed 守卫要求每个成功步骤后有精确且更新的前台包名后置观察；抢切 App、过期/无效观察和动作失败都会终止所有后续步骤且不重试；仍需真实 Android 观察器/适配器和设备竞态证据 | 人为抢切 App 得到零越界动作 |
| PLAN-004 | VERIFY | PLAN-003 | 内存 fail-closed 恢复门记录有界失败证据，拒绝未变化证据；仅在证据变化后放行一次恢复，之后拒绝所有恢复；仍需真实规划器/执行器接线和面向用户的恢复证据 | 未改变条件不重试；改变条件最多一次 |
| POLICY-001 | DONE | — | L0–L4 风险、敏感页面拒绝和包允许列表基础 | `AgentPolicyTest` 与 M5 证据 |
| POLICY-002 | DONE | ROUTE-004 | 绑定目标、内容、时间和任务 ID 的新鲜确认 | 纯内存一次性本机用户确认绑定 task/request/plan、加盐目标/内容摘要、前台解锁设备状态和 1–60 秒单调时窗；精确 50 条矩阵拒绝变化、过期、锁屏/后台、助手伪造、取消与重放，零模型/动作调用，尚未接线 |
| POLICY-003 | DONE | POLICY-002 | Android/OpenCode/root 内存 audience 分层边界，只允许 Android/OpenCode 同层元数据，拒绝所有跨层请求；broker 出现前 root 继续拒绝 | 低层 token 不能调用高层工具 |
| POLICY-004 | DONE | POLICY-003 | 覆盖包名、可见文本和请求标签的本地支付/OTP/密码/规避风控永久拒绝语料库；19-case 回归且零模型/动作调用 | 模型诱导、UI 文案变体均为 DENY |

## 7. 工具网关与 Android 能力

| ID | 状态 | 依赖 | 交付物 | 完成证据 |
|---|---|---|---|---|
| TOOL-001 | DONE | BASE-006 | 不可变五工具 v1 目录：精确风险、六个封闭输入/输出 Schema、回滚声明、受众与超时 | Java/fixture/静态门禁拒绝重名、未知版本、缺失 Schema 和无法解析回滚，零执行接线 |
| TOOL-002 | DONE | TOOL-001,POLICY-002 | 纯 loopback/同 UID 授权核心把一次内部确认 receipt 换成一次 1–30 秒、绑定完整调用的 capability | 50 组 Java 与 7 个 fixture 文件拒绝远端、跨 UID、确认复用/错范围、非法/陈旧调用漂移、过期、时钟回退、重放与跨网关，适配器执行为 0 |
| TOOL-003 | VERIFY | TOOL-002 | 纯协调器把目录上限内超时绑定到一次性权限，最多调用一个注入式适配器；超时/取消中断 worker，范围/幂等重放拒绝，结果私有且有界 | 25 组 Java 与 5 个结果 fixture 已通过；真实进程 kill、断网及适配器资源关闭仍缺证据，不能用合成故障冒充 |
| TOOL-004 | DONE | — | 公开 Intent、Settings、相册、相机、浏览器、地图、拨号基础 | M2 确定性动作证据 |
| TOOL-005 | DONE | — | 当前通知汇总和确认式消息草稿 | M4 隐私/撤权/零发送证据 |
| TOOL-006 | DONE | — | 包绑定语义无障碍与一次内存视觉恢复 | M5 10 App 和恢复证据 |
| TOOL-007 | BACKLOG | TOOL-001 | MediaStore 文件查询/复制/移动/回滚适配器 | 测试目录，不触碰私人相册原件 |
| TOOL-008 | BACKLOG | TOOL-001 | 日历/提醒公开 Provider 适配器 | 预览、确认、创建、删除回滚测试账号 |
| TOOL-009 | BACKLOG | TOOL-001 | 媒体播放、暂停、音量与路由适配器 | 来电/耳机/锁屏矩阵 |
| TOOL-010 | BACKLOG | TOOL-003 | 文本输入、滚动、返回、选择的语义工具 | 不点击通用“允许/确认/下一步” |
| TOOL-011 | BACKLOG | TOOL-010 | 15+ 常用 App 版本化适配器和失败说明 | 每 App 正常/拒绝/页面变化用例 |
| TOOL-012 | BACKLOG | TOOL-003 | 工具执行前后快照与可逆操作回滚目录 | 结果不能仅依赖退出码或模型描述 |

## 8. OpenCode 移动执行器

| ID | 状态 | 依赖 | 交付物 | 完成证据 |
|---|---|---|---|---|
| OC-001 | DONE | — | 手机 OpenCode TUI/Web、独立模型 profile 与真实中转调用 | Android AI Stack 验收记录 |
| OC-002 | DONE | TOOL-002 | 封闭的私有 pending dry-run 协议只接受来自既有当前用户复杂任务的三类审核任务 | Java/fixture/静态门禁拒绝伪造来源、live 状态、未知权限形字段和通用 shell/工作区/网络/root/凭据表面，零执行 |
| OC-003 | DONE | OC-002 | 在可信 App 私有根目录下为每任务分配 input/output 租约 | 真实临时文件系统矩阵拒绝绝对/穿越、符号链接、重复任务和跨任务路径，零内容/进程执行 |
| OC-004 | DONE | OC-003 | 注入适配器的有界 runner 强制审核 profile/agent 及超时、token、步骤、脱敏输出上限 | 矩阵覆盖成功、预算越界、拒绝、超时、取消，真实进程/网络/内容路径均为零 |
| OC-005 | DONE | OC-004 | 类型化生命周期投影已接线到可见只读卡片，只展示类别、状态和有界已完成步骤数 | 不暴露 Prompt、Token、路径或完整终端输出 |
| OC-006 | DONE | OC-004 | 已登记任务停止会取消 worker、撤销活跃本地网关 Token、停止注入的 process/listener/tmux 句柄，并无跟随递归释放唯一私有租约 | 真实 OS 句柄/设备验收仍未关闭 |
| OC-007 | DONE | OC-004 | Fail-closed 类型意图策略只允许项目摘要、测试诊断和受控整理；拒绝 root、敏感路径、破坏性 Git/删除、网络、shell 转义和未知文本，零执行 | 对抗任务无法越过策略 |
| OC-008 | DONE | OC-007 | 9 轮合成临时工作区回归：项目摘要、测试诊断和受控整理各 3 轮；每轮通过协议/策略/有界适配器/结果/清理，每类拒绝 Git/网络意图 | 真实 OpenCode 尚未验证 |
| OC-009 | BACKLOG | OC-008 | OpenCode Web 作为可选人工接管面 | 接管/归还任务所有权，不重复执行 |
| OC-010 | DONE | OC-008 | 可见双语本地小模型说明限制未来用途为无权限建议；不得自动启用/切模型/规划/工具/root，且不内置权重 | 只是建议，最终模型选择始终可见 |

## 9. Root Capability Broker

| ID | 状态 | 依赖 | 交付物 | 完成证据 |
|---|---|---|---|---|
| ROOT-001 | DONE | BASE-006 | Root 威胁模型、封闭允许/拒绝目录与设备人类所有者恢复责任 | 双语审查；目录不授予能力，通用 `su -c` 继续拒绝 |
| ROOT-002 | DONE | ROOT-001,POLICY-003 | 内存固定动作 root broker 核心绑定精确 signer、3 个只读 action ID、空参数 schema 和一次性 request ID；没有 root 适配器或设备执行 | 普通 App、shell、重放、畸形参数拒绝 |
| ROOT-003 | DONE | ROOT-002 | 只读诊断：服务、端口、电池、音频、包与 profile 状态；仅固定类别/状态/标签投影，无 adapter 或设备执行 | 输出脱敏、有长度上限、不含用户内容 |
| ROOT-004 | DONE | ROOT-002 | 服务停止 dry-run 预检：包/进程/PID/端口精确匹配、新鲜确认；无进程信号或设备执行 | 拒绝误杀，停止后无残留 |
| ROOT-005 | DONE | ROOT-002 | 固定范围的内存 AES-256-GCM 备份/恢复信封，含新 IV、错误密钥/篡改拒绝且无磁盘路径；真实持久化/清理/离线恢复仍受门禁约束 | 明文临时文件删除、完整恢复演练 |
| ROOT-006 | DONE | ROOT-002 | 固定 profile 内存事务账本：预检、快照摘要、rollback 漂移拒绝和重启后核验状态；没有 installer/设备执行 | 预检、快照、失败回滚、重启后状态 |
| ROOT-007 | DONE | ROOT-002 | 固定 profile dry-run/差异预览要求不同精确摘要、新鲜确认且未过期；结果固定且没有系统 apply | 未确认、目标变化、过期全部拒绝 |
| ROOT-008 | DONE | ROOT-003 | 内存脱敏 root 决定审计与永久 broker 实例撤销；无 Token 持久化且撤销后请求持续拒绝；全局停止接线仍待完成 | 全局停止后无法继续调用 |
| ROOT-009 | DONE | ROOT-004 | fail-closed 的破坏命令、宽泛/系统路径和凭据/支付/规避拒绝语料库；未匹配 root 形态输入也保持拒绝且无命令执行 | 对抗输入 100% 拒绝，无实际破坏 |
| ROOT-010 | BACKLOG | ROOT-009 | 独立测试设备上的安装、升级、回滚和卸载 | 主 OnePlus 不作为首轮破坏性试验设备 |

## 10. 产品体验与人类控制

| ID | 状态 | 依赖 | 交付物 | 完成证据 |
|---|---|---|---|---|
| UX-001 | DONE | CHAT-004 | 首页展示彼此独立的唤醒/ASR/Conversation/Phone Agent/OpenCode/root 元数据状态；不启动运行时、不暴露凭据，并明确未接线路径 | 切任一配置其他状态不变 |
| UX-002 | BACKLOG | CHAT-005 | 对话界面：听到的文字、回复、停止、清空和隐私状态 | 大字号、深色、中英文、旋转/重建 |
| UX-003 | DONE | PLAN-001 | 只读任务卡投影已批准目标摘要、审核步骤/当前步骤、预算、固定结果和接管；不含任务正文/路径/Token/模型输出/推理，也无执行接线 | 不显示内部思维链或凭据 |
| UX-004 | DONE | POLICY-002 | 固定双语确认预览展示 App、目标、内容、权限和停止/回滚；取消仍为默认，且不授予或执行任何能力 | 不倒计时确认，也不预选同意 |
| UX-005 | BACKLOG | TOOL-003 | 全局停止统一入口：App、通知、语音和可选微件 | 1 秒内状态变为 stopping；最终资源归零 |
| UX-006 | BACKLOG | TOOL-012 | 脱敏历史逐条删除、全部清除和关闭保存 | 清除后私有目录无残留正文/图片 |
| UX-007 | DONE | ROOT-003 | 只读双语权限中心展示用途、受限状态/最近使用可见性、由用户掌控的 Android 撤销路径和 root 不支持/未接线原因；不授予任何能力 | 不诱导开启 root/无障碍/通知 |
| UX-008 | VERIFY | UX-003 | 类型化公开失败投影提供原因、影响和一个恢复路径，不接收原始错误文本；任务卡失败已接入，其余界面仍需采用 | 同一失败不展示多个无效重试按钮 |

## 11. 可靠性、安全与发布

| ID | 状态 | 依赖 | 交付物 | 完成证据 |
|---|---|---|---|---|
| REL-001 | VERIFY | — | OnePlus DSP OFF/ARMED 物理拔线功耗 A/B | 三轮可比 A/B + 8–24h 完整 TSV；亮屏/供电/通话样本作废 |
| REL-002 | HUMAN | VOICE-002 | 提示音/TTS 真人听感 | 真人确认清晰、音量合适、不暗示已执行 |
| REL-003 | HUMAN | VOICE-006,VOICE-007 | 真人多人/噪声/距离 ASR/KWS | 预注册样本与失败，不用 TTS 代替真人 |
| REL-004 | BACKLOG | CHAT-012,TOOL-011 | 100 次混合聊天/命令/任务压力 | 0 Fatal/ANR、0 重复动作、0 录音残留 |
| REL-005 | BACKLOG | OC-008,ROOT-010 | 弱网、断网、进程杀死、重启和模型超时恢复 | 每种故障回到可解释可停止状态 |
| REL-006 | BACKLOG | UX-005 | 8–24h 服务组合待机回归 | 精确服务状态、功耗、wakelock、端口证据 |
| SEC-001 | DONE | ROOT-001 | 双语威胁模型覆盖聊天/通知注入、工具/Schema 投毒、OpenCode 穿越、root 提权与破坏/外传路径；设备级验收仍明确保留 | 双语 STRIDE/滥用用例 |
| SEC-002 | DONE | SEC-001 | 双语代码对应数据流/保留表覆盖音频、文本、截图、通知、位置、文件、轨迹与凭据；静态验证器检查六类和当前本地边界，第三方/设备证据仍明确保留 | 每类保留期、存储、上传和删除规则 |
| SEC-003 | VERIFY | TOOL-002 | 双语传输/能力边界将 TLS、重定向、loopback、UID 与重放保护对应到现有自动证据；独立设备 MITM/真实 listener 演练仍必需 | MITM/跨 UID/重放测试 |
| SEC-004 | DONE | OC-007,ROOT-009 | 双语聚合对抗测试集保留注入、穿越、越权和隐私外传语料；自动本地边界通过，真实 adapter/设备仍明确保留 | 所有高危尝试 fail closed |
| RELEASE-001 | DONE | — | debug/release 分离、仓库外签名、SBOM/provenance/ClamAV 基线 | M6 与 PR #11 证据 |
| RELEASE-002 | BACKLOG | CHAT-012,REL-004 | 版本递增的无模型通用 release 候选 | 两次字节一致、严格静态扫描 |
| RELEASE-003 | BACKLOG | RELEASE-002 | 精确候选的全新安装、升级/降级、回滚、卸载 | 干净 AOSP 与 OnePlus 非破坏性路径 |
| RELEASE-004 | HUMAN | RELEASE-002 | 加密签名恢复包移到独立离线介质 | 所有者控制介质上的解密/哈希演练 |
| RELEASE-005 | BACKLOG | RELEASE-003,REL-001,RELEASE-004 | 双语 Release Notes、兼容矩阵、已知限制和下载校验 | bundle 验证通过，公开批准后上传 |

## 12. 进度维护任务

| ID | 状态 | 依赖 | 交付物 | 完成证据 |
|---|---|---|---|---|
| PROGRESS-001 | DONE | BASE-004 | 每个 PR 更新 `STATUS.md` 的 Now/Next/Blocked/Evidence | 双语模板 + CI 正反例拒绝缺失状态、账本镜像、多任务或未知 ID |
| PROGRESS-002 | DONE | PROGRESS-001 | GitHub Issue 模板包含任务 ID、依赖、验收、回滚和人类门禁 | 10 个必填字段、禁用公开空白 Issue、双入口链接及结构/YAML 校验通过 |
| PROGRESS-003 | BLOCKED | PROGRESS-002 | GitHub Project 五列和标签映射 | 10 标签已创建回读、manifest/漂移检查通过；Project 授权面不可用，恢复条件见看板文档 |
| PROGRESS-004 | DONE | PROGRESS-001 | 生成只读状态摘要的脚本，不自动修改证据 | 文本/JSON 显示 102 项闭合计数、当前/下一项、PR、阻断、人工门禁和公开证据 |

## 13. 当前推荐顺序

```text
BASE-004
  → BASE-005
  → VOICE-001 → VOICE-002 → VOICE-003 → VOICE-004
  → CHAT-001 → CHAT-002 → CHAT-003 → CHAT-004 → CHAT-005
  → ROUTE-002 → ROUTE-003 → BASE-006 → ROUTE-004
  → PLAN-001 → POLICY-002 → TOOL-001 → TOOL-002
  → CHAT-006 → CHAT-007 → CHAT-009 → CHAT-012
  → OC-002 ... OC-008
  → ROOT-001 ... ROOT-010
  → REL/SEC/RELEASE 最终门禁
```

物理功耗 `REL-001`、真人听感 `REL-002`、真人声学 `REL-003` 和离线介质 `RELEASE-004` 是并行的人类/设备门禁；执行模型不能把它们改成 `DONE`。
