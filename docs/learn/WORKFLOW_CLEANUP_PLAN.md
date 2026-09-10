# Workflow Cleanup Plan

## Current workflows

- `android-ci.yml` — **KEEP**. Core Android Continuous Integration (CI): unit tests, lint, Debug APK build, wrapper validation, and APK metadata checks.
- `security.yml` — **KEEP**. CodeQL security analysis on pushes, pull requests, and a weekly schedule.
- `aider.yml` — **KEEP**. Explicit `@aider` issue-comment trigger for repository coding assistance.
- `release.yml` — **KEEP**. Publishes the Debug APK to GitHub Pages and creates a GitHub Release on `main` pushes/manual runs.
- `repo-hygiene.yml` — **KEEP**. Small guard against tracked generated/local files; this is a maintenance safety check, not a duplicate Android build.
- `deploy.yml` — **REMOVE**. It is manual-only and deploys the entire repository through `peaceiris/actions-gh-pages@v3`, while `release.yml` already owns the GitHub Pages APK publication. Keeping both creates overlapping deployment paths.

## Rule

Delete only workflows with no unique responsibility. Do not delete CI, security, coding-agent, release, or repository-hygiene coverage merely to reduce the file count.

## Lessons from reference projects

- Prefer clear responsibility boundaries (Koog-style separation of agent concerns).
- Keep verification explicit and machine-readable (Debroid-inspired operational hygiene).
- Keep recovery/verification paths bounded and observable (self-healing CI/CD study).
- Preserve useful Android agent capabilities rather than removing functionality during cleanup (Operit study).
