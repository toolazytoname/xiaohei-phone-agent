# FVC-020 真实文字模型与离线 TTS 最小闭环

[English](acceptance-fvc-020.md) · [自由语音聊天计划](free-voice-chat-delivery-plan.zh-CN.md) · [当前状态](../STATUS.md)

验收日期：2026-08-11。所有真实请求都使用无私人信息的固定短提示；未记录 endpoint、Token、模型 ID 或完整 UI XML。

## 一次失败、一次条件变化、两轮通过

1. 初始 profile 把 HTTPS 根路径当作 Conversation base。客户端因此请求了根路径下的 `/chat/completions`，返回 `PARSE_ERROR`。该指纹只发生一次，没有重试。
2. 只将 endpoint 改为相同主机的 `/v1` base；Token、模型、TTS 和其他渠道均未改变。
3. 第一轮固定提示要求一句中文回复，得到非空中文回复；离线系统 TTS 进入 `SPEAKING` 后到 `WAITING_FOLLOWUP`。
4. 第二轮固定引用追问得到“中文”，证明历史顺序为 `system,user,assistant,user`，页面进入两轮等待追问状态。
5. 点击可见“结束聊天并清空内存上下文”后，页面显示本地清空；没有新模型调用。

## 取消、离线和资源边界

- `bash apps/android/xiaohei-android/test.sh` 全部通过。`BoundedConversationTransportTest` 覆盖取消、超时、断流、429、重定向和响应限制；`PendingConversationCallTest` 覆盖同步/迟到 callback；`ConversationControlPolicyTest` 覆盖 23 个精确本地控制短语。为避免无意义额外付费请求，没有在真实中转上重复取消测试。
- 已有 `CHAT-011` 的 AOSP 证据覆盖 Conversation 关闭后的精确本地 FAQ 与未知输入 fail-closed；本次不切换已工作的私有配置去重复它。
- 两个成功 turn 后，系统 TTS 显示 `WAITING_FOLLOWUP`；结束聊天后没有活跃小黑录音客户端。CPU KWS 已保持 `OFF`。
- 测试中一次主页误触发了全局停止和一次“打开相册”测试入口；未删除/发送/修改用户数据。随后 DSP 已恢复为 `ACTIVE(handle=5)`、`captureRequested=false`。

## 结论

`FVC-020` 完成，L1 真实文字模型与离线播报可用。配置 UI 将在后续收敛为显式 `/v1` 提示/归一化，以防止根路径误配；这一改进不需要再重放真实模型请求。下一项 `FVC-030` 建立独立开放对话 ASR profile。
