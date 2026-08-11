#!/usr/bin/env python3
from pathlib import Path

root = Path(__file__).resolve().parents[1]
src = root / 'apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei'
agent = (src / 'AgentActivity.java').read_text(encoding='utf-8')
client = (src / 'PhoneAgentClient.java').read_text(encoding='utf-8')

def require(condition, message):
    if not condition:
        raise SystemExit('FAIL phone-agent planning boundary: ' + message)

for term in (
    'UnconfirmedActionRequest.Request pendingPlanningRequest',
    'UnconfirmedActionRequest.fromConversationMessage(',
    'new MemoryConversationSession.Message(MemoryConversationSession.Role.USER, task)',
    '"agent-request-" + UUID.randomUUID()',
    'Instant.now().toString()',
    'request.outcome != UnconfirmedActionRequest.Outcome.CREATED',
    'PhoneAgentClient.plan(this, request.request)',
    'pendingPlanningRequest != request.request',
    '!proposal.requestId.equals(request.request.requestId)',
    '!proposal.requestId.equals(request.requestId)',
    'if (pendingPlanningRequest == request.request) pendingPlanningRequest = null;',
    '@Override protected void onDestroy()',
    'pendingPlanningRequest = null;',
):
    require(term in agent, 'missing AgentActivity term: ' + term)
require('PhoneAgentClient.plan(this, task)' not in agent,
        'raw editable text bypasses typed pending request')
for term in (
    'static Proposal plan(Context context, UnconfirmedActionRequest.Request pending)',
    'if (!validPendingRequest(pending))',
    'String task = pending.userTextForPlanner();',
    'pending.requestId',
    'private static boolean validPendingRequest(UnconfirmedActionRequest.Request request)',
    'request.requiresConfirmation',
    'request.dryRun && !request.publicLogSafe',
    'request.sensitiveFields.equals(Collections.singletonList(UnconfirmedActionRequest.SENSITIVE_FIELD))',
):
    require(term in client, 'missing PhoneAgentClient term: ' + term)
require('static Proposal plan(Context context, String task)' not in client,
        'raw-task planner overload remains')
print('PASS phone-agent-planning-boundary source=typed_user pending=dry_run request_binding=exact planner_raw_text_bypass=0')
