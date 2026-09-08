# Nexus AI — Repository Cleanup Record

## Goal

Keep the Git repository focused on source, tests, configuration, Skills, documentation, and intentionally versioned assets. Build/run garbage should stay out of Git.

## Removed from `main`

The cleanup removed generated/local-only paths that were visibly tracked in the repository:

- `.gradle/` — Gradle local cache/state
- `build/` — root build output
- `app/build/` — Android/Gradle build output, intermediates, reports, generated files, and APK build products
- `local.properties` — machine-specific Android SDK/local configuration

These are reproducible or machine-specific and should not be part of the source repository.

## `.gitignore` protection

The repository now ignores:

- `.env`
- `local.properties`
- `.gradle/`
- `**/build/`
- APK/AAB outputs
- `.idea/`
- `*.iml`
- `.DS_Store`

This prevents ordinary local builds and CI-related workspace files from being reintroduced accidentally.

## Intentionally preserved

The cleanup did **not** remove source code, tests, Skills, CI workflows, legal files, or project documentation merely because they are old.

The root web files (`index.html`, `script.js`, `style.css`, `metadata.json`) are retained for now because their exact current purpose has not been sufficiently confirmed. They should be reviewed separately rather than deleted blindly.

Historical Nexus functionality also remains tracked through project records so that useful capabilities are not lost during refactoring.

## Cleanup rule for future work

Classify files before deleting them:

| Category | Action |
|---|---|
| Source / test / config | Preserve unless confirmed obsolete |
| Documentation / provenance | Preserve and consolidate where useful |
| Build output / cache | Remove from Git and ignore |
| Machine-local config | Remove from Git and ignore |
| Temporary logs/reports | Remove unless intentionally used as documentation |
| Historical feature code | Mark CURRENT/REFACTOR/RECOVER/DEPRECATED before removal |
| Unknown legacy file | Review first; do not guess |

## Relationship to CI

GitHub Actions should generate build artifacts during runs rather than commit them into the repository. The repository is the source of truth; CI output belongs in Actions artifacts/releases when intentionally published.

## Status

This cleanup is a repository-hygiene change. It does not mean historical Nexus capabilities are deleted from the product plan; it only removes reproducible local/build garbage from Git tracking.
