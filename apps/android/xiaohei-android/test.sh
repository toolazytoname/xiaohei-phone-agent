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
  "$project_dir/src/io/github/toolazytoname/xiaohei/AsrProfile.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/AsrProfileTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.AsrProfileTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/AsrProvider.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/AsrProviderTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.AsrProviderTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ConversationVoiceTurnCoordinator.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/ConversationVoiceTurnCoordinatorTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.ConversationVoiceTurnCoordinatorTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ConversationEntryPolicy.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/ConversationEntryPolicyTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.ConversationEntryPolicyTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/AgentPolicy.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/SensitiveActionDenialPolicy.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/AgentPolicyTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.AgentPolicyTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/AgentPolicy.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/SensitiveActionDenialPolicy.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/SemanticAccessibilityOperationPolicy.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/SemanticAccessibilityOperationPolicyTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.SemanticAccessibilityOperationPolicyTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/AgentPolicy.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/SensitiveActionDenialPolicy.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/SemanticAccessibilityOperationPolicy.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/SemanticAppAdapterRegistry.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/SemanticAppAdapterRegistryTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.SemanticAppAdapterRegistryTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/SensitiveActionDenialPolicy.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/SensitiveActionDenialPolicyTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.SensitiveActionDenialPolicyTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ChannelProfileConfig.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/TtsChannelConfig.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ConfigMigration.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/ConfigMigrationTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.ConfigMigrationTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ChannelProfileConfig.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/ChannelProfileConfigTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.ChannelProfileConfigTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ChannelProfileConfig.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/TtsChannelConfig.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/TtsChannelConfigTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.TtsChannelConfigTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/TtsLifecycle.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/TtsLifecycleTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.TtsLifecycleTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" "$project_dir/src/io/github/toolazytoname/xiaohei/SentenceTtsQueue.java" "$project_dir/tests/io/github/toolazytoname/xiaohei/SentenceTtsQueueTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.SentenceTtsQueueTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/AudioDuplexArbiter.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/AudioDuplexArbiterTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.AudioDuplexArbiterTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/AudioDuplexArbiter.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ProcessAudioDuplex.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/ProcessAudioDuplexTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.ProcessAudioDuplexTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/AudioInterruptionPolicy.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/AudioInterruptionPolicyTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.AudioInterruptionPolicyTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" "$project_dir/src/io/github/toolazytoname/xiaohei/FailureFingerprint.java" "$project_dir/tests/io/github/toolazytoname/xiaohei/FailureFingerprintTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.FailureFingerprintTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/GlobalStopRegistry.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/GlobalStopRegistryTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.GlobalStopRegistryTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" "$project_dir/src/io/github/toolazytoname/xiaohei/GlobalStopRegistry.java" "$project_dir/src/io/github/toolazytoname/xiaohei/ApplicationStopHub.java" "$project_dir/tests/io/github/toolazytoname/xiaohei/ApplicationStopHubTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.ApplicationStopHubTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" "$project_dir/src/io/github/toolazytoname/xiaohei/ToolCatalog.java" "$project_dir/tests/io/github/toolazytoname/xiaohei/ToolCatalogTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.ToolCatalogTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" "$project_dir/src/io/github/toolazytoname/xiaohei/MediaTestCollectionPolicy.java" "$project_dir/tests/io/github/toolazytoname/xiaohei/MediaTestCollectionPolicyTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.MediaTestCollectionPolicyTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" "$project_dir/src/io/github/toolazytoname/xiaohei/CalendarTestAccountPolicy.java" "$project_dir/tests/io/github/toolazytoname/xiaohei/CalendarTestAccountPolicyTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.CalendarTestAccountPolicyTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" "$project_dir/src/io/github/toolazytoname/xiaohei/MediaControlPolicy.java" "$project_dir/tests/io/github/toolazytoname/xiaohei/MediaControlPolicyTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.MediaControlPolicyTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/FreshConfirmationGate.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ToolCatalog.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ToolGateway.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/ToolGatewayTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.ToolGatewayTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/FreshConfirmationGate.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ToolCatalog.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ToolGateway.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ToolExecutionCoordinator.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/ToolExecutionCoordinatorTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.ToolExecutionCoordinatorTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/FreshConfirmationGate.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ToolCatalog.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ToolGateway.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/LocalAndroidToolFlow.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/LocalAndroidToolFlowTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.LocalAndroidToolFlowTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" "$project_dir/src/io/github/toolazytoname/xiaohei/ToolCatalog.java" "$project_dir/src/io/github/toolazytoname/xiaohei/TaskPlanValidator.java" "$project_dir/tests/io/github/toolazytoname/xiaohei/TaskPlanValidatorTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.TaskPlanValidatorTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ToolCatalog.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/TaskPlanValidator.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/MinimalPlannerRequest.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/MinimalPlannerRequestTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.MinimalPlannerRequestTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/PlanStepObservationGuard.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/PlanStepObservationGuardTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.PlanStepObservationGuardTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" "$project_dir/src/io/github/toolazytoname/xiaohei/ToolOutcomeEvidenceGate.java" "$project_dir/tests/io/github/toolazytoname/xiaohei/ToolOutcomeEvidenceGateTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.ToolOutcomeEvidenceGateTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/TtsChannelConfig.java" \
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
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ConversationControlPolicy.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/ConversationControlPolicyTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.ConversationControlPolicyTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/OfflineFaqFallback.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/OfflineFaqFallbackTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.OfflineFaqFallbackTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ConversationPrivacyPolicy.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/ConversationPrivacyPolicyTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.ConversationPrivacyPolicyTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/MemoryConversationSession.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ConversationSessionCoordinator.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ConversationPromptPolicy.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ConversationControlPolicy.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ConversationPrivacyPolicy.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/ConversationAcceptanceMatrixTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.ConversationAcceptanceMatrixTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/CommandRouter.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/IntentRouteClassifier.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/IntentRouteClassifierTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.IntentRouteClassifierTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/CommandRouter.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/IntentRouteClassifier.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/RouteClarificationPolicy.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/RouteClarificationPolicyTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.RouteClarificationPolicyTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/CommandRouter.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/IntentRouteClassifier.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/RouteClarificationPolicy.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/MemoryConversationSession.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/UnconfirmedActionRequest.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/UnconfirmedActionRequestTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.UnconfirmedActionRequestTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/CommandRouter.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/IntentRouteClassifier.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/RouteClarificationPolicy.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/MemoryConversationSession.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/UnconfirmedActionRequest.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ToolCatalog.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/OpenCodeTaskProtocol.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/OpenCodeTaskProtocolTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.OpenCodeTaskProtocolTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/CommandRouter.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/IntentRouteClassifier.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/RouteClarificationPolicy.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/MemoryConversationSession.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/UnconfirmedActionRequest.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ToolCatalog.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/OpenCodeTaskProtocol.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/OpenCodeWorkspaceBoundary.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/OpenCodeWorkspaceBoundaryTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.OpenCodeWorkspaceBoundaryTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/CommandRouter.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/IntentRouteClassifier.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/RouteClarificationPolicy.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/MemoryConversationSession.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/UnconfirmedActionRequest.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ToolCatalog.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/OpenCodeTaskProtocol.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/OpenCodeWorkspaceBoundary.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/OpenCodeBoundedRunner.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/OpenCodeBoundedRunnerTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.OpenCodeBoundedRunnerTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/CommandRouter.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/IntentRouteClassifier.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/RouteClarificationPolicy.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/MemoryConversationSession.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/UnconfirmedActionRequest.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ToolCatalog.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/OpenCodeTaskProtocol.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/OpenCodeWorkspaceBoundary.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/OpenCodeBoundedRunner.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/OpenCodeProgressProjection.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/OpenCodeProgressProjectionTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.OpenCodeProgressProjectionTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" "$project_dir/src/io/github/toolazytoname/xiaohei/OpenCodeTakeoverOwnership.java" "$project_dir/tests/io/github/toolazytoname/xiaohei/OpenCodeTakeoverOwnershipTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.OpenCodeTakeoverOwnershipTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/FailureRecoveryProjection.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/TaskCardProjection.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/TaskCardProjectionTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.TaskCardProjectionTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" "$project_dir/src/io/github/toolazytoname/xiaohei/FailureRecoveryProjection.java" "$project_dir/tests/io/github/toolazytoname/xiaohei/FailureRecoveryProjectionTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.FailureRecoveryProjectionTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ConfirmationPreview.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/ConfirmationPreviewTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.ConfirmationPreviewTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/PermissionCenterProjection.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/PermissionCenterProjectionTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.PermissionCenterProjectionTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/CommandRouter.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/IntentRouteClassifier.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/RouteClarificationPolicy.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/MemoryConversationSession.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/UnconfirmedActionRequest.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/FreshConfirmationGate.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ToolCatalog.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ToolGateway.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/OpenCodeTaskProtocol.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/OpenCodeWorkspaceBoundary.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/OpenCodeBoundedRunner.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/OpenCodeStopCoordinator.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/OpenCodeStopCoordinatorTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.OpenCodeStopCoordinatorTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/CommandRouter.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/IntentRouteClassifier.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/RouteClarificationPolicy.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/MemoryConversationSession.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/UnconfirmedActionRequest.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ToolCatalog.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/OpenCodeTaskProtocol.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/OpenCodeToolPolicy.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/OpenCodeToolPolicyTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.OpenCodeToolPolicyTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/CommandRouter.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/IntentRouteClassifier.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/RouteClarificationPolicy.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/MemoryConversationSession.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/UnconfirmedActionRequest.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ToolCatalog.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/OpenCodeTaskProtocol.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/OpenCodeWorkspaceBoundary.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/OpenCodeBoundedRunner.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/OpenCodeToolPolicy.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/OpenCodeAcceptanceMatrixTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.OpenCodeAcceptanceMatrixTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/ToolCatalog.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/AuthorizationTierPolicy.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/AuthorizationTierPolicyTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.AuthorizationTierPolicyTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/RootCapabilityBroker.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/RootCapabilityBrokerTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.RootCapabilityBrokerTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/RootCapabilityBroker.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/RootAuditRevocationTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.RootAuditRevocationTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/RootEncryptedBackup.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/RootEncryptedBackupTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.RootEncryptedBackupTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/RootProfileTransaction.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/RootProfileTransactionTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.RootProfileTransactionTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" "$project_dir/src/io/github/toolazytoname/xiaohei/RootSystemChangePreview.java" "$project_dir/tests/io/github/toolazytoname/xiaohei/RootSystemChangePreviewTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.RootSystemChangePreviewTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/RootCapabilityBroker.java" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/RootReadOnlyDiagnostics.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/RootReadOnlyDiagnosticsTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.RootReadOnlyDiagnosticsTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/RootServiceLifecyclePolicy.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/RootServiceLifecyclePolicyTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.RootServiceLifecyclePolicyTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/RootDestructiveDenialPolicy.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/RootDestructiveDenialPolicyTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.RootDestructiveDenialPolicyTest
javac -encoding UTF-8 -source 8 -target 8 -d "$test_dir" \
  "$project_dir/src/io/github/toolazytoname/xiaohei/FreshConfirmationGate.java" \
  "$project_dir/tests/io/github/toolazytoname/xiaohei/FreshConfirmationGateTest.java"
java -cp "$test_dir" io.github.toolazytoname.xiaohei.FreshConfirmationGateTest
