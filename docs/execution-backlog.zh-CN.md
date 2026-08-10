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
| VOICE-003 | BACKLOG | VOICE-002 | `SPEAKING/WAITING_FOLLOWUP/INTERRUPTED` 状态机 | 非法跳转拒绝，停止后资源归零 |
| VOICE-004 | BACKLOG | VOICE-003 | 半双工音频生命周期：录音关闭后才播报 | audio_flinger 证明录音与 TTS 不重叠 |
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
| CHAT-004 | READY | CHAT-003 | 单轮文字聊天 UI，不带动作权限 | 真机回复显示；模型不可调用工具 |
| CHAT-005 | BACKLOG | CHAT-004,VOICE-002 | 单轮“说一句、答一句”闭环 | 真人提问 → 文本 → TTS；全局停止有效 |
| CHAT-006 | BACKLOG | CHAT-004 | 内存会话：turn、时间和 token 三重预算 | 超限摘要或清空；进程重启不恢复正文 |
| CHAT-007 | BACKLOG | CHAT-006 | 3–8 轮半双工追问窗口 | 指代、结束聊天、超时、切模型、锁屏测试 |
| CHAT-008 | BACKLOG | CHAT-006 | 系统 Prompt 与隐私上下文最小化 | Prompt 注入、工具伪造和敏感字段测试 |
| CHAT-009 | BACKLOG | CHAT-007 | “停止、重说、清空、继续聊”语音/按钮控制 | 每条控制零模型调用且幂等 |
| CHAT-010 | BACKLOG | CHAT-007 | 可选系统 TTS/中转 TTS 适配器选择 | 切换不改变 Conversation 模型或动作服务 |
| CHAT-011 | BACKLOG | CHAT-007 | 断网时本地固定 FAQ/0.6B 有界兜底 | 明确标记本地限制；不假装远端成功 |
| CHAT-012 | BACKLOG | CHAT-009 | 对话验收：20 问、5 中断、5 超时、5 隐私拒绝 | 精确候选版本零崩溃、零录音残留 |

## 6. 意图路由、计划与授权

| ID | 状态 | 依赖 | 交付物 | 完成证据 |
|---|---|---|---|---|
| ROUTE-001 | DONE | — | 固定短命令优先的本地 `CommandRouter` | 路由与歧义单元测试 |
| ROUTE-002 | BACKLOG | CHAT-004 | 三路分类：聊天 / 确定性命令 / 复杂任务 | 100 条脱敏文本，无动作误分类 |
| ROUTE-003 | BACKLOG | ROUTE-002 | 低置信度只追问，不猜测执行 | 相册/相机、聊天/命令歧义矩阵 |
| ROUTE-004 | BACKLOG | BASE-006,ROUTE-002 | 聊天中的“帮我做”转成 `ActionRequest`，不直接执行 | 模型回复文本无法伪造已确认状态 |
| PLAN-001 | BACKLOG | BASE-006 | 规则优先规划器与版本化 Plan Schema | 未知工具、超步数、循环依赖拒绝 |
| PLAN-002 | BACKLOG | PLAN-001 | 远端规划模型适配器，只接收最小快照 | Prompt/Token/截图范围审计 |
| PLAN-003 | BACKLOG | PLAN-001 | 每步执行后重新观察和验证后置条件 | 人为抢切 App 得到零越界动作 |
| PLAN-004 | BACKLOG | PLAN-003 | 一次有证据恢复和失败指纹去重 | 未改变条件不重试；改变条件最多一次 |
| POLICY-001 | DONE | — | L0–L4 风险、敏感页面拒绝和包允许列表基础 | `AgentPolicyTest` 与 M5 证据 |
| POLICY-002 | BACKLOG | ROUTE-004 | 绑定目标、内容、时间和任务 ID 的新鲜确认 | 目标变化、超时、锁屏后确认失效 |
| POLICY-003 | BACKLOG | POLICY-002 | Root/OpenCode/普通 Android 分层授权 | 较低层令牌不能调用较高层工具 |
| POLICY-004 | BACKLOG | POLICY-003 | 支付/OTP/密码/风控绕过永久拒绝回归库 | 模型诱导、UI 文案变体均为 DENY |

## 7. 工具网关与 Android 能力

| ID | 状态 | 依赖 | 交付物 | 完成证据 |
|---|---|---|---|---|
| TOOL-001 | BACKLOG | BASE-006 | 工具注册表：名称、版本、风险、输入/输出、回滚 | 重名、未知版本和缺失字段拒绝 |
| TOOL-002 | BACKLOG | TOOL-001,POLICY-002 | loopback Tool Gateway 与短时 capability token | 非本机、过期、重放和跨任务调用拒绝 |
| TOOL-003 | BACKLOG | TOOL-002 | 每工具超时、取消、幂等键和结构化错误 | 杀进程/断网/重复调用测试 |
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
| OC-002 | BACKLOG | TOOL-002 | 小黑到 OpenCode 的任务协议，不暴露通用 shell | 协议 fixture 与未知字段拒绝 |
| OC-003 | BACKLOG | OC-002 | 每任务独立工作区、允许目录和清理策略 | 路径逃逸、符号链接和跨任务读取拒绝 |
| OC-004 | BACKLOG | OC-003 | `oc run` 有界 runner：模型、agent、超时、token/步骤预算 | 卡死、退出、取消和输出上限测试 |
| OC-005 | BACKLOG | OC-004 | 流式进度映射到小黑任务卡 | 不把 Prompt、Token 或完整终端日志展示给用户 |
| OC-006 | BACKLOG | OC-004 | 用户停止同时终止 OpenCode 子进程和工具令牌 | 进程、端口、tmux、临时工作区无残留 |
| OC-007 | BACKLOG | OC-004 | 允许工具集合；禁止任意 root、密钥目录和破坏性 Git | 对抗任务无法越过策略 |
| OC-008 | BACKLOG | OC-007 | 三类真实任务：项目摘要、测试诊断、受控文件整理 | 每类 3 轮，结果/失败/回滚可复核 |
| OC-009 | BACKLOG | OC-008 | OpenCode Web 作为可选人工接管面 | 接管/归还任务所有权，不重复执行 |
| OC-010 | BACKLOG | OC-008 | 本地小模型仅用于分类/短任务的自动建议 | 只是建议，最终模型选择始终可见 |

## 9. Root Capability Broker

| ID | 状态 | 依赖 | 交付物 | 完成证据 |
|---|---|---|---|---|
| ROOT-001 | BACKLOG | BASE-006 | Root 威胁模型：攻击面、允许/拒绝能力、恢复责任 | 双语审查，不含私有模块或密钥 |
| ROOT-002 | BACKLOG | ROOT-001,POLICY-003 | 签名绑定 broker、固定动作 ID 和精确参数 Schema | 普通 App、shell、重放、畸形参数拒绝 |
| ROOT-003 | BACKLOG | ROOT-002 | 只读诊断：服务、端口、电池、音频、包与 profile 状态 | 输出脱敏、有长度上限、不含用户内容 |
| ROOT-004 | BACKLOG | ROOT-002 | 受控服务启停；PID/包/端口必须匹配 | 拒绝误杀，停止后无残留 |
| ROOT-005 | BACKLOG | ROOT-002 | 备份/恢复固定目录与加密工件 | 明文临时文件删除、完整恢复演练 |
| ROOT-006 | BACKLOG | ROOT-002 | 设备 profile 安装/卸载事务 | 预检、快照、失败回滚、重启后状态 |
| ROOT-007 | BACKLOG | ROOT-002 | 系统修改 dry-run、差异预览和新鲜确认 | 未确认、目标变化、过期全部拒绝 |
| ROOT-008 | BACKLOG | ROOT-003 | Root 日志和能力令牌撤销 | 全局停止后无法继续调用 |
| ROOT-009 | BACKLOG | ROOT-004 | 破坏性命令、宽泛路径、支付/凭据目录拒绝库 | 对抗输入 100% 拒绝，无实际破坏 |
| ROOT-010 | BACKLOG | ROOT-009 | 独立测试设备上的安装、升级、回滚和卸载 | 主 OnePlus 不作为首轮破坏性试验设备 |

## 10. 产品体验与人类控制

| ID | 状态 | 依赖 | 交付物 | 完成证据 |
|---|---|---|---|---|
| UX-001 | BACKLOG | CHAT-004 | 首页分开显示唤醒、ASR、Conversation、Phone Agent、OpenCode、Root | 切任一配置其他状态不变 |
| UX-002 | BACKLOG | CHAT-005 | 对话界面：听到的文字、回复、停止、清空和隐私状态 | 大字号、深色、中英文、旋转/重建 |
| UX-003 | BACKLOG | PLAN-001 | 任务卡：目标、计划、当前步骤、预算、结果和接管 | 不显示内部思维链或凭据 |
| UX-004 | BACKLOG | POLICY-002 | 风险确认页展示 App、目标、内容、权限和回滚 | 取消默认焦点，高风险不得倒计时确认 |
| UX-005 | BACKLOG | TOOL-003 | 全局停止统一入口：App、通知、语音和可选微件 | 1 秒内状态变为 stopping；最终资源归零 |
| UX-006 | BACKLOG | TOOL-012 | 脱敏历史逐条删除、全部清除和关闭保存 | 清除后私有目录无残留正文/图片 |
| UX-007 | BACKLOG | ROOT-003 | 权限中心：用途、最近使用、撤销和不支持原因 | 不诱导开启 root/无障碍/通知 |
| UX-008 | BACKLOG | UX-003 | 失败信息固定为原因、影响、一个恢复入口 | 同一失败不展示多个无效重试按钮 |

## 11. 可靠性、安全与发布

| ID | 状态 | 依赖 | 交付物 | 完成证据 |
|---|---|---|---|---|
| REL-001 | VERIFY | — | OnePlus DSP OFF/ARMED 物理拔线功耗 A/B | 三轮可比 A/B + 8–24h 完整 TSV；亮屏/供电/通话样本作废 |
| REL-002 | HUMAN | VOICE-002 | 提示音/TTS 真人听感 | 真人确认清晰、音量合适、不暗示已执行 |
| REL-003 | HUMAN | VOICE-006,VOICE-007 | 真人多人/噪声/距离 ASR/KWS | 预注册样本与失败，不用 TTS 代替真人 |
| REL-004 | BACKLOG | CHAT-012,TOOL-011 | 100 次混合聊天/命令/任务压力 | 0 Fatal/ANR、0 重复动作、0 录音残留 |
| REL-005 | BACKLOG | OC-008,ROOT-010 | 弱网、断网、进程杀死、重启和模型超时恢复 | 每种故障回到可解释可停止状态 |
| REL-006 | BACKLOG | UX-005 | 8–24h 服务组合待机回归 | 精确服务状态、功耗、wakelock、端口证据 |
| SEC-001 | BACKLOG | ROOT-001 | 更新威胁模型覆盖聊天 Prompt 注入、工具投毒和 root | 双语 STRIDE/滥用用例 |
| SEC-002 | BACKLOG | SEC-001 | 隐私数据流：音频、文本、截图、通知、位置、文件 | 每类保留期、存储、上传和删除规则 |
| SEC-003 | BACKLOG | TOOL-002 | 能力令牌、网络 TLS、重定向、loopback 与证书策略 | MITM/跨 UID/重放测试 |
| SEC-004 | BACKLOG | OC-007,ROOT-009 | 对抗测试集：Prompt 注入、路径逃逸、越权、隐私外传 | 所有高危尝试 fail closed |
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
