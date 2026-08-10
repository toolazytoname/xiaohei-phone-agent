#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
manifest="$repo_root/manifests/github-progress.v1.json"
mode="${1:---check}"

if [[ "$mode" != "--check" && "$mode" != "--apply" ]]; then
  printf 'Usage: %s [--check|--apply]\n' "$0" >&2
  exit 2
fi

owner="$(jq -r '.owner' "$manifest")"
repository="$(jq -r '.repository' "$manifest")"
repo="$owner/$repository"
remote_labels="$(gh label list --repo "$repo" --limit 200 --json name,color,description)"
failures=0

while IFS= read -r encoded; do
  label="$(printf '%s' "$encoded" | base64 --decode)"
  name="$(jq -r '.name' <<<"$label")"
  color="$(jq -r '.color' <<<"$label")"
  description="$(jq -r '.description' <<<"$label")"

  current="$(jq -c --arg name "$name" '.[] | select(.name == $name)' <<<"$remote_labels")"
  if [[ "$mode" == "--apply" ]]; then
    gh label create "$name" --repo "$repo" --color "$color" --description "$description" --force >/dev/null
    continue
  fi

  if [[ -z "$current" || "$(jq -r '.color' <<<"$current")" != "$color" ||
        "$(jq -r '.description' <<<"$current")" != "$description" ]]; then
    printf 'FAIL label drift: %s\n' "$name" >&2
    failures=$((failures + 1))
  fi
done < <(jq -r '.labels[] | @base64' "$manifest")

if [[ "$failures" -ne 0 ]]; then
  exit 1
fi

if [[ "$mode" == "--apply" ]]; then
  printf 'PASS GitHub progress labels applied repo=%s labels=10\n' "$repo"
else
  printf 'PASS GitHub progress labels repo=%s labels=10 drift=0\n' "$repo"
fi
