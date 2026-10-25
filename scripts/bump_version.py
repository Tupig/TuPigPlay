#!/usr/bin/env python3
"""Bump TuPigCarPlay VERSION_NAME / VERSION_CODE in version.properties.

Usage:
  scripts/bump_version.py patch|minor|major   # SemVer + VERSION_CODE++
  scripts/bump_version.py code                # VERSION_CODE++ only
  scripts/bump_version.py --print             # print current name and code
"""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PROPS = ROOT / "version.properties"
SEMVER = re.compile(r"^(\d+)\.(\d+)\.(\d+)$")


def read_props(path: Path) -> dict[str, str]:
    values: dict[str, str] = {}
    for raw in path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        values[key.strip()] = value.strip()
    if "VERSION_NAME" not in values or "VERSION_CODE" not in values:
        raise SystemExit(f"missing VERSION_NAME/VERSION_CODE in {path}")
    return values


def write_props(path: Path, name: str, code: int) -> None:
    path.write_text(
        "# Single source for the product APK (module :mobile).\n"
        "# versionName / git tag: SemVer MAJOR.MINOR.PATCH (tag is v${VERSION_NAME}).\n"
        "# VERSION_CODE: monotonic; bump on every published APK (see scripts/bump_version.py).\n"
        f"VERSION_NAME={name}\n"
        f"VERSION_CODE={code}\n",
        encoding="utf-8",
    )


def bump_semver(name: str, kind: str) -> str:
    match = SEMVER.match(name)
    if not match:
        raise SystemExit(f"VERSION_NAME must be MAJOR.MINOR.PATCH, got {name!r}")
    major, minor, patch = (int(match.group(1)), int(match.group(2)), int(match.group(3)))
    if kind == "major":
        return f"{major + 1}.0.0"
    if kind == "minor":
        return f"{major}.{minor + 1}.0"
    if kind == "patch":
        return f"{major}.{minor}.{patch + 1}"
    raise SystemExit(f"unknown bump {kind!r}")


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "action",
        nargs="?",
        choices=("patch", "minor", "major", "code"),
        help="SemVer segment to bump, or 'code' for VERSION_CODE only",
    )
    parser.add_argument("--print", action="store_true", help="print VERSION_NAME and VERSION_CODE")
    parser.add_argument("--dry-run", action="store_true", help="print result without writing")
    args = parser.parse_args(argv)

    values = read_props(PROPS)
    name = values["VERSION_NAME"]
    code = int(values["VERSION_CODE"])

    if args.print and not args.action:
        print(f"VERSION_NAME={name}")
        print(f"VERSION_CODE={code}")
        print(f"TAG=v{name}")
        return 0

    if not args.action:
        parser.error("action required unless --print")

    if args.action == "code":
        new_name, new_code = name, code + 1
    else:
        new_name, new_code = bump_semver(name, args.action), code + 1

    print(f"VERSION_NAME={name} -> {new_name}")
    print(f"VERSION_CODE={code} -> {new_code}")
    print(f"TAG=v{new_name}")
    if args.dry_run:
        return 0
    write_props(PROPS, new_name, new_code)
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
