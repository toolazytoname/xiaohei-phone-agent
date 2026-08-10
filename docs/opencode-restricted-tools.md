# OpenCode restricted tools

[简体中文](opencode-restricted-tools.zh-CN.md) · [stop/cleanup](opencode-stop-cleanup.md) · [status](../STATUS.md)

`OC-007` is a fail-closed intent policy for future adapters. Only project summary, test diagnosis, and controlled file organization can be classified as allowed. Root, sensitive paths, destructive Git/delete actions, network transfer, shell chaining/escaping, and unknown text are denied before execution.

It is not a shell parser or command executor. Future real adapters must accept only policy-approved typed operations, not free-form command strings.
