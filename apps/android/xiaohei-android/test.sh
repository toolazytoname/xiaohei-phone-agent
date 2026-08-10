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
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/AgentPolicy.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/AgentPolicyTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.AgentPolicyTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ChannelProfileConfig.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ConfigMigration.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/ConfigMigrationTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.ConfigMigrationTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ChannelProfileConfig.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/ChannelProfileConfigTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.ChannelProfileConfigTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/TtsLifecycle.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/TtsLifecycleTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.TtsLifecycleTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" "$project_dir/src/io/github/toolazytoname/xiaohei/FailureFingerprint.java" "$project_dir/tests/io/github/toolazytoname/xiaohei/FailureFingerprintTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.FailureFingerprintTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" "$project_dir/src/io/github/toolazytoname/xiaohei/ToolCatalog.java" "$project_dir/tests/io/github/toolazytoname/xiaohei/ToolCatalogTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.ToolCatalogTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" "$project_dir/src/io/github/toolazytoname/xiaohei/ToolCatalog.java" "$project_dir/src/io/github/toolazytoname/xiaohei/ToolGateway.java" "$project_dir/tests/io/github/toolazytoname/xiaohei/ToolGatewayTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.ToolGatewayTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" "$project_dir/src/io/github/toolazytoname/xiaohei/ToolCatalog.java" "$project_dir/src/io/github/toolazytoname/xiaohei/TaskPlanValidator.java" "$project_dir/tests/io/github/toolazytoname/xiaohei/TaskPlanValidatorTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.TaskPlanValidatorTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ModelChannelBackup.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/ModelChannelBackupTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.ModelChannelBackupTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/BoundedConversationTransport.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/BoundedConversationTransportTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.BoundedConversationTransportTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/PendingConversationCall.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/PendingConversationCallTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.PendingConversationCallTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/MemoryConversationSession.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/MemoryConversationSessionTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.MemoryConversationSessionTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/MemoryConversationSession.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ConversationSessionCoordinator.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/ConversationSessionCoordinatorTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.ConversationSessionCoordinatorTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/MemoryConversationSession.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ConversationPromptPolicy.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/ConversationPromptPolicyTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.ConversationPromptPolicyTest
