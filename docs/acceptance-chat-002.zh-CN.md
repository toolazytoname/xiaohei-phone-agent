# CHAT-002 Android Keystore 与无 Token 备份验收

日期：2026-08-11

设备：OnePlus 8T（`KB2000`），Android 14

## 真机流程

1. 初始关闭且为空的 Conversation profile 被临时填入允许的 loopback 地址 `http://127.0.0.1`、占位模型和仅用于本次运行的随机测试 Token。未执行健康检查、模型请求或外网请求。
2. 可见保存结果显示 `Conversation Token 已安全配置`。App 私有检查只发现 Conversation 偏好文件中的 `token_iv` 与 `token_ciphertext`，没有明文 Token 字段。
3. 正常 Android 分享预览直接展示 `xiaohei-model-channels.v3` 的完整非敏感备份正文。它包含 ASR 模式、启用标志、地址/模型和 TTS 元数据，不包含 Token 值、IV、密文、alias 或 `token_` 字段；没有选择任何分享目标。
4. 正常恢复页通过多行 UI 接受有效的无敏感值 v3 备份，并可见提示三个 Token 槽均已清除，Conversation、TTS 与 Phone Agent 均关闭且未启动服务。之后不存在 `secure_channel*.xml` Token 偏好文件。
5. 已恢复测试前状态：本地 ASR（`asr_mode=0`）、Conversation 关闭且地址/模型为空、Phone Agent 关闭、系统 TTS 选中且设备默认引擎为离线 `ChineseTtsTflite`、CPU 唤醒词 OFF、DSP `ACTIVE(handle=4)`。

## 本项证明的内容

这是 Conversation Keystore 槽在独立实体设备上的“保存 → 无 Token 导出 → 恢复/清除”闭环。它证明 UI 流程不启动模型服务，也不会在可见备份中暴露测试 Token。

它不验证真实远端模型凭据、外部分享接收方、卸载/重装后的 Keystore 行为、其他 OEM 或任何模型请求；这些属于独立环境或 Release 门禁。

不提交测试 Token、截图、原始 UI XML、备份正文、凭据、APK、模型或私有设备内容。

[English](acceptance-chat-002.md)
