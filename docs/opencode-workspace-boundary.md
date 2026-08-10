# OpenCode Task Workspace Boundary

[简体中文](opencode-workspace-boundary.zh-CN.md) · [Task protocol](opencode-task-protocol.md) · [Status](../STATUS.md)

Status date: 2026-08-10. `OC-003` adds a task-private workspace lease for the future OpenCode runner. It creates only empty `input` and `output` directories below a trusted app-private root. It does not start OpenCode, read or write task content, create a network connection, or accept a user/model-supplied filesystem root.

## Isolation rule

```text
trusted private app root
  └── xiaohei-opencode-tasks/
        └── <task-id>/
              ├── input/     only lease INPUT relative paths
              └── output/    only lease OUTPUT relative paths
```

The runtime lease retains real paths privately. The public `opencode-workspace-lease.v1` metadata exposes only the task identity, the two allowed areas, `private_app_storage`, `path_exposure=none`, and `public_log_safe=false`; it never serializes an actual path.

Resolution accepts a non-empty relative path no longer than 512 characters. It rejects absolute paths, `.`/`..` segments, paths escaping the selected area, any existing symbolic-link component (including the root), duplicate task IDs, invalid task protocol state, and attempts to use another task's absolute or traversal path. No content is opened: a successful result is only a private candidate path for a later constrained runner.

## Evidence boundary

The Java test uses a newly created temporary root and deletes it afterward. It proves two independent leases, four safe paths, seven traversal/absolute rejections, three symbolic-link rejections, and two cross-task rejections while reporting zero content reads, writes, and process calls. It does not test a real OpenCode runner or solve later open-time TOCTOU concerns; `OC-004`/`OC-006` must use safe runner/open/cleanup semantics.

```sh
bash apps/android/xiaohei-android/test.sh
python3 scripts/verify-opencode-workspace-lease-contract.py
python3 scripts/verify-opencode-workspace-boundary.py
bash scripts/verify.sh
```
