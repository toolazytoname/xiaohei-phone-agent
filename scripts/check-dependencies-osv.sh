#!/usr/bin/env bash
set -euo pipefail

apk="${1:-}"
[[ -s "$apk" ]] || { printf 'usage: %s /absolute/path/to.apk\n' "$0" >&2; exit 2; }
for tool in curl jq unzip strings shasum; do command -v "$tool" >/dev/null || {
  printf 'missing dependency: %s\n' "$tool" >&2; exit 2; }; done

work=$(mktemp -d /tmp/xiaohei-osv.XXXXXX)
trap 'rm -rf "$work"' EXIT
unzip -q "$apk" 'lib/arm64-v8a/*.so' -d "$work"
ort="$work/lib/arm64-v8a/libonnxruntime.so"
ort_version=$(strings "$ort" | grep -E -m1 '^1\.[0-9]+\.[0-9]+$' || true)
[[ -n "$ort_version" ]] || { printf 'FAIL unable to identify ONNX Runtime version\n' >&2; exit 1; }

query() {
  local package=$1 version=$2
  curl -fsS https://api.osv.dev/v1/query -H 'Content-Type: application/json' \
    --data "{\"package\":{\"name\":\"$package\",\"ecosystem\":\"PyPI\"},\"version\":\"$version\"}"
}
ort_result=$(query onnxruntime "$ort_version")
sherpa_result=$(query sherpa-onnx 1.13.4)
ort_count=$(jq '.vulns // [] | length' <<<"$ort_result")
sherpa_count=$(jq '.vulns // [] | length' <<<"$sherpa_result")
printf 'OSV onnxruntime=%s findings=%s sherpa-onnx=1.13.4 findings=%s\n' \
  "$ort_version" "$ort_count" "$sherpa_count"
printf 'native sha256:\n'
shasum -a 256 "$work"/lib/arm64-v8a/*.so
[[ "$ort_count" -eq 0 && "$sherpa_count" -eq 0 ]] || exit 1
printf 'PASS osv-known-vulnerabilities=0 coordinate_mapping=PyPI-inference\n'
