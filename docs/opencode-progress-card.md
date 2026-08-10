# OpenCode redacted progress card

[简体中文](opencode-progress-card.zh-CN.md) · [bounded runner](opencode-bounded-runner.md) · [status](../STATUS.md)

`OC-005` maps only typed lifecycle events to a visible read-only card: task kind, queued/running/succeeded/failed/cancelled state, and completed-step count bounded by the reviewed step limit. The card is intentionally public-log-safe.

It cannot accept or render a task instruction, task/request/plan ID, filesystem path, token budget or usage, credential, model response, terminal output, or arbitrary error text. A disconnected default is visible before a real runner is wired.

The current card does not prove a live OpenCode process exists. Future adapter wiring must emit only the reviewed enum events and must retain the runner's stop/cleanup and privacy boundaries.

```sh
bash apps/android/xiaohei-android/test.sh
python3 scripts/verify-opencode-progress-projection.py
```
