# Evidence-changing recovery

`FailureFingerprint.RecoveryGate` is an in-memory fail-closed recovery policy for a single task attempt. It records the first bounded fingerprint, denies a matching failure or recovery request, and grants exactly one recovery only when a new fingerprint proves that the observed condition changed.

After the grant, every further recovery request is denied even if its evidence changes again. Invalid or missing evidence is also denied. The gate does not execute a tool, retry a request, persist a fingerprint, or expose the fingerprint publicly.

PLAN-004 remains `VERIFY`: a future planner/executor must wire this gate to real observations and show the user the single recovery path before any real adapter can rely on it.
