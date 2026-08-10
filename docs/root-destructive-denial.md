# Root destructive request denial

[简体中文](root-destructive-denial.zh-CN.md) · [root boundary](root-capability-boundary.md) · [status](../STATUS.md)

`ROOT-009` adds a defense-in-depth, fail-closed denial policy for raw root-shaped requests. Destructive commands, broad/system/traversal/wildcard paths, and secret/payment/evasion material receive separate denial decisions; all remaining input is also denied as unknown. It does not parse or execute shell text.

The fixed root broker accepts no free-form command or path parameters, so this policy grants no new surface. It is a permanent regression corpus against accidental future adapter expansion. No `su`, shell, filesystem, network, model, or device API exists in this policy.
