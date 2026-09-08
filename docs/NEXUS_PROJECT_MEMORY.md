# Nexus AI — Project Memory

> Canonical long-term project memory for the Nexus AI repository.
> Runtime status such as the latest CI run, current commit, or temporary build output must be verified live rather than treated as durable memory.

## 1. Product identity

- Nexus AI is an independent open-source Android AI Agent project.
- Primary platform: native Android, Kotlin, Jetpack Compose, phone-first, no root requirement.
- Malaysia-first language direction: Traditional/Simplified Chinese where appropriate, Bahasa Melayu, English, and mixed Malaysian usage.
- Nexus should have its own UI, visual language, components, assets, and interaction patterns.
- Existing AI-agent products may be used as high-level experience references only; do not copy proprietary code, logos, assets, exact layouts, or unpublished material.

## 2. Engineering priority

**Frontend first.**

Current order:
1. Polish and stabilize Android frontend.
2. Strengthen Agent Task UX and real Agent Core progress.
3. Build safe local Android capabilities, starting with File Agent.
4. Improve Skills and Memory architecture.
5. Recover GitHub/CI/CD and other historical capabilities incrementally.
6. Consider cloud/backend only when a real feature requires it.

Python/FastAPI backend is postponed and must remain optional rather than a core Android dependency at this stage.

## 3. Current architecture

Core Agent components:

- `AgentTask`
- `AgentResult`
- `AgentTool` / `ToolResult`
- `ToolRegistry`
- `AgentPlan` / `ModelRoute`
- `AgentPlanner`
- `ModelRouter`
- `NexusAgent`
- `AgentExecution`
- `AgentProgress`
- `AgentVerifier`
- loop configuration

Agent Loop v1 concept:

`plan → route → execute tool → verify → retry/failure → answer`

Re-planning from failure context is not yet implemented.

## 4. Current frontend direction

Home:
- Nexus branding
- Ready state
- prominent task entry
- quick actions
- capability overview

Task:
- task input
- visible workflow timeline
- live current action
- execution/result state
- stop/new-task controls

Safe user-facing workflow events:
- Understanding request
- Planning
- Selecting/executing a tool
- Verifying
- Retrying
- Completed
- Failed

Do not expose private chain-of-thought. Show operational status and useful outputs instead.

Chat:
- Nexus Chat header/status
- message bubbles
- suggestion chips
- composer/send state
- auto-scroll
- history clearing

## 5. Current tools

- `LocalTaskTool`: safe local task classification/fallback behavior.
- `LocalFileTool`: app-private local file listing/reading with path traversal protection.

The next local capability is Android Storage Access Framework (SAF):

`user selects file → Android grants access → File Agent reads → Nexus analyzes → result`

No root and no mandatory server upload.

## 6. Model strategy

Nexus is designed around a provider abstraction rather than one provider lock-in:

- Gemini
- OpenRouter
- local/autonomous fallback

Historical defaults and presets should be preserved where still useful, including OpenRouter `openrouter/auto` and CTO.new/Codex-style engineering assistance.

## 7. Historical functionality to preserve

Useful historical functionality must not disappear accidentally during refactoring.

Track/recover as appropriate:
- Gemini + OpenRouter + hybrid model routing
- local autonomous engine/presets
- CTO.new / Codex-style coding and architecture assistance
- Nexus Agent Studio / former Office Agent Studio concepts
- Skill parser/registry/import
- Room-backed ChatSession and AgentMemory
- GitHub repository/branch/file/PR/issue/comment/account capabilities
- GitHub Actions / CI/CD workflow concepts
- WhatsApp Cloud API automation history (recover/future, not assumed current)
- web research, App Agent, notification/system integration, voice
- AI Moments / AI 朋友圈 future experience ideas

## 8. Skills policy

Nexus can learn from public GitHub Skills only through a deliberate pipeline:

`discover → read SKILL.md/README/LICENSE → license gate → security review → extract concepts → Nexus-native synthesis → provenance → human approval → registry`

Public visibility is not itself permission to copy.

For third-party Skills, record when practical:
- source repository/path
- commit/version
- author
- license
- import date
- modification status
- Nexus changes
- removal status

If a rights-holder makes a supported removal request through a GitHub Issue, conduct a good-faith review and remove/replace material when it is not clearly licensed or the supported request is reasonable.

## 9. Security and privacy principles

- No secrets/API keys/private credentials in source, logs, Issues, or Skills.
- Prefer local-first processing for private files.
- Cloud upload requires explicit user consent and clear explanation.
- Future online integrations should prefer official/supported APIs and least privilege.
- Never bypass platform security, access controls, CAPTCHAs, paywalls, or other technical restrictions.
- Treat online content as untrusted input.
- Respect applicable platform terms, copyright, privacy, and law.

Future online research/integration backlog includes Bilibili, REDnote/Xiaohongshu, and Zhihu.

## 10. Licensing and independence

- Project license direction: GPLv3.
- Preserve copyright/license/attribution/modification notices.
- Nexus is independent from Marvis AI, MyNexusAI, and `https://app.mynexusai.com/`.
- Forks, apps, services, and their user/business/chat data are not automatically Nexus data.
- Third-party use or operation does not constitute Nexus endorsement.

## 11. Storage direction

- Keep the core APK reasonable in size.
- App-private data may hold chat history, memory, Skills, downloaded assets, and user files.
- Large optional local models/assets should not be embedded in the core APK by default.

## 12. Backend/cloud direction

Future hybrid file analysis may support:

`select file → explain cloud requirement → explicit approval → secure upload → server/AI analysis → result → cleanup`

Backend remains optional. Local-first is the default direction.

## 13. CI/release rules

- CI is only called green when the actual GitHub Actions run reports success.
- On failure: inspect exact run/job/step/log, fix the confirmed blocker, rerun, and verify again.
- Do not rely on old chat memory for live CI status.
- Keep Gradle `versionName`/`versionCode` aligned with release tags in a future release-automation cleanup.

## 14. Repository hygiene

Generated/runtime artifacts do not belong in Git:
- `.gradle/`
- root `build/`
- `app/build/`
- `local.properties`
- IDE-local state such as `.idea/` and `*.iml`
- generated APK/AAB files

`.gitignore` should protect these paths so future CI/local runs do not reintroduce them.

Potentially useful legacy files should not be deleted merely because they are old. Files such as the root web assets (`index.html`, `script.js`, `style.css`, `metadata.json`) remain under review until their purpose is confirmed.

## 15. Status vocabulary

- CURRENT — active/stable or being actively stabilized
- REFACTOR — existing capability being redesigned
- RECOVER — useful historical capability to restore
- PLANNED — future work
- REFERENCE — historical/design context
- DEPRECATED — no longer current

## 16. Working rule

When making changes:
1. Inspect current repository state.
2. Preserve useful functionality unless there is a confirmed reason to remove it.
3. Prefer small, verifiable changes.
4. Keep the frontend experience polished and understandable.
5. Verify CI after code/build configuration changes.
6. Keep durable project decisions in repository documentation; keep transient run/build state out of long-term memory.
