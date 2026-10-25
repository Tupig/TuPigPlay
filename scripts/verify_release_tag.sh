#!/usr/bin/env bash
# Fail unless GIT_REF tag matches version.properties VERSION_NAME.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck disable=SC1091
VERSION_NAME="$(python3 "$ROOT/scripts/bump_version.py" --print | awk -F= '/^VERSION_NAME=/{print $2}')"
TAG="${1:-${GITHUB_REF_NAME:-}}"
if [[ -z "$TAG" ]]; then
  echo "usage: verify_release_tag.sh vX.Y.Z" >&2
  exit 2
fi
EXPECTED="v${VERSION_NAME}"
if [[ "$TAG" != "$EXPECTED" ]]; then
  echo "tag $TAG does not match VERSION_NAME ($EXPECTED)" >&2
  exit 1
fi
echo "tag ok: $TAG"
