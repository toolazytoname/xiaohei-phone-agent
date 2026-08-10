# GitHub Delivery Board

[简体中文](github-progress-board.zh-CN.md) · [Status](../STATUS.md) · [Execution ledger](execution-backlog.md)

This board lets the owner inspect Xiaohei delivery from desktop or mobile. `STATUS.md` and the bilingual execution ledgers remain authoritative. GitHub Project is a viewing and triage projection; it cannot turn an unverified claim into completed evidence.

## Fixed model

- Project: `Xiaohei Delivery / 小黑交付`
- Five board columns: `Inbox → Ready → In progress → Verify → Done`
- Fields: delivery state, task ID, dependency, evidence, gate, and next action
- Gates: `None / Human / Device / Power / Offline media`
- Labels: one `delivery`, five `state:*`, and four `gate:*`

Exact names, colors, ordering, and fields live in [`manifests/github-progress.v1.json`](../manifests/github-progress.v1.json). Do not add synonymous states in the web UI.

## Verified scope

On 2026-08-10, all ten repository labels were created through the repository API and read back. Check drift without mutation using:

```bash
bash scripts/sync-github-progress-labels.sh --check
```

Only explicit `--apply` creates or corrects these ten exact labels. It never deletes other labels.

## Current Project gate

The current GitHub CLI token has repository scopes but lacks `read:project` and `project`. The in-app Project page timed out and no external browser connection was available. The five-column Project has therefore not been created, and `PROGRESS-003` must remain `BLOCKED`; completed labels alone are not completion evidence.

Resume when the owner supplies one Project-authorized surface:

```bash
gh auth refresh -s read:project,project
```

After authorization, first check for an existing project with the same title. Then create the six manifest fields and five-column board, link `toolazytoname/xiaohei-phone-agent`, and confirm from mobile GitHub that `Ready`, `Verify`, and a human gate are visible. Expanding the local token is an owner-controlled authority change.

## Mapping rules

1. New issues enter `Inbox` and move to `Ready` only after dependencies are checked.
2. A workstream has at most one `In progress` item.
3. Code awaiting device, human, power, or release evidence moves to `Verify` with the matching `gate:*` label.
4. Move to `Done` only after ledger acceptance plus denial and rollback evidence.
5. If Project and repository disagree, correct Project from the ledger; never auto-edit repository evidence.
