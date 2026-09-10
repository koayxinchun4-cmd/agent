#!/usr/bin/env bash
set -euo pipefail

# Nexus repository hygiene guard.
# A file that matches .gitignore must not already be tracked by Git.
# --no-index is important because normal git check-ignore ignores tracked files.

mapfile -t violations < <(
  git ls-files -z \
    | git check-ignore --no-index -v --stdin 2>/dev/null || true
)

if ((${#violations[@]} > 0)); then
  echo "NEXUS_REPO_HYGIENE::FAIL tracked files match .gitignore rules"
  printf '%s\n' "${violations[@]}"
  echo
  echo "Fix: remove generated/local-only files from Git tracking, then commit the change."
  echo "Example: git rm --cached -r <path>"
  exit 1
fi

echo "NEXUS_REPO_HYGIENE::PASS no tracked files match .gitignore rules"
