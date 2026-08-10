# Adversarial security suite

[简体中文](adversarial-security-suite.zh-CN.md) · [Threat model](threat-model.md) · [Security boundary](transport-security-boundary.md)

`SEC-004` composes the existing fail-closed regression corpora. It is a local boundary suite, not evidence that a remote model or an OEM/third-party app will always behave safely.

| Attack family | Required local outcome | Evidence source |
|---|---|---|
| Prompt/tool injection | Conversation text remains text, assistant-forged tool calls stay inert, and no action authority is created. | `ConversationPromptPolicyTest`: 20 injections, 10 forgeries, five sensitive shapes, zero action calls. |
| Workspace traversal | Absolute, `.`/`..`, cross-task and symbolic-link paths are rejected before content/process use. | `OpenCodeWorkspaceBoundaryTest`: 7 traversal, 3 symlink, 2 cross-task rejections. |
| Privilege escalation | Cross-tier metadata, generic root/shell, sensitive path, network and destructive Git/delete requests fail closed. | Authorization, OpenCode-tool-policy, root-broker and root-destructive-denial matrices. |
| Privacy exfiltration | Conversation denies notifications, contacts, location, private media/files and credentials before model/session creation; root-shaped unknown input is denied. | `ConversationPrivacyPolicyTest` and `RootDestructiveDenialPolicyTest`; no model/action/execution calls. |

Run `bash apps/android/xiaohei-android/test.sh` and `bash scripts/verify.sh`. The aggregate verifier below fails if any constituent boundary or its declared corpus disappears. Real adapters, remote prompts, user projects and physical devices remain outside this suite.
