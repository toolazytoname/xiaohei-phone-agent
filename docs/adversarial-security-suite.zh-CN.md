# 对抗安全测试集

[English](adversarial-security-suite.md) · [威胁模型](threat-model.zh-CN.md) · [传输安全边界](transport-security-boundary.zh-CN.md)

`SEC-004` 组合现有 fail-closed 回归语料。它是本地边界测试集，不证明远端模型、OEM 或第三方 App 永远安全。

| 攻击类别 | 必须的本地结果 | 证据来源 |
|---|---|---|
| Prompt/工具注入 | Conversation 文本始终只是文本，助手伪造工具调用保持惰性，不产生动作权限。 | `ConversationPromptPolicyTest`：20 条注入、10 条伪造、5 类敏感形态，零动作调用。 |
| 工作区穿越 | 绝对、`.`/`..`、跨任务和符号链接路径在内容/进程使用前拒绝。 | `OpenCodeWorkspaceBoundaryTest`：7 条穿越、3 条符号链接、2 条跨任务拒绝。 |
| 越权/提权 | 跨 tier metadata、generic root/shell、敏感路径、网络和破坏性 Git/delete 都 fail closed。 | Authorization、OpenCode tool policy、root broker 和 root destructive denial 矩阵。 |
| 隐私外传 | Conversation 在创建模型/会话前拒绝通知、联系人、位置、私人媒体/文件和凭据；未知 root 形态拒绝。 | `ConversationPrivacyPolicyTest` 和 `RootDestructiveDenialPolicyTest`；零模型/动作/执行调用。 |

运行 `bash apps/android/xiaohei-android/test.sh` 与 `bash scripts/verify.sh`。下方聚合验证器会在任一组成边界或声明语料缺失时失败。真实 adapter、远端 Prompt、用户项目和物理设备仍不在此测试集范围。
