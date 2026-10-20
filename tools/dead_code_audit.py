#!/usr/bin/env python3
"""Conservative dead-code audit for the real SkyPeep Android sources.

Unreferenced Kotlin imports / common private declaration forms and unused Java private
methods are certain enough to block CI. Public or reflection-owned APIs are
reported but not deleted automatically. Run with Python 3; no dependencies.
"""
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "app" / "src" / "com" / "brendigo" / "skypeep"
TOOLS = ROOT / "tools"
report = ["SkyPeep dead-code audit (static conservative pass)",
          "Sources: Kotlin Android implementation and Java asset tooling",
          "External APIs, Android lifecycle overrides and dynamic asset keys are retained."]
problems = []
checked = 0
for file in sorted(list(SOURCE.glob("*.kt")) + list(TOOLS.glob("*.java"))):
    checked += 1
    content = file.read_text(encoding="utf-8")
    code = re.sub(r"(?m)^\s*import\s+[^\n]+\n", "", content)
    code = re.sub(r"(?s)/\*.*?\*/", "", code)
    code = re.sub(r"(?m)//[^\n]*", "", code)
    if file.suffix == ".kt":
        for imp in re.findall(r"(?m)^\s*import\s+([\w.]+)(?:\s+as\s+(\w+))?\s*$", content):
            symbol = imp[1] or imp[0].split(".")[-1]
            if not re.search(r"\b" + re.escape(symbol) + r"\b", code):
                problems.append(f"{file.relative_to(ROOT)}: unused import {imp[0]}")
        declarations = re.findall(
            r"(?m)^\s*private\s+(?:(?:const|lateinit|suspend|data|sealed|enum|inline|tailrec|inner|open|abstract)\s+)*(?:fun|val|var|class|object|interface|typealias)\s+(\w+)\b", code)
        for symbol in declarations:
            if len(re.findall(r"\b" + re.escape(symbol) + r"\b", code)) == 1:
                problems.append(f"{file.relative_to(ROOT)}: unused private {symbol}")
    else:
        for symbol in re.findall(
            r"\bprivate\s+static\s+(?:void|int|double|boolean|Color|BufferedImage)\s+(\w+)\s*\(", code):
            if len(re.findall(r"\b" + re.escape(symbol) + r"\b", code)) == 1:
                problems.append(f"{file.relative_to(ROOT)}: unused Java helper {symbol}")

report.append(f"Inspected {checked} Kotlin/Java production source files")
report.append(f"High-confidence unused symbols: {len(problems)}")
report.extend(" - " + x for x in problems)

# Second pass: production-vs-tests reachability. Pure Kotlin helpers that only
# appear in tests are candidates for manual removal, NOT automatic failures:
# Java callbacks, data-class accessors and dynamic resources are legitimate.
production_files = sorted(SOURCE.glob("*.kt"))
test_dir = ROOT / "app" / "src" / "test" / "java" / "com" / "brendigo" / "skypeep"
test_files = sorted(test_dir.glob("*.kt"))
production = "\\n".join(p.read_text(encoding="utf-8") for p in production_files)
tests = "\\n".join(p.read_text(encoding="utf-8") for p in test_files)
def stripped(source: str) -> str:
    source = re.sub(r"(?s)/\\*.*?\\*/", "", source)
    return re.sub(r"(?m)//[^\\n]*", "", source)

production_code = stripped(production)
test_code = stripped(tests)
test_only = []
unreferenced_candidates = []
# Android/LibGDX lifecycle and framework hook names are invoked externally.
framework_hooks = {
    "onCreate", "create", "render", "resize", "pause", "resume", "dispose"
}
for file in production_files:
    content = stripped(file.read_text(encoding="utf-8"))
    # Anchor at the beginning of a line to exclude calls and lambdas.
    declarations = re.findall(
        r"(?m)^\\s*(?:(?:internal|public|private|override|suspend|inline|tailrec)\\s+)*fun\\s+(\\w+)\\s*\\(",
        content)
    for symbol in declarations:
        if symbol in framework_hooks:
            continue
        production_uses = len(re.findall(r"\\b" + re.escape(symbol) + r"\\b", production_code))
        if production_uses != 1:
            continue
        location = f"{file.relative_to(ROOT)}::{symbol}"
        if re.search(r"\\b" + re.escape(symbol) + r"\\b", test_code):
            test_only.append(location)
        else:
            unreferenced_candidates.append(location)
report.append(f"Inspected {len(test_files)} Kotlin JVM regression test files")
report.append(f"Test-only helper candidates for review: {len(test_only)}")
report.extend(" QA-only: " + name for name in test_only)
report.append(f"Potentially unreferenced production methods: {len(unreferenced_candidates)}")
report.extend(" REVIEW: " + name for name in unreferenced_candidates)
report.append("This second pass is informational: source-level token searches cannot establish")
report.append("reflection/framework/dynamically-named reachability. It never deletes art, tests or code.")
report.append("Dynamic artwork reachability, native GPU allocation and packaged assets are")
report.append("checked by Gradle packaging, resource SHA-256 verification and emulator CI.")
destination = ROOT / "smoke-results" / "dead-code-audit.txt"
destination.parent.mkdir(exist_ok=True)
destination.write_text("\n".join(report) + "\n", encoding="utf-8")
print("\n".join(report))
sys.exit(1 if problems else 0)
