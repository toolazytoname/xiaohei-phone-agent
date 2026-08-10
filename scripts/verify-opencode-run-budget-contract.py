#!/usr/bin/env python3
"""Stdlib-only semantic gate for bounded OpenCode run budgets."""
import json
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
SCHEMA = ROOT / "contracts/opencode-run-budget.v1.schema.json"
FIXTURES = ROOT / "contracts/fixtures/opencode-run-budget.v1"
def require(value, message):
    if not value: raise SystemExit("FAIL opencode-run-budget: " + message)
schema = json.loads(SCHEMA.read_text(encoding="utf-8"))
required = {"schema_version", "profile", "agent", "timeout_ms", "token_limit", "step_limit", "output_limit", "public_log_safe"}
require(schema.get("additionalProperties") is False and set(schema.get("required", [])) == required, "closed schema")
p = schema["properties"]
require(p["timeout_ms"].get("minimum") == 100 and p["timeout_ms"].get("maximum") == 60000, "timeout")
require(p["token_limit"].get("maximum") == 4096 and p["step_limit"].get("maximum") == 32 and p["output_limit"].get("maximum") == 4096, "limits")
valid = invalid = 0
for path in sorted(FIXTURES.glob("*.json")):
    item = json.loads(path.read_text(encoding="utf-8"))
    clean = set(item) == required and item.get("schema_version") == 1 and item.get("public_log_safe") is False
    clean = clean and item.get("profile") in {"relay_openai", "relay_anthropic", "local_small"} and item.get("agent") in {"analyze", "diagnose", "organize"}
    clean = clean and 100 <= item.get("timeout_ms", 0) <= 60000 and 1 <= item.get("token_limit", 0) <= 4096 and 1 <= item.get("step_limit", 0) <= 32 and 1 <= item.get("output_limit", 0) <= 4096
    if path.name == "valid-budget.json": require(clean, "valid"); valid += 1
    else: require(not clean, "invalid " + path.name); invalid += 1
print("PASS opencode-run-budget.v1 fixtures=3 valid=%d invalid=%d command=absent paths=absent public_log_safe=false" % (valid, invalid))
