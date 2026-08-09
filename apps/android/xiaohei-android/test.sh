#!/usr/bin/env bash
set -euo pipefail
project_dir="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
test_dir="$project_dir/build/unit-tests"
rm -rf "$test_dir"
mkdir -p "$test_dir"
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/CommandRouter.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/CommandRouterTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.CommandRouterTest
