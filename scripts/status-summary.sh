#!/usr/bin/env bash
set -euo pipefail
root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ledger="$root/docs/execution-backlog.zh-CN.md"
for state in DONE VERIFY IN_PROGRESS READY BACKLOG BLOCKED HUMAN; do
  count="$(grep -cE "\| [A-Z]+-[0-9]{3} \| $state \|" "$ledger" || true)"
  printf '%-12s %s\n' "$state" "$count"
done
printf '\nNext executable tasks:\n'
grep -E "\| [A-Z]+-[0-9]{3} \| (IN_PROGRESS|READY) \|" "$ledger" | head -10
