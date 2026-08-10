#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

required_files=(
  README.md
  README.zh-CN.md
  STATUS.md
  LICENSE
  SECURITY.md
  CONTRIBUTING.md
  .github/ISSUE_TEMPLATE/delivery-task.yml
  .github/ISSUE_TEMPLATE/config.yml
  .github/pull_request_template.md
  docs/product-brief.md
  docs/product-brief.zh-CN.md
  docs/architecture.md
  docs/architecture.zh-CN.md
  docs/compatibility.md
  docs/compatibility.zh-CN.md
  docs/dsp-device-candidates.md
  docs/dsp-device-candidates.zh-CN.md
  docs/roadmap.md
  docs/roadmap.zh-CN.md
  docs/product-delivery-plan.md
  docs/product-delivery-plan.zh-CN.md
  docs/sovereign-mobile-agent-master-plan.md
  docs/sovereign-mobile-agent-master-plan.zh-CN.md
  docs/execution-backlog.md
  docs/execution-backlog.zh-CN.md
  docs/conversation-transport.md
  docs/conversation-transport.zh-CN.md
  docs/conversation-ui.md
  docs/conversation-ui.zh-CN.md
  docs/conversation-memory-session.md
  docs/conversation-memory-session.zh-CN.md
  docs/conversation-half-duplex.md
  docs/conversation-half-duplex.zh-CN.md
  docs/conversation-prompt-boundary.md
  docs/conversation-prompt-boundary.zh-CN.md
  docs/conversation-local-controls.md
  docs/conversation-local-controls.zh-CN.md
  docs/conversation-tts-selector.md
  docs/conversation-tts-selector.zh-CN.md
  docs/conversation-offline-faq.md
  docs/conversation-offline-faq.zh-CN.md
  docs/conversation-acceptance-chat-012.md
  docs/conversation-acceptance-chat-012.zh-CN.md
  docs/intent-routing-three-way.md
  docs/intent-routing-three-way.zh-CN.md
  docs/intent-routing-clarification.md
  docs/intent-routing-clarification.zh-CN.md
  docs/unconfirmed-action-request.md
  docs/unconfirmed-action-request.zh-CN.md
  docs/rules-first-task-plan.md
  docs/rules-first-task-plan.zh-CN.md
  docs/fresh-confirmation.md
  docs/fresh-confirmation.zh-CN.md
  docs/versioned-tool-catalog.md
  docs/versioned-tool-catalog.zh-CN.md
  docs/loopback-tool-gateway.md
  docs/loopback-tool-gateway.zh-CN.md
  docs/tool-execution-lifecycle.md
  docs/tool-execution-lifecycle.zh-CN.md
  docs/opencode-task-protocol.md
  docs/opencode-task-protocol.zh-CN.md
  docs/opencode-workspace-boundary.md
  docs/opencode-workspace-boundary.zh-CN.md
  docs/opencode-bounded-runner.md
  docs/opencode-bounded-runner.zh-CN.md
  docs/opencode-progress-card.md
  docs/opencode-progress-card.zh-CN.md
  docs/opencode-stop-cleanup.md
  docs/opencode-stop-cleanup.zh-CN.md
  docs/opencode-restricted-tools.md
  docs/opencode-restricted-tools.zh-CN.md
  docs/github-progress-board.md
  docs/github-progress-board.zh-CN.md
  docs/threat-model.md
  docs/threat-model.zh-CN.md
  docs/release-checklist.md
  docs/release-checklist.zh-CN.md
  docs/release-scope-0.2.0-alpha.3.md
  docs/release-scope-0.2.0-alpha.3.zh-CN.md
  docs/release-notes-0.2.0-alpha.3.md
  docs/release-notes-0.2.0-alpha.3.zh-CN.md
  docs/malware-scan-0.2.0-alpha.3.md
  docs/malware-scan-0.2.0-alpha.3.zh-CN.md
  contracts/wakeword-event.v1.schema.json
  contracts/action-request.v1.schema.json
  contracts/task-plan.v1.schema.json
  contracts/confirmation-grant.v1.schema.json
  contracts/tool-catalog.v1.schema.json
  contracts/tool-input-empty.v1.schema.json
  contracts/tool-input-volume.v1.schema.json
  contracts/tool-input-observe.v1.schema.json
  contracts/tool-output-activity.v1.schema.json
  contracts/tool-output-volume.v1.schema.json
  contracts/tool-output-observation.v1.schema.json
  contracts/agent-step-result.v1.schema.json
  contracts/diagnostics.v1.schema.json
  contracts/conversation-session.v1.schema.json
  contracts/tool-call.v1.schema.json
  contracts/tool-result.v1.schema.json
  contracts/capability-token.v1.schema.json
  contracts/opencode-task.v1.schema.json
  contracts/opencode-workspace-lease.v1.schema.json
  contracts/opencode-run-budget.v1.schema.json
  manifests/product.yaml
  manifests/github-progress.v1.json
)

for required_file in "${required_files[@]}"; do
  if [[ ! -f "$required_file" ]]; then
    printf 'FAIL missing required file: %s\n' "$required_file" >&2
    exit 1
  fi
done

for schema in contracts/*.json; do
  python3 -m json.tool "$schema" >/dev/null
done

python3 scripts/verify-conversation-session-contract.py
python3 scripts/verify-action-request-contract.py
python3 scripts/verify-task-plan-contract.py
python3 scripts/verify-confirmation-grant-contract.py
python3 scripts/verify-tool-catalog-contract.py
python3 scripts/verify-tool-gateway-contract.py
python3 scripts/verify-tool-execution-contract.py
python3 scripts/verify-opencode-task-contract.py
python3 scripts/verify-opencode-workspace-lease-contract.py
python3 scripts/verify-opencode-run-budget-contract.py
python3 scripts/verify-github-templates.py
python3 scripts/verify-github-progress-manifest.py
python3 scripts/test-status-summary.py
python3 scripts/verify-conversation-ui-boundary.py
python3 scripts/verify-conversation-memory-boundary.py
python3 scripts/verify-conversation-prompt-boundary.py
python3 scripts/verify-tts-channel-boundary.py
python3 scripts/verify-offline-faq-boundary.py
python3 scripts/verify-conversation-acceptance-boundary.py
python3 scripts/verify-intent-route-classifier.py
python3 scripts/verify-route-clarification-policy.py
python3 scripts/verify-unconfirmed-action-request.py
python3 scripts/verify-task-plan-boundary.py
python3 scripts/verify-fresh-confirmation-boundary.py
python3 scripts/verify-tool-catalog-boundary.py
python3 scripts/verify-loopback-tool-gateway-boundary.py
python3 scripts/verify-tool-execution-boundary.py
python3 scripts/verify-opencode-task-boundary.py
python3 scripts/verify-opencode-workspace-boundary.py
python3 scripts/verify-opencode-bounded-runner.py
python3 scripts/verify-opencode-progress-projection.py
python3 scripts/verify-opencode-stop-cleanup.py
python3 scripts/verify-opencode-tool-policy.py
bash scripts/test-pr-delivery-metadata.sh

python3 - scripts/*.py <<'PY'
import pathlib
import sys

for raw in sys.argv[1:]:
    path = pathlib.Path(raw)
    compile(path.read_text(encoding="utf-8"), str(path), "exec")
PY

python3 - "$repo_root" <<'PY'
import pathlib
import re
import subprocess
import sys
import urllib.parse

root = pathlib.Path(sys.argv[1]).resolve()
failures = []
tracked_markdown = subprocess.run(
    ["git", "ls-files", "--", "*.md"],
    cwd=root,
    check=True,
    capture_output=True,
    text=True,
).stdout.splitlines()
for relative in tracked_markdown:
    markdown = root / relative
    if any(part in {".git", "build", "dist", "local", "private"} for part in markdown.relative_to(root).parts):
        continue
    text = markdown.read_text(encoding="utf-8")
    for raw_target in re.findall(r"\]\(([^)]+)\)", text):
        target = raw_target.strip().split(maxsplit=1)[0].strip("<>")
        if target.startswith(("#", "http://", "https://", "mailto:")):
            continue
        target = urllib.parse.unquote(target.split("#", 1)[0])
        if not target:
            continue
        resolved = (markdown.parent / target).resolve()
        if root not in resolved.parents and resolved != root:
            failures.append(f"{markdown.relative_to(root)}: link escapes repository: {target}")
        elif not resolved.exists():
            failures.append(f"{markdown.relative_to(root)}: missing local link: {target}")

if failures:
    print("FAIL local Markdown links", file=sys.stderr)
    print("\n".join(failures), file=sys.stderr)
    raise SystemExit(1)
PY

for script in scripts/*.sh; do
  bash -n "$script"
done

forbidden_binary="$(find . \
  \( -path '*/build' -o -path './dist' -o -path './local' -o -path './private' \) -prune -o \
  -type f \( -name '*.apk' -o -name '*.aab' -o -name '*.uim' -o -name '*.udm' -o -name '*.so' -o -name '*.gguf' -o -name '*.jks' -o -name '*.keystore' -o -name '*.pem' -o -name '*.key' \) \
  -print -quit)"
if [[ -n "$forbidden_binary" ]]; then
  printf 'FAIL forbidden binary or secret material: %s\n' "$forbidden_binary" >&2
  exit 1
fi

scan_paths=(README.md README.zh-CN.md .github docs contracts manifests components apps device-profiles)
scan_files=()
while IFS= read -r scan_file; do
  scan_files+=("$scan_file")
done < <(git ls-files -- "${scan_paths[@]}")
if grep -IE --exclude='*.schema.json' 'sk-[A-Za-z0-9_-]{20,}|AKIA[0-9A-Z]{16}|BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY' "${scan_files[@]}"; then
  printf 'FAIL possible credential material detected\n' >&2
  exit 1
fi

if grep -IE '/Users/[^ /]+|/home/[^ /]+|[[:space:]][0-9a-f]{16}[[:space:]]' "${scan_files[@]}"; then
  printf 'FAIL possible private path or device identifier detected\n' >&2
  exit 1
fi

printf 'PASS required=%s schemas=%s local_links=ok forbidden_artifacts=0 credential_hits=0 private_path_hits=0\n' \
  "${#required_files[@]}" "$(find contracts -name '*.json' | wc -l | tr -d ' ')"
