#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

required_files=(
  README.md
  README.zh-CN.md
  LICENSE
  SECURITY.md
  CONTRIBUTING.md
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
  contracts/wakeword-event.v1.schema.json
  contracts/action-request.v1.schema.json
  manifests/product.yaml
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

python3 - "$repo_root" <<'PY'
import pathlib
import re
import sys
import urllib.parse

root = pathlib.Path(sys.argv[1]).resolve()
failures = []
for markdown in root.rglob("*.md"):
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

forbidden_binary="$(find . -type f \( -name '*.apk' -o -name '*.aab' -o -name '*.uim' -o -name '*.udm' -o -name '*.so' -o -name '*.gguf' -o -name '*.jks' -o -name '*.keystore' -o -name '*.pem' -o -name '*.key' \) -print -quit)"
if [[ -n "$forbidden_binary" ]]; then
  printf 'FAIL forbidden binary or secret material: %s\n' "$forbidden_binary" >&2
  exit 1
fi

scan_paths=(README.md README.zh-CN.md docs contracts manifests components apps)
if grep -RIE --exclude='*.schema.json' 'sk-[A-Za-z0-9_-]{20,}|AKIA[0-9A-Z]{16}|BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY' "${scan_paths[@]}"; then
  printf 'FAIL possible credential material detected\n' >&2
  exit 1
fi

if grep -RIE '/Users/[^ /]+|/home/[^ /]+|[[:space:]][0-9a-f]{16}[[:space:]]' "${scan_paths[@]}"; then
  printf 'FAIL possible private path or device identifier detected\n' >&2
  exit 1
fi

printf 'PASS required=%s schemas=%s local_links=ok forbidden_artifacts=0 credential_hits=0 private_path_hits=0\n' \
  "${#required_files[@]}" "$(find contracts -name '*.json' | wc -l | tr -d ' ')"
