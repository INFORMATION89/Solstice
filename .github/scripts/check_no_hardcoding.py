#!/usr/bin/env python3
"""Check newly added production Kotlin lines for common hardcoded UI literals."""
from __future__ import annotations
import argparse
import fnmatch
import re
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path

TOKEN_SOURCE = "app/src/main/kotlin/org/breezyweather/ui/expressive/theme/Tokens.kt"
SOURCE_ROOTS = ("app/src/", "data/src/", "domain/src/", "maps-utils/src/", "ui-weather-view/src/", "weather-unit/src/")

@dataclass(frozen=True)
class Finding:
    line: int
    message: str
    excerpt: str

PATTERNS = (
    ("Compose dimension literal", re.compile(r"(?<![\w.])(-?(?:\d+(?:\.\d+)?|\.\d+))\.?(?:dp|sp|px)\b")),
    ("hardcoded Color value", re.compile(r"\bColor\s*\(\s*(0x[0-9a-fA-F]+|#[0-9a-fA-F]{3,8})")),
    ("hardcoded named Color", re.compile(r"\bColor\.(Black|White|Red|Green|Blue|Yellow|Cyan|Magenta|Gray|DarkGray|LightGray)\b")),
    ("hardcoded alpha", re.compile(r"(?:\balpha\s*=\s*|\b(?:Modifier\.)?alpha\s*\(\s*)(-?(?:\d+(?:\.\d+)?|\.\d+))f?\b")),
    ("hardcoded animation duration", re.compile(r"\b(?:delay|tween)\s*\(\s*(?:durationMillis\s*=\s*)?(-?\d+)")),
    ("hardcoded animation timing", re.compile(r"\b(?:durationMillis|delayMillis)\s*=\s*(-?\d+)")),
    ("hardcoded spring/threshold value", re.compile(r"\b(?:dampingRatio|stiffness|threshold|fraction)\s*=\s*(-?(?:\d+(?:\.\d+)?|\.\d+))f?\b")),
    ("hardcoded count or limit", re.compile(r"\b(?:count|limit|maxLines|minLines|itemCount|visibleItems|repeat|take|drop|coerceAtLeast|coerceAtMost)\s*(?:=|\()\s*(-?\d+)")),
    ("hardcoded comparison threshold", re.compile(r"(?:>=|<=|>|<)\s*(-?\d+(?:\.\d+)?)\b")),
    ("hardcoded visible text", re.compile(r'\b(?:Text|BasicText)\s*\(\s*(?:text\s*=\s*)?"([^"]+)"')),
    ("hardcoded UI label or description", re.compile(r'\b(?:contentDescription|label|placeholder|supportingText|headline|title|subtitle|description)\s*=\s*"([^"]+)"')),
    ("hardcoded snackbar text", re.compile(r'\bshowSnackbar\s*\(\s*"([^"]+)"')),
)

def is_production_source(path: str) -> bool:
    return path.endswith(".kt") and path.startswith(SOURCE_ROOTS) and "/src/test/" not in path and "/src/androidTest/" not in path

def scan(path: str, text: str, line_start: int = 1) -> list[Finding]:
    if not is_production_source(path) or path == TOKEN_SOURCE:
        return []
    lines = ["" if x.lstrip().startswith(("//", "/*", "*", "*/")) else x for x in text.splitlines()]
    cleaned = "\n".join(lines)
    result = []
    numeric_rules = {"Compose dimension literal", "hardcoded alpha", "hardcoded animation duration", "hardcoded animation timing", "hardcoded spring/threshold value", "hardcoded count or limit", "hardcoded comparison threshold"}
    for message, pattern in PATTERNS:
        for m in pattern.finditer(cleaned):
            if message in numeric_rules:
                try:
                    if float(m.group(1)) in (0.0, 1.0):
                        continue
                except ValueError:
                    pass
            line = line_start + cleaned.count("\n", 0, m.start())
            result.append(Finding(line, message, cleaned.splitlines()[line-line_start].strip()[:180]))
    return result

def parse_hunks(diff: str) -> list[tuple[str, int, str]]:
    chunks, path, start, lines = [], None, 1, []
    def flush():
        if path is not None and lines:
            chunks.append((path, start, "\n".join(lines)))
    for raw in diff.splitlines():
        if raw.startswith("+++ b/"):
            flush(); path = raw[6:]; lines = []
        elif raw.startswith("@@ "):
            flush(); lines = []
            m = re.search(r"\+(\d+)(?:,\d+)?", raw); start = int(m.group(1)) if m else 1
        elif raw.startswith("+") and not raw.startswith("+++"):
            lines.append(raw[1:])
    flush()
    return chunks

def load_allowlist() -> list[tuple[str, re.Pattern[str]]]:
    path = Path(".github/hardcoding-allowlist.txt")
    entries = []
    if not path.exists(): return entries
    for number, raw in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if not raw.strip() or raw.lstrip().startswith("#"): continue
        try:
            glob, expression = raw.split("\t", 1)
            entries.append((glob.strip(), re.compile(expression.strip())))
        except (ValueError, re.error) as error:
            raise ValueError(f"Invalid allowlist entry at {path}:{number}: {error}") from error
    return entries

def self_test() -> int:
    cases = [
        ("app/src/main/kotlin/x/Screen.kt", "padding(16.dp)", True),
        ("app/src/main/kotlin/x/Screen.kt", 'Text("Forecast")', True),
        ("app/src/main/kotlin/x/Screen.kt", 'Text(\n text = "Forecast"\n)', True),
        ("app/src/main/kotlin/x/Screen.kt", "Color(0xFF123456)", True),
        ("app/src/main/kotlin/x/Screen.kt", "Modifier.alpha(0.4f)", True),
        ("app/src/main/kotlin/x/Screen.kt", "tween(durationMillis = 240)", True),
        ("app/src/main/kotlin/x/Screen.kt", "maxLines = 3", True),
        ("app/src/main/kotlin/x/Screen.kt", "if (temperature > 4)", True),
        ("app/src/main/kotlin/x/Screen.kt", "Color.White", True),
        ("app/src/main/kotlin/x/Screen.kt", "padding(Space.S4)", False),
        ("app/src/main/kotlin/x/Screen.kt", "Text(stringResource(R.string.forecast))", False),
        ("app/src/main/kotlin/x/Screen.kt", "padding(0.dp) + padding(1.dp)", False),
        ("app/src/main/kotlin/x/Screen.kt", "maxLines = 1", False),
        ("app/src/main/kotlin/x/Screen.kt", "if (temperature > 1)", False),
        (TOKEN_SOURCE, "val S4 = 16.dp", False),
        ("app/src/test/kotlin/x/ScreenTest.kt", 'Text("test")', False),
        ("data/src/main/kotlin/x/Model.kt", "threshold = 0.5f", True),
    ]
    for path, source, expected in cases:
        actual = bool(scan(path, source))
        if actual != expected:
            print(f"SELF-TEST FAILED: {path}: {source!r}; expected {expected}, got {actual}", file=sys.stderr); return 1
    print(f"No-hardcoding checker self-test passed ({len(cases)} cases).")
    return 0

def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("base", nargs="?")
    parser.add_argument("--self-test", action="store_true")
    args = parser.parse_args()
    if args.self_test: return self_test()
    if not args.base: parser.error("base commit SHA required unless --self-test is used")
    try:
        allowlist = load_allowlist()
        diff = subprocess.run(["git", "diff", "--no-ext-diff", "--unified=0", "--no-color", args.base, "HEAD", "--",
            "app/src", "data/src", "domain/src", "maps-utils/src", "ui-weather-view/src", "weather-unit/src"],
            check=True, capture_output=True, text=True).stdout
    except (OSError, subprocess.CalledProcessError, ValueError) as error:
        print(f"No-hardcoding check could not run: {error}", file=sys.stderr); return 2
    violations = []
    for path, first_line, added in parse_hunks(diff):
        for finding in scan(path, added, first_line):
            source_line = added.splitlines()[finding.line-first_line]
            if not any(fnmatch.fnmatch(path, glob) and pattern.search(source_line) for glob, pattern in allowlist):
                violations.append((path, finding))
    if violations:
        print("New hardcoded UI literals found in added production Kotlin lines:", file=sys.stderr)
        for path, finding in violations:
            print(f"  {path}:{finding.line}: {finding.message}: {finding.excerpt}", file=sys.stderr)
        print("Use an existing token/derived value or string resource; add exceptions only after owner review.", file=sys.stderr)
        return 1
    print("No-hardcoding check passed: no guarded hardcoded literals in added production Kotlin lines.")
    return 0

if __name__ == "__main__": raise SystemExit(main())
