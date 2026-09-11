# Nexus AI — Old Version History

> Archive index for legacy Nexus AI planning, implementation branches, experiments, and earlier product directions.
>
> **Important:** Git branches are Git references, not ordinary files, so they are not literally moved into this Markdown file. Legacy branches remain preserved on GitHub as historical references. This file records what they represent and prevents them from being mistaken for the current product roadmap.

## Current authority

- `main` = current source of truth for active implementation.
- `CURRENT_ROADMAP.md` = current product direction.
- `history.md` = longer-form historical engineering record.
- `docs/NEXUS_FEATURE_ARCHITECTURE_AND_HISTORY.md` = feature status/history classification.
- `.github/workflows/roadmap.yml` = legacy Phase 1–7 verification workflow; retained for regression/history, not current product priority.

## Legacy branches preserved

The following branches were found in the repository and are recorded here as historical / feature workstreams:

| Branch | Historical purpose / interpretation | Current treatment |
|---|---|---|
| `feature/agent-task-runtime` | Agent task runtime implementation | Legacy feature branch; use `main` for current behavior |
| `feature/apk-upgrade-versioning` | APK/versioning work | Legacy release experiment; do not treat as product roadmap |
| `feature/app-agent` | Native Android App Agent | Historical implementation; concepts may remain relevant |
| `feature/ci-complete-suite` | Expanded CI/security validation | Historical CI workstream; retain useful patterns |
| `feature/integrate-pr6-foundation` | Earlier foundation integration | Historical integration branch |
| `feature/phase2-model-provider` | Model-provider / Phase 2 work | Historical architecture work; superseded by current provider layer |
| `feature/real-model-providers-cleanup` | Provider cleanup iteration | Historical refactor branch |
| `feature/real-model-providers-cleanup-v2` | Provider cleanup iteration v2 | Historical refactor branch |
| `feature/real-model-providers-cleanup-v3` | Provider cleanup iteration v3 | Historical refactor branch |
| `feature/real-model-providers-v2` | Real model provider implementation | Historical provider branch |
| `feature/roadmap-all-no-apk` | Earlier Roadmap ALL verification without APK packaging | Legacy roadmap automation |
| `feature/roadmap-auto-execution` | Earlier roadmap/agent auto-execution experiment | Legacy roadmap automation |
| `feature/settings-language` | Settings and localization work | Historical feature branch; language direction remains current |
| `feature/task-real-results` | Agent task result handling | Historical task-execution work |

## Legacy roadmap

The earlier workflow was organized as:

1. Nexus Foundation
2. Real Agent
3. Mobile Agent
4. Skills + Memory
5. Web / Office / GitHub
6. AI Moments / AI 朋友圈
7. Advanced multi-model, local assets, and agent collaboration

The repository's current workflow implementation later used `Phase 7 - Voice + Optimization`, so the workflow itself should be considered a verification artifact rather than a canonical statement of product strategy.

The legacy roadmap is valuable for regression checks around foundational capabilities, but it is not a requirement to continue implementing old phases in sequence.

## Historical capability classification

According to the architecture/history documentation, older capabilities fall into several categories:

- `CURRENT`: already present in the modern architecture or actively used.
- `RECOVER`: historically present and potentially worth restoring.
- `REFACTOR`: concept remains useful but should be redesigned using the current Nexus architecture.
- `PLANNED`: intentionally future-facing.
- `REFERENCE`: inspiration/study only; not proof of Nexus implementation.

Examples of historical or transitional areas include older Hybrid AI / OpenRouter implementations, CTO-style presets, Office Agent Studio, GitHub skill import, WhatsApp automation, AI Moments, AI Community, and Voice.

## Do not resurrect blindly

Historical code, branches, and feature ideas must not be merged only because they once existed.

Before recovering a historical capability:

1. Check the current architecture.
2. Confirm it still serves the current product direction.
3. Reimplement or adapt it safely rather than blindly restoring old code.
4. Preserve security, privacy, provenance, and Android permission boundaries.
5. Add focused tests and validate against the current contract.

## Relationship to current Nexus

The current Nexus direction is Agent-first and mobile-first:

```text
User Goal
  -> Intent / Planning
  -> Model Selection
  -> Skill / Tool Selection
  -> Safe Android / Web / GitHub Execution
  -> Verification
  -> Recovery when needed
  -> Result + Memory
```

The old branches and roadmap are therefore preserved as **history and regression references**, not as an instruction to build every historical feature.
