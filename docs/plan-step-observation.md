# Plan step observation guard

`PlanStepObservationGuard` is a fail-closed local state machine for multi-step plans. Before a step, it checks the observed foreground package against the expected package. After an adapter reports success, a newer observation must exactly match the planned postcondition before any next step can start.

An app switch, stale observation, invalid package metadata, or action failure transitions the guard to `HALTED`; it has no automatic retry or recovery. The observation carries only a foreground package name and monotonic sequence, not screen text, accessibility trees, screenshots, or media.

The guard has no Android observation or tool-adapter wiring yet. It therefore provides deterministic core coverage only; PLAN-003 remains `VERIFY` pending real before/after observation and an app-switch race on device.
