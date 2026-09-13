#!/usr/bin/env bash
set -euo pipefail

REPO="${1:-koayxinchun4-cmd/agent}"

if ! command -v gh >/dev/null 2>&1; then
  echo "GitHub CLI (gh) is required." >&2
  exit 1
fi

if ! gh auth status >/dev/null 2>&1; then
  echo "Run: gh auth login" >&2
  exit 1
fi

echo "Checking GitHub Pages for ${REPO}..."
if gh api "repos/${REPO}/pages" >/dev/null 2>&1; then
  echo "GitHub Pages is already enabled."
  exit 0
fi

echo "Enabling GitHub Pages with GitHub Actions as the source..."
if gh api --method POST "repos/${REPO}/pages" \
  -H "Accept: application/vnd.github+json" \
  -f build_type=workflow >/dev/null; then
  echo "GitHub Pages enabled."
  exit 0
fi

echo "Could not enable Pages automatically." >&2
echo "Make sure your GitHub account has repository administration permission." >&2
exit 1
