#!/usr/bin/env python3
"""Summarize a private, transcript-free Xiaohei Mandarin evaluation CSV.

The input must never contain recordings, literal utterances, contacts, tokens,
or free-form notes. Output is aggregate-only and intentionally cannot recover
what a person said.
"""
import csv
import pathlib
import re
import statistics
import sys

REQUIRED = (
    "case_id", "speaker", "distance_cm", "environment", "category",
    "expected_outcome", "asr_exact", "routed_safely", "latency_ms", "result",
)
ALLOWED_CATEGORIES = {"command", "open_question", "negative"}
ALLOWED_BOOLEAN = {"yes", "no"}
ALLOWED_RESULTS = {"pass", "fail"}
ALLOWED_ENVIRONMENTS = {"quiet", "ordinary_indoor", "mild_outdoor"}
ALLOWED_OUTCOMES = {
    "OPEN_GALLERY", "OPEN_SETTINGS", "OPEN_BLUETOOTH", "OPEN_CAMERA", "OPEN_BROWSER",
    "STOP", "QUERY_UNREAD", "SAFE_CLARIFY", "SAFE_REJECT", "UNDERSTOOD",
}


def fail(message: str) -> None:
    raise SystemExit("FAIL voice-evaluation: " + message)


def percentile(values, p):
    ordered = sorted(values)
    index = max(0, min(len(ordered) - 1, int((len(ordered) - 1) * p)))
    return ordered[index]


def main(path: pathlib.Path) -> None:
    with path.open(encoding="utf-8", newline="") as stream:
        reader = csv.DictReader(stream)
        if tuple(reader.fieldnames or ()) != REQUIRED:
            fail("headers must exactly match the transcript-free template")
        rows = list(reader)
    if not 30 <= len(rows) <= 50:
        fail("requires 30–50 completed cases")
    seen = set()
    buckets = {key: [] for key in ALLOWED_CATEGORIES}
    speakers, distances, environments, latencies = set(), set(), set(), []
    for number, row in enumerate(rows, 2):
        if set(row) != set(REQUIRED) or any(not row[key].strip() for key in REQUIRED):
            fail(f"row {number} has missing or extra data")
        case_id = row["case_id"].strip()
        if not re.fullmatch(r"C[0-9]{2}", case_id) or case_id in seen:
            fail(f"duplicate case id at row {number}")
        seen.add(case_id)
        if (not re.fullmatch(r"S[1-9][0-9]*", row["speaker"])
                or row["environment"] not in ALLOWED_ENVIRONMENTS
                or row["expected_outcome"] not in ALLOWED_OUTCOMES):
            fail(f"non-redacted categorical value at row {number}")
        if row["category"] not in ALLOWED_CATEGORIES:
            fail(f"unsupported category at row {number}")
        if row["asr_exact"] not in ALLOWED_BOOLEAN or row["routed_safely"] not in ALLOWED_BOOLEAN:
            fail(f"boolean value at row {number}")
        if row["result"] not in ALLOWED_RESULTS:
            fail(f"result must be pass/fail at row {number}")
        try:
            distance = int(row["distance_cm"])
            latency = int(row["latency_ms"])
        except ValueError:
            fail(f"numeric distance/latency at row {number}")
        if distance not in {30, 100, 200} or latency < 0 or latency > 120000:
            fail(f"bounded distance/latency at row {number}")
        speakers.add(row["speaker"])
        distances.add(distance)
        environments.add(row["environment"])
        latencies.append(latency)
        buckets[row["category"]].append(row)
    if len(speakers) < 3 or len(distances) < 3 or len(environments) < 3:
        fail("requires at least three speakers, distances, and environments")
    if len(buckets["open_question"]) < 10 or len(buckets["negative"]) < 10:
        fail("requires at least 10 open questions and 10 negatives")
    command = buckets["command"]
    if not command:
        fail("requires command cases")
    safe = sum(row["routed_safely"] == "yes" for row in command)
    open_understood = sum(row["result"] == "pass" for row in buckets["open_question"])
    misactions = sum(row["routed_safely"] == "no" for row in rows)
    print("PASS voice-evaluation-summary"
          f" cases={len(rows)} speakers={len(speakers)} distances={len(distances)}"
          f" environments={len(environments)} command_safe={safe}/{len(command)}"
          f" open_understood={open_understood}/{len(buckets['open_question'])}"
          f" misactions={misactions} latency_p50_ms={percentile(latencies, .50)}"
          f" latency_p95_ms={percentile(latencies, .95)}")


if __name__ == "__main__":
    if len(sys.argv) != 2:
        raise SystemExit("usage: summarize-voice-evaluation.py /private/transcript-free-results.csv")
    main(pathlib.Path(sys.argv[1]))
