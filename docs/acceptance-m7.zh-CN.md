# M7 自定义唤醒词验收记录 — 部分完成

日期：2026-08-09  
设备：OnePlus 8T（`KB2000`），Android 14 / LineageOS 21  
候选版本：`0.2.0-alpha.1`（`versionCode=2`），debug 签名

## 真机已证明

- 官方 sherpa-onnx 1.13.4 中文 KWS 包已用 SHA-256 `1ee827227c1369b55e0aa5e35de93981ddcaa153238bfa21063260413278f07f` 固定；仓库不提交上游二进制。
- 一份 sherpa runtime 同时承载离线 ASR 与 KWS；构建删除未使用的重复 KWS checkpoint，产物约 64 MB。
- 唯一关键词为 `x iǎo h ēi x iǎo h ēi @小黑小黑`。两次独立普通话声学试验均在第一次呼叫得到 `keyword=小黑小黑`。
- KWS 先释放录音，再由可见 Assistant 会话取得麦克风；离线 ASR 结束后，CPU KWS 自动恢复为 `LISTENING`。
- 第二次试验进入命令路由，但 ASR 没有产出预期白名单命令，因此返回“未匹配命令”且没有动作。这是安全拒绝，不能记作语音到动作通过。
- 独立停止按钮把状态改为 `OFF`、销毁前台服务并释放活跃录音；DSP 始终为 `DETACHED`，CPU KWS 没有改变 DSP 状态。
- 监听时单点快照：总 PSS 约 149 MB、总 RSS 约 284 MB、thermal status 0。该数据不是续航结论。

## 产品边界与未完成门禁

CPU KWS 必须主动开启，有前台通知和麦克风指示，功耗较高且默认关闭。它是通用兜底，不等于低功耗 DSP。M7 仍需准确的唤醒到动作声学闭环、语料误唤醒/漏唤醒与拔线功耗测试、第二设备/profile 独立验收，以及自定义 DSP 关键词或收窄公开承诺。
