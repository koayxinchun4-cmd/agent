---
name: android-ci-agent
description: Diagnose, repair, and verify Nexus Android CI failures on GitHub Actions using run, job, step, log, rerun, and artifact evidence.
category: devops
risk: high
source: Nexus-native synthesis
---

# Nexus Android CI Agent

Use this Skill when Nexus Android CI or GitHub Actions fails, when changing the Android CI workflow, or when verifying a build/test artifact before calling a change complete.

## Goal

Turn a CI failure into an evidence-based repair loop:

`Run -> Job -> Step -> Log -> Classify -> Fix -> Re-run -> Verify -> Artifact`

Never claim CI is green until the relevant GitHub Actions run is actually verified as successful.

## 1. Inspect the current repository first

Before changing workflow configuration:

1. Read the current `.github/workflows/*.yml` files.
2. Identify triggers, jobs, dependencies, Android/Java versions, Gradle commands, caches, artifacts, and concurrency rules.
3. Prefer the repository's existing setup over copying an unrelated workflow example.
4. Keep Nexus Android builds aligned with the project's actual Gradle/Kotlin/AGP configuration.

Do not invent a second workflow inventory inside this Skill when the workflow file itself is the source of truth.

## 2. Diagnose a failed run

When CI fails, inspect in this order:

1. Find the newest relevant workflow run for the branch/commit.
2. Inspect the failed job.
3. Inspect the failed step.
4. Read the complete useful portion of the job log.
5. Separate the primary failure from warnings, cache messages, deprecations, and skipped downstream steps.
6. Record the exact file, line, task, and error when available.

For Nexus, an old failed run must not be used as proof that a newer fix failed. Always correlate the run with the commit SHA.

## 3. Classify common Android failures

### Kotlin / Compose compiler

Look for:
- `Unresolved reference`
- type mismatch
- missing import
- Compose scope errors
- Kotlin compiler or Compose compiler incompatibilities

Fix the smallest confirmed cause first. Avoid speculative rewrites.

### Gradle

Look for:
- dependency resolution failures
- task configuration errors
- plugin/version incompatibilities
- daemon/build cache issues

Prefer fixing the project configuration rather than hiding the failure with weaker CI checks.

### Android SDK / JDK

Check:
- compileSdk / targetSdk
- installed SDK packages
- Java version
- Android Gradle Plugin compatibility

Do not manually download SDK archives when the hosted runner/setup already provides the required SDK.

### Tests / lint

Distinguish:
- compilation failure
- unit-test failure
- lint/static-analysis failure
- instrumentation/device failure

Do not treat a skipped test stage as a passing test stage.

### Release/signing

Never commit keystores, passwords, API keys, or private signing credentials. Use GitHub Secrets for release credentials and keep debug/release signing concerns separate.

## 4. Repair loop

After identifying the confirmed blocker:

1. Fetch the current file contents.
2. Make the smallest necessary change.
3. Commit with a clear message describing the fix.
4. Wait for the new CI run.
5. Inspect the new run, not an older run.
6. If it fails, repeat from evidence.

Do not make several unrelated speculative changes in one repair cycle.

## 5. Verify success

A successful repair requires evidence from GitHub Actions:

- relevant run status is successful;
- required build/test job is successful;
- no required downstream step was silently skipped because of an earlier failure;
- if an artifact is expected, verify that the artifact was produced.

For an Android APK build, the final evidence should preferably include the successful build task and the expected APK artifact.

## 6. Artifacts

Use workflow artifacts for APKs, test reports, lint reports, mappings, or other CI outputs when the workflow publishes them.

Failure diagnostics may also be uploaded with `if: always()` when appropriate so failed runs retain useful reports.

## 7. GitHub Actions design principles

- Use supported GitHub Actions versions and follow the repository's established version/pinning policy.
- Use Gradle caching when it is compatible with the project.
- Keep secrets out of source control.
- Use explicit job dependencies when ordering matters.
- Keep artifact names stable when downstream debugging relies on them.
- Avoid unnecessary matrix builds for a small Android application.
- Add timeouts to jobs that can hang.
- Keep release automation separate from ordinary PR verification when practical.

## 8. Nexus-specific safety rules

- Do not expose API keys, OAuth tokens, keystore contents, or personal credentials in logs, commits, issues, or Skill text.
- Do not delete existing useful Nexus functionality merely to make CI pass.
- Preserve historical functionality unless the change is intentional and documented.
- Do not claim that a tool, feature, backend, or integration is implemented unless the repository actually contains the implementation.
- Keep the Android frontend independent of a future Python backend unless the architecture explicitly changes.

## 9. Current Nexus verification workflow

For a normal Nexus Android change:

`commit -> GitHub Actions -> build/test -> inspect failure if any -> fix -> rerun -> verify green -> continue frontend work`

For CI debugging:

`latest Run -> failed Job -> failed Step -> exact Log -> minimal Fix -> new Run -> verify`

## Provenance / learning notes

This Skill is a Nexus-native synthesis informed by publicly available community Skill patterns. It is not a verbatim copy of another project's Skill.

Reference sources studied:

- `prasad-vennam/Awesome-Android-AI-Agent-Skills`
  - `skills/android-ci-cd-expert/SKILL.md`
  - commit `302d994c51d3fc4411e826ea3c6964234cc20c14`
  - repository license: MIT
  - useful concepts: Android CI/CD, Java setup, Gradle caching, release signing hygiene, R8/release verification, CI anti-patterns.

- `po4yka/RIPDPI`
  - `.agents/skills/ci-workflow-authoring/SKILL.md`
  - commit `d56d2cc532dac6d19fa90619bbe8270d6d45c821`
  - repository license: BSD 3-Clause
  - useful concepts: current workflow as source of truth, pinned action references, artifact preservation, job design, CI failure triage, and managed-device guidance.

Nexus does not claim ownership of the referenced source projects or their original Skills. Their licenses and copyright notices remain applicable to their respective source material.
