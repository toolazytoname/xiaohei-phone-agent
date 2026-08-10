#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

required_files=$'STATUS.md\ndocs/execution-backlog.md\ndocs/execution-backlog.zh-CN.md'

XIAOHEI_CHANGED_FILES="$required_files" \
XIAOHEI_PR_TITLE='PROGRESS-001: enforce delivery metadata' \
XIAOHEI_PR_BODY=$'Task ID / 任务 ID: `PROGRESS-001`\nDependency: BASE-004' \
  bash scripts/verify-pr-delivery-metadata.sh >/dev/null

expect_failure() {
  local description="$1"
  shift
  if "$@" >/dev/null 2>&1; then
    printf 'FAIL expected rejection: %s\n' "$description" >&2
    exit 1
  fi
}

expect_failure 'missing STATUS.md' \
  env XIAOHEI_CHANGED_FILES=$'docs/execution-backlog.md\ndocs/execution-backlog.zh-CN.md' \
      XIAOHEI_PR_TITLE='PROGRESS-001: missing dashboard' \
      XIAOHEI_PR_BODY='Task ID / 任务 ID: `PROGRESS-001`' \
      bash scripts/verify-pr-delivery-metadata.sh

expect_failure 'missing bilingual ledger mirror' \
  env XIAOHEI_CHANGED_FILES=$'STATUS.md\ndocs/execution-backlog.md' \
      XIAOHEI_PR_TITLE='PROGRESS-001: missing mirror' \
      XIAOHEI_PR_BODY='Task ID / 任务 ID: `PROGRESS-001`' \
      bash scripts/verify-pr-delivery-metadata.sh

expect_failure 'multiple task IDs' \
  env XIAOHEI_CHANGED_FILES="$required_files" \
      XIAOHEI_PR_TITLE='PROGRESS-001 and PROGRESS-002' \
      XIAOHEI_PR_BODY='Task ID / 任务 ID: `PROGRESS-001` and `PROGRESS-002`' \
      bash scripts/verify-pr-delivery-metadata.sh

expect_failure 'unknown task ID' \
  env XIAOHEI_CHANGED_FILES="$required_files" \
      XIAOHEI_PR_TITLE='UNKNOWN-999' \
      XIAOHEI_PR_BODY='Task ID / 任务 ID: `UNKNOWN-999`' \
      bash scripts/verify-pr-delivery-metadata.sh

printf 'PASS PR delivery metadata accepts one known task and rejects missing status, missing mirror, multiple IDs, and unknown IDs\n'
