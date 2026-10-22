#!/usr/bin/env python3
"""Check that the release step's embedded Bash is syntactically valid.

No YAML parser or network dependency: locate the one literal run block for the
trusted-branch publisher and pass its dedented body to bash -n.
"""
from pathlib import Path
import subprocess
import sys

source = Path(sys.argv[1] if len(sys.argv) > 1 else ".github/workflows/android.yml")
lines = source.read_text(encoding="utf-8").splitlines()
try:
    heading = next(i for i, line in enumerate(lines)
                   if line.strip() == "- name: Publish APK and unsigned AAB after successful QA")
    start = next(i for i in range(heading + 1, len(lines)) if lines[i].strip() == "run: |")
except StopIteration as error:
    raise SystemExit(f"Release shell validation: missing publisher step: {error}")
indent = len(lines[start]) - len(lines[start].lstrip())
body = []
for line in lines[start + 1:]:
    if line.strip() and len(line) - len(line.lstrip()) <= indent:
        break
    body.append(line[indent + 2:] if line.strip() else "")
if not body:
    raise SystemExit("Release shell validation: publisher is empty")
result = subprocess.run(["bash", "-n"], input="\n".join(body) + "\n", text=True, capture_output=True)
if result.returncode:
    raise SystemExit(f"Release script has invalid Bash syntax:\n{result.stderr}")
print("PASS: release Bash syntax valid, including literal release notes")
