# OpenCode bounded runner

[简体中文](opencode-bounded-runner.zh-CN.md) · [workspace boundary](opencode-workspace-boundary.md) · [status](../STATUS.md)

Status date: 2026-08-10. `OC-004` provides a pure-Java, injected-adapter runner boundary. It accepts only a pending OpenCode task and its matching private workspace lease, then enforces a reviewed profile, agent label, 100–60,000 ms deadline, 1–4,096 token budget, 1–32 step budget, and 1–4,096-code-point redacted-output budget.

The adapter receives only the typed task, lease, bounded meter, and cancellation signal. Any token, step, or output overrun is a private structured budget failure; deadline and cancellation interrupt the injected worker. Results contain usage counts only and are never public-log safe.

This is not a real `oc run` integration: it launches no process, connects no model/network, reads/writes no task content, and exposes no command, path, credential, root, or UI authority. A later reviewed adapter must meter every model/tool event and add safe open/cleanup semantics before real execution can be claimed.

```sh
bash apps/android/xiaohei-android/test.sh
bash scripts/verify.sh
```
