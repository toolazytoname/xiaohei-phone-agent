#!/usr/bin/env bash
set -euo pipefail

# Builds the same selected variant twice and requires byte-identical APKs.
# Supply the same model inputs and, for release, the same external signing-key
# environment as build.sh. This is an artifact check, not a substitute for key
# governance or model redistribution approval.

project_dir="$(CDPATH= cd -- "$(dirname -- "$0")/../apps/android/xiaohei-android" && pwd)"
work_dir="$(mktemp -d /tmp/xiaohei-repro.XXXXXX)"
trap 'rm -rf "$work_dir"' EXIT

(cd "$project_dir" && bash build.sh)
first="$work_dir/first.apk"
cp "$project_dir/build/xiaohei-${XIAOHEI_BUILD_VARIANT:-debug}.apk" "$first"
(cd "$project_dir" && bash build.sh)
second="$project_dir/build/xiaohei-${XIAOHEI_BUILD_VARIANT:-debug}.apk"

if ! cmp -s "$first" "$second"; then
  printf 'FAIL reproducible-build variant=%s first=%s second=%s\n' \
    "${XIAOHEI_BUILD_VARIANT:-debug}" "$(shasum -a 256 "$first" | awk '{print $1}')" \
    "$(shasum -a 256 "$second" | awk '{print $1}')" >&2
  exit 1
fi
if [[ "${XIAOHEI_BUILD_VARIANT:-debug}" == release ]]; then
  XIAOHEI_EXPECT_RELEASE=1 bash "$project_dir/../../../scripts/scan-release-apk.sh" "$second"
else
  bash "$project_dir/../../../scripts/scan-release-apk.sh" "$second"
fi
printf 'PASS reproducible-build variant=%s sha256=%s\n' \
  "${XIAOHEI_BUILD_VARIANT:-debug}" "$(shasum -a 256 "$second" | awk '{print $1}')"
