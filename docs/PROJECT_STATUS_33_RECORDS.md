# Nexus AI — Project Status 33 Records

> Status snapshot: 2026-09-08
>
> Purpose: preserve the 33 core decisions, capabilities, history items, constraints, and current engineering status discussed during the Nexus AI rebuild. This document is a project record, not a claim that every listed feature is already implemented.

## 1. Product identity
Nexus AI is an independent, open-source Android AI Agent project. It should have its own product identity and UI rather than copying another product's interface.

## 2. Platform
Primary target is native Android, Kotlin + Jetpack Compose, phone-first, without requiring root access.

## 3. Regional language direction
The product is designed for Malaysia-first usage, including Traditional/Simplified Chinese where appropriate, Bahasa Melayu, English, and mixed Malaysian communication.

## 4. Agent product experience
The frontend should feel like a modern personal AI Agent: a clear home screen, task entry, visible progress, tools, verification, and completion states.

## 5. UI originality
Marvis AI may be used only as a high-level product-experience reference. Nexus must not copy proprietary code, logos, assets, or an exact visual layout.

## 6. Home screen direction
The current frontend direction includes Nexus branding, Ready status, a prominent task-entry card, quick actions, and an Agent capabilities list.

## 7. Chat screen direction
The chat frontend includes a Nexus Chat header, status indicator, message bubbles, auto-scroll, suggestion chips, text composer, send state, and history clearing.

## 8. Agent workflow visualization
Future task UI should expose the Agent workflow concept: understand request → plan → use tool → verify → retry when needed → complete.

## 9. Agent Core foundation
The project has an Agent Core foundation containing AgentTask, AgentResult, AgentTool, ToolResult, ToolRegistry, AgentPlan, AgentPlanner, ModelRouter, NexusAgent, AgentExecution, AgentVerifier, and loop configuration.

## 10. Agent loop
Agent Loop v1 follows planner → model route → tool execution → verification → retry/failure handling. Re-planning from failure context is intentionally not yet implemented.

## 11. Model abstraction
The project is designed to support Gemini, OpenRouter, and a local/on-device fallback rather than depending on one provider.

## 12. Gemini
Gemini API integration was fixed from the obsolete model endpoint to the `v1beta` generateContent flow using a supported Gemini model configuration, with error handling.

## 13. OpenRouter
OpenRouter remains part of the multi-model direction, historically using `openrouter/auto` as the default routing choice.

## 14. Local fallback
When external model configuration is unavailable, Nexus should retain a local/autonomous fallback path rather than making the app unusable.

## 15. Specialist presets
Historical local presets include CTO.new-style architecture/engineering assistance and coding/Codex-style tasks. These capabilities should be preserved or reintroduced through the Agent architecture.

## 16. CTO.new / coding capability
The historical project direction includes architecture design, technology selection, module planning, code review, sprint planning, Kotlin/Compose coding assistance, debugging, and code improvement.

## 17. Agent Studio
The historical Office Agent Studio direction is being transformed into a Nexus Agent Studio / Agent Workspace concept for multi-agent tasks, workflows, GitHub work, and custom tasks.

## 18. Skills
Nexus should support a Skill registry and Skill parser/import workflow, including extraction of GitHub `SKILL.md`-style definitions where rights and licensing permit.

## 19. Skills provenance
Third-party Skills should record source repository/path, commit or version, author, license, import date, modification status, Nexus changes, and removal status when practical. No unverified third-party Skill inventory should be presented as confirmed open source.

## 20. Skills removal policy
If an original Skill developer requests removal through a GitHub Issue and provides enough information to identify the Skill and their rights/concern, Nexus should conduct a good-faith review and remove or replace the material when it is not clearly licensed or the supported request is reasonable.

## 21. Memory
Room-backed conversation history and AgentMemory are part of the architecture. Memory should support useful context without turning private user/business/chat data into public project data.

## 22. Chat persistence
ChatSession and related local history functionality should be preserved while the frontend and Agent architecture are refactored.

## 23. GitHub integration
The historical feature set includes repository access, branches, file content, pull requests, issues, issue comments/updates, GitHub account information, and an OAuth/device-flow direction.

## 24. GitHub Actions / CI/CD
Historical Office Agent Studio functionality included CI/CD workflow concepts such as build, test, artifact, release, and GitHub Actions workflow injection. These should be recovered incrementally after the frontend foundation is stable.

## 25. WhatsApp automation history
The historical project included WhatsApp Cloud API, sending messages, phone number/access/verification configuration, webhook configuration, and a Gemini + GitHub + WhatsApp automation bridge. Parts were later removed during security/bug cleanup; classify this history as RECOVER/FUTURE rather than assuming it is currently implemented.

## 26. Mobile Agent capabilities
Planned mobile abilities include File Agent, App Agent, web research, notification/system integration, and voice. Android SDK/Intent/Storage Access Framework should be preferred; Accessibility requires explicit user authorization; root/private APIs are not required.

## 27. Storage strategy
The core APK should remain reasonable in size. App data can hold chat history, memory, skills, downloaded assets, and user files. Large optional local models/assets should not be embedded in the core APK by default.

## 28. AI Moments / AI 朋友圈
AI Moments and AI 朋友圈 are product-experience ideas for later phases, not reasons to block the current frontend foundation.

## 29. Backend direction
The Android frontend is currently the priority. Python/FastAPI backend work is postponed and should remain optional/future rather than a core dependency of the current Android app.

## 30. Legal / independence / contribution rules
Nexus is explicitly independent from Marvis AI, MyNexusAI, and `https://app.mynexusai.com/`. The project welcomes contributors, requires preservation of applicable notices, and should not copy proprietary code/assets/unpublished material or misrepresent affiliation.

## 31. License direction
Nexus uses GPLv3 as the project license direction. GPLv3 governs distribution of covered code; it does not automatically make a user's private/business/chat data public to the original developer. Contributors and forks remain responsible for applicable copyright, license, and other legal obligations.

## 32. Frontend-first engineering priority
The current engineering priority is: stabilize Android frontend/UI and CI first, then continue Agent Task UI and backend/advanced integrations later. Existing useful historical functionality must not be accidentally deleted during refactoring.

## 33. Current CI status and recovery rule
The project should only declare CI green after the actual GitHub Actions run reports success. The recovery rule is: inspect the actual failing workflow/job/log, fix the concrete error, rerun, and verify success before continuing.

---

## Follow-up development backlog

### 34. Hybrid Cloud / Server File Analysis — PLANNED
Nexus should later support an optional cloud/server analysis path for tasks that benefit from backend compute, such as large files, complex document processing, Python/data analysis, long-running Agent tasks, or larger server-side models.

Design direction:
- **Local-first by default**: ordinary/private files should remain on-device when practical.
- **Explicit user consent** before a file is uploaded to a server.
- Clearly communicate what is uploaded, where it goes, retention/deletion behavior, and relevant privacy implications.
- Use HTTPS and appropriate authentication/authorization.
- Apply file-size/type limits, safe file handling, access control, encryption, and automatic cleanup where appropriate.
- Keep the backend optional rather than making the Android app depend on Python/FastAPI.
- A future Hybrid Agent may route tasks between local Android tools and cloud tools based on capability, size, privacy, and user approval.

Possible future flow:
`User selects file → Nexus explains cloud requirement → user approves → secure upload → server/AI analysis → result → cleanup according to policy`

This is a **future development item**, not part of the current SAF/File Agent v1 implementation.

## Status labels

- **CURRENT** — already present or actively being stabilized.
- **REFACTOR** — historical capability is being redesigned into the Nexus architecture.
- **RECOVER** — useful historical capability should be restored after the foundation is stable.
- **PLANNED** — future product/engineering work.
- **REFERENCE** — retained as design/history context.
- **DEPRECATED** — should not be treated as the current implementation.

## Immediate next step
1. Continue Android SAF → File Agent v1.
2. Keep the current frontend/Agent Core work local-first.
3. After the foundation is stable, evaluate Hybrid Cloud / Server File Analysis as a separate future feature.
4. Keep backend/software-server work postponed unless it becomes necessary for a later feature.
