# 小黑自由语音聊天：执行者运行手册

[English](free-voice-chat-executor-runbook.md) · [可执行交付计划](free-voice-chat-delivery-plan.zh-CN.md) · [当前状态](../STATUS.md) · [任务账本](execution-backlog.zh-CN.md)

状态日期：2026-08-11。本页把自由语音聊天拆成**唯一顺序**的可执行任务。它是交给下一个执行模型的操作清单，不是完成声明。

## 0. 交付定义与不可逾越边界

本轮最低目标是 **L2 按键语音聊天**：用户点“说话”后，只录这一句；开放中文 ASR 转写后发给已配置的 Conversation 渠道；离线系统 TTS 播报回复；用户明确点“继续说”才开始下一句。文本和语音共用最多 6 轮、2048 token、5 分钟的内存上下文。

可选后续目标是 **L3 DSP 逐轮聊天**：已验证厂商 DSP 唤醒 → 精确“开始聊天”短意图 → 一次开放问句 → 回复/播报 → Android 录音释放且 DSP 重新 armed。它不等于自定义“小黑小黑”DSP 唤醒。

- 聊天回复只是文字/语音，绝不直接进入 `ActionDispatcher`、root、OpenCode 或工具执行器。
- 复杂手机任务只生成可编辑的 Phone Agent 草稿，并要求可见的新鲜确认；付款、转账、OTP、密码、规避风控永久拒绝。
- 不提交或输出 token、私有 URL、模型/APK、原始音频、转写、手机序列号、UI dump；不修改未跟踪的 `docs/articles/`。
- 不安装通用无模型 APK 覆盖 OnePlus 的含模型私有候选。
- 相同错误指纹不重试；只有代码、endpoint/model、网络或物理状态确实变化后，才允许一次新的最小验证。

## 1. 每个任务统一流程

1. 完整阅读本页、`STATUS.md`、交付计划、任务账本，以及任务所列源文件和验收页。
2. 执行 `git status -sb`；保留用户的 `docs/articles/` 和所有私有工件。
3. 先补最小自动测试或静态门禁，再做最小实现。不得借机重构相邻模块。
4. 运行任务命令、`bash scripts/verify.sh`、相关 Java 测试、`git diff --check`；Android 代码变更还要构建匹配的含模型私有候选。
5. 在 acceptance 文档分开记录自动、AOSP、OnePlus、HUMAN 证据。代码绿灯最多是 `VERIFY`，不能替代真实声学/电话/功耗。
6. 一个任务一个小 PR：写任务 ID、影响、验证、回滚、未完成物理门禁；两项必需 CI 和人工 review 后才合并。
7. 合并后更新 `STATUS.md`、账本和本页状态。遇到 HUMAN 门禁，记录一次所需动作后停止该项，不轮询 ADB、不反复截图。

## 2. 唯一执行队列

| 顺序 | 任务 | 初始状态 | 允许范围 | 验收与完成口径 | 遇到什么必须停下 |
|---:|---|---|---|---|---|
| 1 | `FVC-040A` 语音 turn 代码门禁 | READY | Conversation UI、语音状态机、`VoiceCommandSession`、对应单测/静态脚本 | 点说话先停止 TTS 再申请 input lease；partial 不发请求；一个 final 最多一次发送；空/取消/超时释放录音；Thinking/Speaking 不录音；生命周期失效迟到 callback。静态/JVM 通过后标 `VERIFY`。 | 语音路径能触发动作、或 lease 可能重叠。 |
| 2 | `FVC-050A` 半双工多轮代码门禁 | READY（依赖 1） | Conversation coordinator、控制短语策略、测试 | `WAITING_FOLLOWUP` 才显示“继续说”；每次仅开一轮；语音/文字共享 6 轮/2048/5 分钟；停止/重说/清空/继续/结束本地完成且 0 模型调用；切模型取消并清空。 | TTS 完成后自动开麦，或两条上下文并存。 |
| 3 | `FVC-060A` DSP 到 Conversation 的资源边界 | READY（依赖 1） | DSP 短意图入口、Conversation 启动 Intent、资源释放门禁 | 仅精确开始聊天短意图进入 Conversation；不宣称自定义 DSP；每次转写释放 recorder；TTS/网络不启动 CPU KWS；DSP/TTS 冲突确定性处理。 | 需要改厂商 DSP 资产、未知 root 命令或让 CPU 常驻录音。 |
| 4 | `FVC-070A` 中断与音频路由代码门禁 | READY（依赖 1） | TTS audio focus、Activity/电话/路由信号、测试 | pause/lock/global stop/route change 取消输入、HTTP、输出且不自动恢复；来电/媒体焦点策略和旧 route callback 有覆盖。 | 需要伪造通话记录、保存私人音频或重复拨号。 |
| 5 | `FVC-110A` 无模型 AOSP 回归 | READY（依赖 1–4） | 公开 source-only 构建与模拟器脚本 | 全新 AOSP：ASR 有/无状态诚实；mock 两轮；停止后零 Recorder/Fatal/ANR；卸载清理。不得写成 OnePlus/DSP 证据。 | 构建、安装或模拟器基线不稳定；记录首个失败指纹。 |
| 6 | `FVC-110B` OnePlus 私有候选构建预检 | READY（依赖 5） | 私有本地输入 APK、签名/资产哈希、构建脚本 | 不读取/打印秘密；确认来源、签名和模型资产后才构建。安装只在签名匹配时进行；记录脱敏哈希与版本。 | 缺失可信私有输入、签名不同，或会覆盖模型资产。 |
| 7 | `FVC-040B` OnePlus 一轮真机闭环 | HUMAN（依赖 6） | 手机 Conversation UI 与最小诊断 | 真人按说话并问 1 个预注册非隐私问题：final → **恰好一次**远端回复 → 离线 TTS → `WAITING_FOLLOWUP` → Recorder 归零。另做一次取消：不新增请求。只记 pass/fail/类别/耗时。 | token/profile/网络不变时同一失败重现；停止烧 token。 |
| 8 | `FVC-050B` OnePlus 两轮与边界 | HUMAN（依赖 7） | 手机 Conversation UI | 两轮预注册指代问题，精确 2 次调用、显示 `2/6`；停止播报、结束聊天、切模型均按规则清除。 | 回复误进动作链路或 TTS/ASR 重叠。 |
| 9 | `FVC-060B` OnePlus DSP L3 样本 | HUMAN（依赖 7） | 已验证厂商 DSP、状态页、最小诊断 | 息屏：厂商词唤醒 → 开始聊天 → 开放问句 → 一次回复/TTS → DSP re-arm，CPU KWS 始终 OFF、终态零 Recorder。一次样本只证明该路径。 | 电话、充电、亮屏或不完整样本；记无效，不记通过。 |
| 10 | `FVC-070B` 电话/焦点/路由物理矩阵 | HUMAN（依赖 7） | 真实电话、媒体/闹钟、蓝牙/耳机 | LISTENING/THINKING/SPEAKING 各一次来电；至少一种焦点丢失和每种可支持 route 切换；全部停止/释放/不自动恢复。 | 重复同一失败电话；先记录差异再修。 |
| 11 | `FVC-080` 中文 ASR 选型 | HUMAN（依赖 7） | 评测协议、候选 ASR profile、脱敏汇总脚本 | 30–50 条预注册样本、≥3 人/距离/环境；当前模型 vs 无热词 profile vs 一个候选，每条每候选一次；以语义成功、延迟、RSS/CPU、包增量选择 Conversation 模型。 | 没有预注册样本，或重复朗读刷对。 |
| 12 | `FVC-120` 真人产品验收 | HUMAN（依赖 8–11） | 评测表与真机 | 完成安静/房间/远距/轻噪声、三组两轮、五次停止、三次恢复、一次来电；确认自然中文/状态/无回声/终态无资源。 | 关键体验失败；记录失败，不能宣称日常可用。 |
| 13 | `FVC-130` 发布、回滚、文档 | READY（依赖 5；公开 release 依赖 12） | README、架构、兼容性、操作卡、证据、release 工件 | 双语严格区分 L2/L3/实验免触摸/聊天转动作；重建 source-only 工件、SBOM/provenance/扫描；AOSP 安装/升级/回退/卸载；review 后合并。预览只写已过门禁。 | 私有资产可能进入 Git，或无人可验证签名。 |

## 3. PR 拆分

任务 1–4 各一个 PR，任务 5 单独一个 PR。任务 6 若无代码改动只产生脱敏 acceptance 记录。每次真机验证仅在条件变化后写一个证据 PR。任务 13 最后独立 PR。禁止把语音交互改动和无关 OpenCode/root/微件重构混在一起。

## 4. 人类配合清单

| 任务 | 你需要做的事 | 预计 |
|---|---|---:|
| 7 | 连手机；按提示说两句预注册非隐私问题；点一次取消 | 约 3 分钟 |
| 8 | 说两组两轮问题；点一次停止播报/结束/切 profile | 约 5 分钟 |
| 9 | 拔线、息屏，用厂商唤醒词后说“开始聊天”和一个问题；不充电、不通话 | 约 3 分钟 |
| 10 | 按提示在三个时机各打一通电话；按提示连接/断开音频设备 | 约 10 分钟 |
| 11–12 | 按预注册表一次性完成语音与听感评测 | 约 30–45 分钟 |

没有明确进入这些任务时，执行者不得要求电话、重复朗读、持续插线或截图。

## 5. 给下一个执行模型的启动提示

> 阅读 `AGENTS.md`、本运行手册、`docs/free-voice-chat-delivery-plan.zh-CN.md`、`docs/execution-backlog.zh-CN.md` 和 `STATUS.md`。执行表中第一个依赖满足且为 READY 的任务，一次只做一个。保留未跟踪 `docs/articles/`。先测试后实现；运行 `bash scripts/verify.sh`、相关测试/构建和 `git diff --check`。自动验证不能替代 OnePlus/HUMAN 门禁。不得提交或输出 secrets、私有 URL、模型、APK、原始语音/转写、设备数据或 UI dump；不得以相同条件重复模型调用、ADB 试验、截图或电话。每个工作包完成后更新 acceptance、`STATUS.md` 和账本，开最小 PR；两项必需 CI 与 review 后才合并。遇到 HUMAN/物理门禁，准确记录所需动作并停止该项。
