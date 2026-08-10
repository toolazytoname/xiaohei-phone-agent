# Confirmation preview

Before a bounded Phone Agent proposal can be confirmed, the UI renders one fixed review card: App, target, planned content, required permission, and stop/rollback path. Cancel is the default safe decision; there is no timed or preselected acceptance.

The preview only describes the existing single-step Phone Agent path. It does not issue a `FreshConfirmationGate` receipt, request or expand Android permissions, call a model, start an accessibility task, invoke a tool, OpenCode, root, or an adapter. A local foreground user gesture remains required at the existing confirmation boundary.

For this path, rollback means using **Global stop Phone Agent**. A stopped or failed task does not retry or continue automatically.
