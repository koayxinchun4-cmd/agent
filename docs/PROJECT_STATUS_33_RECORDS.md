# Nexus AI — Project Status Records

> Status snapshot: 2026-09-09
>
> Purpose: preserve the core decisions, capabilities, history items, constraints, cleanup records, and current engineering direction discussed during the Nexus AI rebuild. This document is a project record, not a claim that every listed feature is already implemented.

## 1–35. Core product, architecture, history, and backlog

The original 1–35 records remain unchanged in substance:

- Product identity: independent open-source Android AI Agent.
- Platform: native Android, Kotlin + Jetpack Compose, phone-first, no root.
- Malaysia-first multilingual direction.
- Modern visible Agent product experience.
- Original Nexus UI; no copying proprietary UI/code/assets.
- Home, Chat, and Agent Task UI direction.
- Agent Core and Agent Loop v1.
- Gemini, OpenRouter, and local fallback model strategy.
- Historical specialist/CTO/Codex-style capabilities.
- Nexus Agent Studio / Agent Workspace direction.
- Skills registry/parser/import and provenance policy.
- Room-backed Memory and Chat persistence.
- GitHub and GitHub Actions/CI/CD history.
- WhatsApp automation history marked RECOVER/FUTURE.
- Planned File Agent, App Agent, Web Research, notifications/system integration, and voice.
- APK/storage strategy.
- AI Moments / AI 朋友圈 future product layer.
- Backend postponed and optional.
- Independence/legal/contribution rules.
- GPLv3 direction.
- Frontend-first engineering priority.
- Live CI verification rule.
- Hybrid Cloud / Server File Analysis — PLANNED.
- Network Security & Online Platform Integration — PLANNED, including future Bilibili, REDnote/Xiaohongshu, and Zhihu research/integration.

For the detailed historical wording, see the project history/report documents and `docs/NEXUS_PROJECT_MEMORY.md`.

## 36. Repository hygiene / build garbage cleanup — CURRENT

The repository was cleaned of generated or machine-local files that were being tracked:

- `.gradle/`
- root `build/`
- `app/build/`
- `local.properties`

`.gitignore` was strengthened to prevent these and related local outputs from returning:

- `.gradle/`
- `**/build/`
- `local.properties`
- `.idea/`
- `*.iml`
- `*.apk`
- `*.aab`
- `.env`
- `.DS_Store`

This cleanup removes reproducible run/build garbage, not Nexus source code or useful historical functionality.

## 37. Canonical project memory — CURRENT

`docs/NEXUS_PROJECT_MEMORY.md` is now the durable project-memory reference. It separates:

- long-term product/architecture decisions
- historical capabilities to preserve/recover
- security/licensing principles
- frontend-first priorities
- repository hygiene rules

Transient data such as the latest CI run, current commit, temporary errors, or build output should be verified live and should not be treated as durable project memory.

## Status labels

- **CURRENT** — already present or actively being stabilized.
- **REFACTOR** — historical capability is being redesigned into the Nexus architecture.
- **RECOVER** — useful historical capability should be restored after the foundation is stable.
- **PLANNED** — future product/engineering work.
- **REFERENCE** — retained as design/history context.
- **DEPRECATED** — should not be treated as the current implementation.

## Immediate next step

1. Continue polishing the Android frontend and Agent Task experience.
2. Keep local-first execution and safe File Agent work as the near-term capability path.
3. Keep the repository clean of generated build/run garbage.
4. Keep durable project memory and project status synchronized in `docs/`.
5. After frontend/Agent Core stabilization, continue Skills/Memory integration.
6. Evaluate cloud/server and online-platform features later; backend remains optional/postponed.
