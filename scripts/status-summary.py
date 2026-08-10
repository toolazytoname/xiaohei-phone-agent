#!/usr/bin/env python3
import argparse
import json
import re
import subprocess
from collections import Counter
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
LEDGER = ROOT / "docs" / "execution-backlog.zh-CN.md"
STATUS = ROOT / "STATUS.md"
KNOWN_STATES = ("DONE", "VERIFY", "IN_PROGRESS", "READY", "BACKLOG", "BLOCKED", "HUMAN")


def git(*args: str) -> str:
    result = subprocess.run(
        ["git", *args],
        cwd=ROOT,
        check=True,
        capture_output=True,
        text=True,
    )
    return result.stdout.strip()


def parse_tasks() -> list[dict[str, str]]:
    tasks: list[dict[str, str]] = []
    for line in LEDGER.read_text(encoding="utf-8").splitlines():
        cells = [cell.strip() for cell in line.strip().strip("|").split("|")]
        if len(cells) < 4 or not re.fullmatch(r"[A-Z]+-[0-9]{3}", cells[0]):
            continue
        if cells[1] not in KNOWN_STATES:
            continue
        tasks.append(
            {
                "id": cells[0],
                "state": cells[1],
                "depends_on": cells[2],
                "deliverable": cells[3],
                "evidence": cells[4] if len(cells) > 4 else "",
            }
        )
    if not tasks:
        raise SystemExit("FAIL status summary: no ledger tasks found")
    return tasks


def recent_evidence(limit: int = 5) -> list[str]:
    entries: list[str] = []
    in_section = False
    for line in STATUS.read_text(encoding="utf-8").splitlines():
        if line.startswith("## 最近证据 / Recent evidence"):
            in_section = True
            continue
        if in_section and line.startswith("## "):
            break
        if in_section and line.startswith("- "):
            entries.append(line[2:].strip())
            if len(entries) == limit:
                break
    return entries


def recent_prs(limit: int = 5) -> list[dict[str, object]]:
    log = git("log", "-50", "--format=%h%x09%s")
    prs: list[dict[str, object]] = []
    seen: set[int] = set()
    for line in log.splitlines():
        sha, separator, subject = line.partition("\t")
        if not separator:
            continue
        match = re.search(r"\(#([0-9]+)\)$", subject)
        if not match:
            continue
        number = int(match.group(1))
        if number in seen:
            continue
        seen.add(number)
        prs.append({"number": number, "sha": sha, "subject": subject})
        if len(prs) == limit:
            break
    return prs


def snapshot() -> dict[str, object]:
    tasks = parse_tasks()
    counts = Counter(task["state"] for task in tasks)
    return {
        "schema_version": "xiaohei.status-summary.v1",
        "revision": git("rev-parse", "--short=12", "HEAD"),
        "task_total": len(tasks),
        "counts": {state: counts.get(state, 0) for state in KNOWN_STATES},
        "current": [task for task in tasks if task["state"] == "IN_PROGRESS"],
        "next": [task for task in tasks if task["state"] == "READY"],
        "blocked": [task for task in tasks if task["state"] == "BLOCKED"],
        "human_gates": [task for task in tasks if task["state"] == "HUMAN"],
        "recent_prs": recent_prs(),
        "recent_evidence": recent_evidence(),
        "authority": [
            "STATUS.md",
            "docs/execution-backlog.md",
            "docs/execution-backlog.zh-CN.md",
        ],
    }


def print_tasks(title: str, tasks: list[dict[str, str]]) -> None:
    print(f"\n{title}:")
    if not tasks:
        print("  (none)")
        return
    for task in tasks:
        evidence = f" — {task['evidence']}" if task["evidence"] else ""
        print(f"  {task['id']} [{task['state']}] {task['deliverable']}{evidence}")


def print_text(data: dict[str, object]) -> None:
    print("Xiaohei delivery status / 小黑交付状态")
    print(f"Revision / 版本: {data['revision']}")
    print(f"Tasks / 任务总数: {data['task_total']}")
    for state, count in data["counts"].items():
        print(f"  {state:<12} {count}")

    print_tasks("Current / 当前", data["current"])
    print_tasks("Next / 下一步", data["next"])
    print_tasks("Blocked / 阻断", data["blocked"])
    print_tasks("Human gates / 人工门禁", data["human_gates"])

    print("\nRecent merged PRs / 最近合并 PR:")
    prs = data["recent_prs"]
    if not prs:
        print("  (none)")
    for pr in prs:
        print(f"  #{pr['number']} {pr['sha']} {pr['subject']}")

    print("\nRecent evidence / 最近证据:")
    evidence_entries = data["recent_evidence"]
    if not evidence_entries:
        print("  (none)")
    for entry in evidence_entries:
        print(f"  - {entry}")

    print("\nAuthority / 权威来源:")
    for path in data["authority"]:
        print(f"  {path}")


def main() -> None:
    parser = argparse.ArgumentParser(description="Read-only Xiaohei delivery status summary")
    parser.add_argument("--json", action="store_true", help="emit machine-readable UTF-8 JSON")
    args = parser.parse_args()
    data = snapshot()
    if args.json:
        print(json.dumps(data, ensure_ascii=False, indent=2))
    else:
        print_text(data)


if __name__ == "__main__":
    main()
