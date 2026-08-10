#!/usr/bin/env python3
"""Verify CHAT-006 budget alignment and the absence of transcript persistence paths."""

import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "apps/android/xiaohei-android/src/io/github/toolazytoname/xiaohei/MemoryConversationSession.java"
SCHEMA = ROOT / "contracts/conversation-session.v1.schema.json"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(f"FAIL Conversation memory boundary: {message}")


source = SOURCE.read_text(encoding="utf-8")
schema = json.loads(SCHEMA.read_text(encoding="utf-8"))
properties = schema["properties"]

expected_constants = {
    "MIN_TURNS": properties["max_turns"]["minimum"],
    "MAX_TURNS": properties["max_turns"]["maximum"],
    "MIN_TOKEN_BUDGET": properties["token_budget"]["minimum"],
    "MAX_TOKEN_BUDGET": properties["token_budget"]["maximum"],
    "MIN_TIMEOUT_MS": properties["timeout_ms"]["minimum"],
    "MAX_TIMEOUT_MS": properties["timeout_ms"]["maximum"],
}
for name, value in expected_constants.items():
    match = re.search(rf"static final (?:int|long) {name} = (\d+)(?:L)?;", source)
    require(match is not None and int(match.group(1)) == value, f"{name} aligned with contract")

for forbidden in (
    "android.content",
    "SharedPreferences",
    "SQLite",
    "RoomDatabase",
    "FileOutputStream",
    "ObjectOutputStream",
    "Serializable",
    "Parcel",
    "Log.",
    "System.out",
    "transcriptText",
):
    require(forbidden not in source, f"no persistence/logging path: {forbidden}")

require("static final List" not in source and "static List" not in source,
        "no process-global transcript collection")
require("messages.clear();" in source, "terminal paths release transcript references")
require("TOKEN_BUDGET_CLEARED" in source and "TURN_LIMIT_CLEARED" in source and
        "TIMEOUT_CLEARED" in source and "CANCELLED_CLEARED" in source,
        "all terminal budget outcomes are explicit")
require("class Status" in source and "String text" not in source[source.index("class Status"):source.index("Status(")],
        "public-log-safe status excludes transcript text")

print("PASS Conversation memory boundary budgets=contract persistence_paths=0 terminal_clear=4 public_status_text=0")
