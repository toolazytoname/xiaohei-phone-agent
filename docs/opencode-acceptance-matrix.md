# OpenCode controlled-task acceptance matrix

[简体中文](opencode-acceptance-matrix.zh-CN.md) · [restricted tools](opencode-restricted-tools.md) · [status](../STATUS.md)

`OC-008` runs nine synthetic acceptance rounds: three each for project summary, test diagnosis, and controlled organization. Every round constructs a typed pending task, evaluates the restricted policy, uses an injected bounded adapter, checks a structured success result, and releases a newly created private temporary lease. Each kind also rejects a Git/network adversarial intent.

This proves the composed local boundaries, not a real OpenCode task. It opens no user project, process, model connection, network, or real tool; `real_opencode=0` is an explicit result.
