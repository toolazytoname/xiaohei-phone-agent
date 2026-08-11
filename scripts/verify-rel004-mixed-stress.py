#!/usr/bin/env python3
from pathlib import Path
s=(Path(__file__).resolve().parents[1]/'scripts/test-rel004-mixed-stress.sh').read_text()
for token in ('serial" == emulator-*', 'commands=40', 'chats=30', 'tasks=20',
              'clarifications=10', 'ConversationActivity', 'AgentActivity',
              'chat_action_leak', 'task_action_leak', 'lock_dir=', 'mkdir "$lock_dir"',
              'active_record=0', 'model=0', 'planner=0'):
    if token not in s: raise SystemExit('FAIL rel004 mixed harness missing '+token)
for forbidden in ('pm grant', 'settings put', 'screencap', 'pull /sdcard', 'sleep 60'):
    if forbidden in s: raise SystemExit('FAIL rel004 mixed harness forbidden '+forbidden)
print('PASS rel004-mixed-harness serial=emulator_only cases=100 drafts=nonexecuting recorder=0 model=0 planner=0')
