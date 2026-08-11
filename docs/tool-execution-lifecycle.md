# Bounded Tool Execution Lifecycle

[简体中文](tool-execution-lifecycle.zh-CN.md) · [Authorization](loopback-tool-gateway.md) · [Catalog](versioned-tool-catalog.md) · [Status](../STATUS.md)

Status date: 2026-08-11. `TOOL-003` adds a coordinator for exactly one already-authorized adapter call. It enforces the reviewed per-tool deadline, cancellation, one-use authorization/idempotency, bounded structured output, and private error reduction. The app now has one visible, cancel-first local caller: after an unlocked foreground tap it can query only the item count in `Pictures/XiaoheiTest/`. It cannot read image content, copy, rename, delete, select arbitrary paths, run a shell/root command, open a network client, or accept a remote caller.

## Authority and lifecycle

```text
local confirmation
       │ consume once
       ▼
loopback/same-UID gateway ── full-call digest + idempotency + timeout
       │ authorize once
       ▼
private execution permit
       │ consume once and match the unchanged call
       ▼
execution coordinator ── submit at most one injected adapter
       ├─ success / bounded output
       ├─ typed adapter failure / rollback required
       ├─ user, global-stop, disconnect, or caller cancellation
       └─ deadline / interrupt worker and close executor
```

A public JSON call or token is not executable authority. The coordinator accepts only the private runtime permit produced by a successful gateway authorization. Taking that permit clears it; reuse is reported as `authorization_replay`. Any call mutation after authorization fails as `scope_changed` before the adapter runs.

The call's `timeout_ms` is part of the capability digest and must be between 100 ms and the immutable catalog timeout for that tool. The coordinator uses that exact value in a monotonic `Future.get` deadline. Completion and cancellation share one synchronized terminal point: whichever wins determines the outcome, and a late cancel returns false. Timeout, running cancellation, or caller interruption requests `Future.cancel(true)`; every terminal path detaches the cancellation signal, calls `shutdownNow`, and waits at most 100 ms for the test-owned worker.

## Structured outcomes

`tool-result.v1` contains only stable enums and bounded data:

| Status | Typical error codes | Adapter calls |
|---|---|---:|
| `success` | `none` | 1 |
| `denied` | authorization denied/replay/expired, scope changed | 0 |
| `cancelled` | user, global stop, client disconnected, caller interrupted | 0–1 |
| `timeout` | deadline exceeded | 1 |
| `failed` | adapter missing, invalid output, network unavailable, process nonzero, adapter failure | 0–1 |
| `rollback_required` | rollback required | 1 |

Output is an immutable string map with at most 32 fields, names limited to lower snake case, and values limited to 1024 characters. Results use monotonic start/finish/duration fields and always set `public_log_safe=false`. Raw exception messages, stacks, model text, screenshots, accessibility trees, and private Android data are outside this contract.

## Acceptance evidence

The deterministic Java matrix has 25 groups:

- five catalog tools complete once through injected success adapters;
- five deadlines interrupt a blocking worker;
- five cancellation paths cover pre-start user stop, running user stop, global stop, client disconnect, and caller-thread interruption;
- five structured failure paths cover synthetic network unavailable, synthetic nonzero process exit, untyped adapter failure, rollback required, and invalid output;
- five authorization cases cover missing/issued-only authority, execution-permit replay, changed scope, and idempotency replay.

Nine running workers acknowledge interruption and every result reports zero or one adapter call. Five public result fixtures independently test the closed outcome contract.

```sh
bash apps/android/xiaohei-android/test.sh
python3 scripts/verify-tool-gateway-contract.py
python3 scripts/verify-tool-execution-contract.py
python3 scripts/verify-tool-execution-boundary.py
bash scripts/verify.sh
```

## Visible local query path

The main page links to **Controlled tool acceptance: read-only query Xiaohei test gallery**. The separate screen first shows the exact scope and defaults to no execution. A foreground local confirmation creates a fresh confirmation receipt, exchanges it for one loopback/same-UID capability, consumes that capability into the authorized Android bridge, and runs only `android.media_test_collection` with `{ "operation": "query" }`. The result exposes only the collection count; a cancellation, authorization failure, or adapter failure is displayed without automatic retry. Closing the page cancels the pending confirmation and the running signal.

This is deliberately a disposable acceptance path, not a gallery feature. Copy/move/rollback and the calendar test adapter remain unavailable from the UI until separately reviewed with their own visible confirmations and reversible device evidence.

## Evidence boundary and remaining work

All adapters in `TOOL-003` tests are injected in-memory test doubles. `network_unavailable` and `process_exit_nonzero` prove typed error mapping only; they do **not** prove that a real socket, child process, Android component, microphone, or root resource was opened or killed. The worker-interruption tests prove the coordinator requests interruption and closes its executor; a future real adapter must also close its own process/network/platform handles cooperatively and receive separate kill/disconnect/device acceptance.

Android tools, OpenCode tools, and root tools keep separate reviewed catalogs and audiences. This local screen is not a socket listener or a trusted external transport proof. Later tasks must add device evidence for the read-only query, explicit permission/rollback evidence for each mutating test adapter, and separate trusted peer evidence without widening this coordinator into a generic shell or approval-clicking path.
