"""Tests for the #99 report redaction and translation manifests.

Failure output intentionally contains only repository-relative names and line
numbers. It never echoes the matched text.
"""

from __future__ import annotations

import hashlib
import json
import re
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
REPORTS = (
    "execution-brief-20261021-078-r3.md",
    "test-runtime-evidence-20261021-078-attempt1.md",
    "test-runtime-evidence-20261021-078.md",
    "docs/reports/20261021-zh-CN/execution-brief-20261021-078-r3.md",
    "docs/reports/20261021-zh-CN/test-runtime-evidence-20261021-078-attempt1.md",
    "docs/reports/20261021-zh-CN/test-runtime-evidence-20261021-078.md",
)
SOURCE_MANIFEST = "docs/reports/20261021-zh-CN/source-manifest.json"
TRANSLATION_MANIFEST = "docs/reports/20261021-zh-CN/translation-validation.json"
USER_HOME_PATH = re.compile(r'''/Users/[^\s"'`<>]+''')


def sha256(relative_path: str) -> str:
    return hashlib.sha256((ROOT / relative_path).read_bytes()).hexdigest()


class DocumentationPrivacy099Test(unittest.TestCase):
    def test_execution_reports_contain_no_machine_home_paths(self) -> None:
        matches = []
        for relative_path in REPORTS:
            lines = (ROOT / relative_path).read_text(encoding="utf-8").splitlines()
            for line_number, line in enumerate(lines, start=1):
                if USER_HOME_PATH.search(line):
                    matches.append(f"{relative_path}:{line_number}")

        self.assertEqual(
            [],
            matches,
            "Machine-specific home-path locations found: " + ", ".join(matches),
        )

    def test_translation_manifests_bind_all_sixteen_pairs(self) -> None:
        source_data = json.loads((ROOT / SOURCE_MANIFEST).read_text(encoding="utf-8"))
        translation_data = json.loads(
            (ROOT / TRANSLATION_MANIFEST).read_text(encoding="utf-8")
        )
        source_rows = source_data["reports"]
        translation_rows = translation_data["报告"]

        self.assertEqual(16, len(source_rows), "source manifest row count changed")
        self.assertEqual(16, len(translation_rows), "translation manifest row count changed")

        source_pairs = {(row["source"], row["translation"]) for row in source_rows}
        translation_pairs = {(row["原件"], row["中文件"]) for row in translation_rows}
        self.assertEqual(source_pairs, translation_pairs, "manifest pairs differ")

        for row_number, row in enumerate(source_rows, start=1):
            self.assertEqual(
                row["sha256"],
                sha256(row["source"]),
                f"source hash mismatch at manifest row {row_number}",
            )

        for row_number, row in enumerate(translation_rows, start=1):
            self.assertEqual(
                row["原件SHA256"],
                sha256(row["原件"]),
                f"original hash mismatch at translation row {row_number}",
            )
            self.assertEqual(
                row["中文件SHA256"],
                sha256(row["中文件"]),
                f"translation hash mismatch at translation row {row_number}",
            )


if __name__ == "__main__":
    unittest.main()
