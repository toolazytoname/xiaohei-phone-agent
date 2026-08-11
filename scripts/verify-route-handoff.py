#!/usr/bin/env python3
from pathlib import Path

root = Path(__file__).resolve().parents[1]
src = root / 'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei'
main = (src / 'MainActivity.java').read_text(encoding='utf-8')
conversation = (src / 'ConversationActivity.java').read_text(encoding='utf-8')
agent = (src / 'AgentActivity.java').read_text(encoding='utf-8')

for term in (
    'RouteClarificationPolicy.Decision route = RouteClarificationPolicy.decide(text);',
    'if (route.kind != RouteClarificationPolicy.Kind.ROUTE)',
    'if (route.route == IntentRouteClassifier.Route.CHAT)',
    'if (route.route == IntentRouteClassifier.Route.COMPLEX_TASK)',
    'openConversationDraft(text);',
    'openAgentDraft(text);',
    'ConversationActivity.EXTRA_PREFILL_TEXT',
    'AgentActivity.EXTRA_TASK_DRAFT',
    'CommandRouter.Request request = route.command',
):
    if term not in main:
        raise SystemExit('FAIL route handoff missing MainActivity term: ' + term)
for term in (
    'static final String EXTRA_PREFILL_TEXT',
    'private void consumePrefill(Intent intent)',
    'input.setText(text.trim());',
    'no model/action call',
):
    if term not in conversation:
        raise SystemExit('FAIL route handoff missing Conversation term: ' + term)
for term in (
    'static final String EXTRA_TASK_DRAFT',
    'private void consumeTaskDraft(Intent intent)',
    'taskInput.setText(text.trim());',
    'routeDraftPending',
    'request planning and confirm separately.',
):
    if term not in agent:
        raise SystemExit('FAIL route handoff missing Agent term: ' + term)
if 'ConversationClient.ask(' in main or 'PhoneAgentClient.plan(' in main or 'actions.execute(this, route.command)' in main:
    raise SystemExit('FAIL route handoff must not start model or action from chat/complex branch')
print('PASS route-handoff clarification=inert chat=prefill_only complex=prefill_only command=existing model_start=0 action_start=0')
