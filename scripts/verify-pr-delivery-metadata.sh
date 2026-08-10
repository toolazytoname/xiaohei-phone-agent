#!/usr/bin/env bash
set -euo pipefail

base_ref="${1:-${XIAOHEI_BASE_REF:-}}"
head_ref="${2:-${XIAOHEI_HEAD_REF:-HEAD}}"

if [[ -n "${XIAOHEI_CHANGED_FILES:-}" ]]; then
  changed_files="$XIAOHEI_CHANGED_FILES"
else
  if [[ -z "$base_ref" ]]; then
    printf 'FAIL PR delivery metadata needs a base ref\n' >&2
    exit 1
  fi
  changed_files="$(git diff --name-only "$base_ref...$head_ref")"
fi

require_changed() {
  local required="$1"
  if ! grep -Fxq "$required" <<<"$changed_files"; then
    printf 'FAIL PR must update %s\n' "$required" >&2
    exit 1
  fi
}

require_changed STATUS.md
require_changed docs/execution-backlog.md
require_changed docs/execution-backlog.zh-CN.md

task_declarations="$(grep -E '^Task ID / 任务 ID:' <<<"${XIAOHEI_PR_BODY:-}" || true)"
task_ids="$(grep -Eo '[A-Z]+-[0-9]{3}' <<<"$task_declarations" | sort -u || true)"
declaration_count=0
if [[ -n "$task_declarations" ]]; then
  declaration_count="$(wc -l <<<"$task_declarations" | tr -d ' ')"
fi
if [[ -z "$task_ids" ]]; then
  task_count=0
else
  task_count="$(wc -l <<<"$task_ids" | tr -d ' ')"
fi

if [[ "$declaration_count" -ne 1 || "$task_count" -ne 1 ]]; then
  printf 'FAIL PR body must contain exactly one Task ID / 任务 ID field with one stable ID; fields=%s ids=%s\n' \
    "$declaration_count" "$task_count" >&2
  exit 1
fi

if ! grep -Fq "| $task_ids |" docs/execution-backlog.md ||
   ! grep -Fq "| $task_ids |" docs/execution-backlog.zh-CN.md; then
  printf 'FAIL unknown or unsynchronized task ID: %s\n' "$task_ids" >&2
  exit 1
fi

printf 'PASS PR delivery metadata task=%s status=updated ledgers=synchronized\n' "$task_ids"
