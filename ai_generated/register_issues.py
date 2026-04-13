#!/usr/bin/env python3
"""Issue一括登録スクリプト - GITHUB_TOKEN にIssues write権限が必要"""
import json
import subprocess
import sys
import re
import time

REPO = "okegawaatclink/gaido-my-app"

with open("ai_generated/issues.json", "r") as f:
    issues = json.load(f)

# Check for already registered issues
try:
    with open("ai_generated/issue_numbers.json", "r") as f:
        id_to_number = json.load(f)
except FileNotFoundError:
    id_to_number = {}

print(f"=== Issue一括登録 ({len(issues)}件, 登録済み{len(id_to_number)}件) ===")

for i, issue in enumerate(issues):
    issue_id = issue["id"]
    if issue_id in id_to_number:
        print(f"[{i+1}/{len(issues)}] SKIP (already #{id_to_number[issue_id]}): {issue['title']}")
        continue

    print(f"[{i+1}/{len(issues)}] Creating: {issue['title']}")

    cmd = [
        "gh", "issue", "create",
        "-R", REPO,
        "--title", issue["title"],
        "--body", issue["body"],
    ]
    for label in issue.get("labels", []):
        cmd.extend(["--label", label])

    result = subprocess.run(cmd, capture_output=True, text=True)

    if result.returncode != 0:
        print(f"  ERROR: {result.stderr.strip()}")
        sys.exit(1)

    url = result.stdout.strip()
    match = re.search(r"/issues/(\d+)", url)
    if match:
        number = int(match.group(1))
        id_to_number[issue_id] = number
        print(f"  -> Created: #{number}")

        # Save progress after each issue
        with open("ai_generated/issue_numbers.json", "w") as f:
            json.dump(id_to_number, f, ensure_ascii=False, indent=2)
    else:
        print(f"  WARNING: Could not extract issue number from: {url}")

    time.sleep(1)  # Rate limit

print(f"\n=== 完了: {len(id_to_number)}件登録済み ===")
