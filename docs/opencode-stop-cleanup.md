# OpenCode stop and cleanup boundary

[简体中文](opencode-stop-cleanup.zh-CN.md) · [bounded runner](opencode-bounded-runner.md) · [status](../STATUS.md)

`OC-006` provides one registered-task global-stop boundary. It cancels the runner signal, revokes all active local gateway tokens, asks each injected process/listener/tmux resource handle to stop, and recursively deletes only the registered private lease tree without following symlinks.

The matrix covers three successful injected resources, wrong-task denial, repeat-stop idempotence, recursive workspace deletion, and a symlink whose external target remains intact. Failure to stop a handle or release the private lease is surfaced as `CLEANUP_FAILED`.

No real OpenCode process, port, listener or tmux session is started by this code. Actual adapters must implement their own termination and pass independent device acceptance before those real resources can be claimed clean.

```sh
bash apps/android/xiaohei-android/test.sh
python3 scripts/verify-opencode-stop-cleanup.py
```
