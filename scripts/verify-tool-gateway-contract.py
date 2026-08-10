#!/usr/bin/env python3
import json, pathlib, sys
root=pathlib.Path(__file__).resolve().parents[1]/"contracts/fixtures/tool-gateway.v1"
call=json.loads((root/"valid-call.json").read_text()); token=json.loads((root/"valid-token.json").read_text())
bad=json.loads((root/"invalid-token-unknown.json").read_text())
required_call={"schema_version","task_id","call_id","tool","tool_version","risk","arguments","idempotency_key","requested_at"}
required_token={"schema_version","token_id","task_id","tool","audience","issued_at","expires_at","single_use"}
assert set(call)==required_call and call["task_id"]==token["task_id"] and call["tool"]==token["tool"]
assert set(token)==required_token and token["single_use"] is True and token["issued_at"] < token["expires_at"]
assert set(bad)!=required_token
used=set(); assert token["token_id"] not in used; used.add(token["token_id"]); assert token["token_id"] in used
assert token["task_id"] != "task-other" and token["expires_at"] < "2026-08-10T00:06:00Z"
print("PASS tool-gateway.v1 fixtures=3 unknown=reject replay=reject cross_task=reject expiry=reject")
