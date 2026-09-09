# Nexus AI — Project History

> Historical record of the Nexus AI project, its architecture evolution, major milestones, engineering decisions, CI/CD work, and reference studies.
>
> **Project:** Nexus AI
> **Repository:** https://github.com/koayxinchun4-cmd/agent
> **License:** GNU General Public License v3.0 (GPLv3)
> **Status:** Active development

## 1. Project Identity

Nexus AI is an independent native Android AI Agent project built around Android, Kotlin, Jetpack Compose, and model/tool integration.

The project is intended to evolve from a simple mobile AI assistant into a practical mobile AI Agent platform with:

- AI conversation and task execution
- Tool calling and Agent planning
- Multi-model routing
- Local fallback execution
- Android App and File abilities
- Skills and persistent Memory
- GitHub and CI/CD integration
- Web research
- Voice and Android integrations
- Future AI Moments / AI 朋友圈 experiences
- Community contributions and extensibility

Nexus AI is independent from Marvis AI, MyNexusAI, and their operators or developers. Public projects may be studied for architecture and engineering ideas, but proprietary code, assets, branding, unpublished technology, and other protected material are not treated as Nexus AI original work.

## 2. Engineering Principles

The project gradually adopted several long-term principles:

1. **Native Android first** — Kotlin + Jetpack Compose.
2. **No root requirement** — Android platform APIs and explicit user permissions are preferred.
3. **Compatibility over destructive rewrites** — useful historical functionality should be preserved when architecture changes.
4. **Agent-first architecture** — tasks, plans, tools, models, verification, memory, and skills are treated as separate building blocks.
5. **Small-step development** — prefer one small task → CI → Green → next task, instead of changing many systems at once.
6. **Security by default** — never commit API keys, tokens, private credentials, or personal data.
7. **Bounded automation** — AI repair loops must have explicit limits and must not silently merge or deploy production changes.
8. **Study ≠ Copy** — reference projects are studied, attributed, and reimplemented conceptually rather than copied without a clear license basis.
9. **English-first technical naming** — technical/scientific names remain English-first with Traditional Chinese explanations where appropriate.
10. **Traditional Chinese UI** — Chinese UI/documentation uses Traditional Chinese (`zh-TW`), not Simplified Chinese.

## 3. Early Foundation

The original application started as an Android AI assistant centered on Google Gemini API integration.

The early foundation established:

- Android application structure
- Kotlin
- Jetpack Compose / Material 3
- Room persistence
- Retrofit / OkHttp / Moshi networking
- Gemini AI integration
- Local fallback behavior when no valid API key is available
- Initial Home / Chat / More navigation
- Settings and language direction

The project later expanded the original assistant concept into an Agent architecture instead of remaining a conventional chat application.

## 4. AI Engine and Model Evolution

### Initial AI architecture

The project originally used AI engine abstractions around Gemini and OpenRouter, including a hybrid repository and local autonomous fallback behavior.

Historical goals included:

- Gemini as a primary cloud model
- OpenRouter as an additional model route
- Local deterministic/autonomous fallback
- `openrouter/auto` as an OpenRouter default
- Specialist coding / CTO-style behavior

### Model Provider layer

The architecture was later refactored toward explicit model providers.

Major concepts introduced:

- `ModelProvider`
- `ModelResponse`
- `ModelProviderRegistry`
- `LocalModelProvider`
- `ModelRoute`
- `ModelRouter`

This created a foundation for selecting between Gemini, OpenRouter, and Local execution without coupling Agent logic to a single provider.

A compile issue in the provider registry was corrected by deriving available routes from registered provider values rather than an incorrect `providers` reference.

The Model Provider work was merged through PR #9 with commit:

`52d5d31ef6e5059dd32b460923474643735e9786`

## 5. Agent Core

The project then moved toward a real Agent Loop.

Core abstractions include:

- `AgentTask`
- `AgentResult`
- `AgentTool`
- `ToolRegistry`
- `AgentPlan`
- `AgentPlanner`
- `AgentVerifier`
- `NexusAgent`
- `AgentExecution`
- `AgentStepResult`
- `AgentLoopConfig`
- `AgentProgress`

The basic execution model became:

```text
User Task
   ↓
Understand
   ↓
Plan
   ↓
Route Model
   ↓
Execute Tool / Provider
   ↓
Verify
   ↓
Retry when appropriate
   ↓
Completed / Failed
```

The longer-term state machine is:

```text
CREATED
  ↓
UNDERSTANDING
  ↓
PLANNING
  ↓
EXECUTING
  ↓
VERIFYING
  ├──→ RETRYING → EXECUTING
  └──→ COMPLETED / FAILED

WAITING_FOR_USER
WAITING_FOR_PERMISSION
WAITING_FOR_TOOL
```

The first Agent Loop implementation supports bounded retry and verification. Full failure-aware re-planning remains a future enhancement because the current planner does not yet select a new strategy from failure context.

## 6. Agent Task UI

A dedicated Agent Task screen was introduced so Nexus could execute tasks instead of only displaying chat messages.

Major milestones included:

- Agent Task v1 screen
- Home → Start a task navigation
- Task input
- Execution workflow timeline
- Stop / modify task actions
- Live progress callback
- ViewModel progress state
- Live Agent Task execution timeline

Important commits included:

- `74f492c` — `feat: add Nexus Agent Task v1 screen`
- `49f12be6...` — `feat: open Agent Task from home`
- `860763...` — progress callback
- `6753b...` — TaskViewModel progress state
- `e218fb9df8c6667d6f966009163a070e0b340544` — live timeline

The live Agent Task implementation passed GitHub Actions Run #107.

## 7. Local File Agent

Nexus gained a native local file capability using the app-private `filesDir` area.

Design goals:

- Safe relative path resolution
- Directory traversal protection
- File listing
- File reading
- Unit tests for normal access and traversal rejection

This was integrated into the Agent tool architecture and planner.

## 8. Android App Agent

The App Agent was implemented as a native Android capability.

It can launch an explicitly requested package using Android Intent APIs.

Security/design boundaries:

- Explicit package name is required
- Android decides whether a launchable activity exists
- No root
- No hidden/private Android APIs
- No unauthorized Accessibility automation
- No hidden app discovery
- No arbitrary background control

Chinese app-launch matching and Android Package Visibility behavior were subsequently corrected.

Important fixes included:

- `22a2e3682916bafb46d7a720cfab96079d9b628f`
- `db11dc2fa6d311de9b191616a58cd321daddd6b8`

App Agent work was merged through PR #7 with merge commit:

`866219ad89c8d97e01d9ed799ee08d71c41fd19d`

CI Run #130 completed successfully.

## 9. Skills and Memory

The Agent architecture was expanded with two important extensibility systems.

### Skills

Nexus introduced:

- `SKILL.md` parsing
- Skill Registry
- Skills Tool
- GitHub skill import/extraction direction

The project explicitly adopted a provenance-aware Skills policy so third-party skills are not silently treated as Nexus original content.

### Memory

Nexus introduced persistent Agent Memory using Room-based storage.

Key concepts include:

- `ChatSession`
- `AgentMemory`
- Memory Tool
- conversation persistence
- historical conversation handling

The Agent Task + Memory + Skills integration was brought into main through PR #12.

PR #12 was eventually merged with:

`cbe8e691c4dfe4769ca96ec5935fd83caedd937b`

## 10. Regression Testing and CI Lessons

As the Agent architecture evolved, an important CI lesson appeared: tests must follow the current behavioral contract rather than historical UI strings.

Two `AgentLoopTest` assertions failed because the tests still expected old Chinese retry text while the current implementation emitted English verification messages such as:

- `retrying`
- `maximum attempts reached`

The fix was intentionally kept minimal: production code was not changed; only the stale test expectations were aligned.

PR #15:

`test: align AgentLoop retry assertions`

Result:

- Android CI #146 — Green
- Security #10 — Green
- PR #15 — merged

Merge commit:

`0a5b6f7f5ded24bd3baf80ab5d19a2b1804b70f5`

This reinforced the project's preferred workflow:

```text
Detect failure
   ↓
Find exact root cause
   ↓
Make the smallest compatible fix
   ↓
Run CI
   ↓
Green
   ↓
Continue
```

## 11. Complete Android CI and Security

PR #10 established a more complete GitHub Actions validation suite.

The CI/security direction includes:

- Android Build
- Unit Test
- Lint
- Debug APK packaging
- APK artifact upload
- Gradle Wrapper Validation
- CodeQL / security checks
- GitHub Pages deployment workflow

The project also added an Aider workflow for repository-level Issue/PR solving.

The Android CI workflow uses GitHub Actions and supports automatic validation on `main` pushes and Pull Requests targeting `main`.

The repository learned that concurrency settings such as `cancel-in-progress` can cause older runs to be cancelled when newer runs start, so CI status must always be interpreted against the latest relevant commit.

## 12. Coding Agent Backends

Nexus treats external coding systems as potential backends rather than as the Nexus Agent itself.

### Aider

Aider is the currently confirmed repository-integrated coding backend.

Current workflow behavior:

```text
GitHub Issue / PR comment
        ↓
      @aider
        ↓
Aider analyzes repository
        ↓
Aider modifies code
        ↓
Commit
        ↓
CI
```

The workflow is designed for settling repository issues and can be used for focused fixes.

### cto.new

The cto.new GitHub App is installed on the account, but repository-level execution/configuration has not been confirmed as an active workflow in this repository.

### Codex

Codex can be treated as another possible engineering/review backend, but external availability and usage limits must be considered.

## 13. CI/CD and Self-Healing Direction

The project distinguishes:

- **CI — Continuous Integration（持續整合）**: build, test, lint, security, and validation.
- **CD — Continuous Delivery / Deployment（持續交付／部署）**: artifacts, releases, delivery, or deployment.

The intended engineering pipeline is:

```text
Issue
 ↓
Nexus Agent
 ↓
Coding Backend
(Aider / Codex / other)
 ↓
Git Commit
 ↓
CI
 ↓
Failure?
 ├── Yes → AI diagnosis → minimal repair → CI again
 └── No
 ↓
Green
 ↓
CD
```

The project explicitly prefers bounded self-healing instead of an infinite autonomous loop.

Production merge/deployment must remain explicitly controlled and should not silently happen as a side effect of AI repair.

## 14. Roadmap Auto-Execution

PR #11 introduced the direction for running Nexus roadmap work through GitHub Actions.

The intended scope is Phase 1 through Phase 7:

1. Nexus Foundation
2. Real Agent
3. Mobile Agent
4. Skills + Memory
5. Web / Office / GitHub
6. AI Moments / AI 朋友圈
7. Advanced multi-model, local assets, and agent collaboration

The desired behavior is:

```text
Existing Nexus functionality
        ↓
Regression Tests
        ↓
Phase task
        ↓
CI
        ↓
If failed:
AI analyzes failure
        ↓
Minimal fix
        ↓
CI again
        ↓
Green
        ↓
Next task
```

PR #11 remains a roadmap/automation workstream and should be validated incrementally rather than executing the entire roadmap in one large change.

## 15. Reference Project Study

Nexus development includes structured study of public projects with similar goals.

Primary references studied include:

### Operit

Repository:
https://github.com/AAswordman/Operit

Study topics:

- Android AI Agent architecture
- Tools
- Workspace
- Skills
- Memory
- Local/cloud models
- MCP

License observed during study: LGPL-3.0.

### Koog

Repository:
https://github.com/JetBrains/koog

Study topics:

- Kotlin Agent framework
- Tool architecture
- Retry / fault tolerance
- Persistence
- History compression
- LLM switching
- MCP
- Memory / RAG
- Streaming
- Tracing / observability

License observed during study: Apache-2.0.

### OpenClaw Android / AnyClaw direction

Study topics:

- Android packaging for coding agents
- Local execution environments
- Multi-agent coding

License observed for the studied OpenClaw Android repository: MIT, with third-party notices.

### Autonomous Self-Healing Agent

Study topics:

- Failure detection
- Memory / RAG
- Diagnosis
- Solver
- Sandbox verification
- PR / deployment
- Rollback

The project is particularly relevant to Nexus CI self-healing design.

### Debroid

Study topics:

- Android debugging for AI agents
- Machine-readable debugging
- Breakpoints
- Exceptions
- Variables
- Call frames
- Stepping

License observed during study: Apache-2.0.

### Nexus AI itself

The Nexus repository is also a **PRIMARY SELF-STUDY** target.

The project is continuously reviewed in the same categories as reference repositories:

- Source architecture
- Build system
- CI
- Runtime behavior
- Tests
- Security
- Documentation
- License/provenance

## 16. Study / Fork Policy

Reference study follows this rule:

> **Study ≠ Copy**

A GitHub Fork is optional and can preserve GitHub's fork relationship, but a Fork is not a license to remove attribution or ignore the original project's terms.

When a reference project is used for study:

- Keep the original repository URL.
- Record the study commit/ref when practical.
- Record the license.
- Preserve required attribution and notices.
- Clearly identify original project code versus Nexus-native implementation.
- Prefer reimplementing concepts inside Nexus instead of copying source code unnecessarily.
- Respect contributor and original-author requests when they are reasonable and supported by the applicable license/provenance rules.

## 17. Historical Feature Preservation

Useful functionality from earlier versions is considered part of Nexus's engineering history and should not be deleted merely because the architecture is being rewritten.

Historical capabilities that remain tracked include:

- Gemini / OpenRouter / Hybrid AI
- Local Autonomous Engine and presets
- CTO.new / coding assistant concepts
- Office Agent Studio concepts
- GitHub repositories / branches / files / Issues / PRs
- GitHub Actions / CI/CD
- Skills parsing and registry
- Memory and conversation persistence
- App Agent
- Local File Agent
- Settings and language selection
- Future Web Research
- Voice
- AI Moments
- AI 朋友圈
- WhatsApp automation concepts

Some historical capabilities, especially WhatsApp integration, were partially removed during security/bug cleanup and are classified as historical/recovery/future work rather than assumed to be currently active.

## 18. Product and UI Evolution

The UI evolved from a simple AI assistant toward an Agent workspace.

Current high-level navigation:

```text
HOME
CHAT
MORE
```

The Home experience includes:

- Nexus AI identity
- Personal AI Agent positioning
- Start a task
- Quick Actions
- Agent capabilities

The Chat experience includes:

- Conversation history
- Message bubbles
- Suggestion chips
- Input/send composer
- Auto-scroll
- Clear history

The Agent Task experience adds an execution timeline and progress state so users can observe what the Agent is doing.

## 19. Language Direction

Official application language options are:

- English
- 繁體中文 / Traditional Chinese
- Bahasa Melayu / Malay

Chinese localization uses `zh-TW`.

Technical names, code identifiers, repository paths, and scientific terms remain English-first; Traditional Chinese explanations are added when useful for beginners.

## 20. Security and Data Principles

Nexus must not embed real secrets in source control.

Examples of protected material:

- Gemini API keys
- GitHub tokens
- OAuth credentials
- WhatsApp credentials
- private user data
- personal files

`.env` is ignored and `.env.example` contains placeholders only.

Independent forks or applications may operate independently. Their user data, chats, files, business data, content, and operational behavior do not automatically become Nexus AI project data simply because Nexus code is used.

## 21. Current Development Method

The project currently favors incremental execution.

Preferred loop:

```text
Check current status
        ↓
Choose ONE smallest useful task
        ↓
Implement
        ↓
Run CI / Security
        ↓
Equal?
 ├── Yes → Green → Continue
 ├── No → Diagnose → Fix
 └── Unknown → Check actual GitHub state
```

Status terminology:

- **Equal / 相同：✅**
- **Not equal / 不相同：❌**
- **Unknown / 尚未確認：🟡**
- **Green：🟢**
- **In progress：🟡**
- **Failed：🔴**

When continuing work, the latest PR, CI, Security, mergeability, and `main` state should be checked before choosing the next task.

## 22. Major Verified Milestones

| Milestone | Result |
|---|---|
| Android / Compose foundation | 🟢 |
| Gemini integration | 🟢 |
| Local fallback | 🟢 |
| Agent Core | 🟢 |
| Agent Loop | 🟢 |
| Agent Task UI | 🟢 |
| Local File Agent | 🟢 |
| App Agent | 🟢 |
| Model Provider layer | 🟢 |
| Memory | 🟢 |
| Skills | 🟢 |
| Android CI | 🟢 |
| Security checks | 🟢 baseline |
| Aider coding workflow | 🟢 integrated |
| Roadmap auto-execution | 🟡 workstream |
| Self-healing CI | 🟡 evolving |
| Reference project study | 🟡 ongoing |
| Web Research | 🟡 planned/evolving |
| Voice | 🟡 planned/evolving |
| AI Moments | 🟡 planned |
| AI 朋友圈 | 🟡 planned |

## 23. Current Direction

Nexus AI is moving toward a layered mobile Agent architecture:

```text
┌──────────────────────────────┐
│           Nexus UI           │
├──────────────────────────────┤
│       Agent Task Layer       │
├──────────────────────────────┤
│ Planner / Router / Verifier  │
├──────────────────────────────┤
│ Models / Providers           │
├──────────────────────────────┤
│ Tools / Skills / Memory      │
├──────────────────────────────┤
│ Android Platform Abilities   │
├──────────────────────────────┤
│ GitHub / Web / CI/CD         │
└──────────────────────────────┘
```

The long-term objective is not simply to make Nexus answer questions. It is to make Nexus capable of understanding a task, planning work, selecting an appropriate model/backend, using tools, verifying results, recovering from bounded failures, remembering useful context, and safely completing practical Android tasks.

## 24. Record Maintenance

This file is a historical record, not a promise that every listed capability is currently enabled.

For current implementation truth, prefer:

- repository source code
- current GitHub Actions results
- current Pull Requests
- `docs/NEXUS_PROJECT_MEMORY.md`
- project roadmap/status documents

When a major milestone is completed, update this file with:

- date or commit/ref
- feature/workstream
- important implementation decision
- CI/security result
- relevant PR or commit
- known limitations

---

**Nexus AI — build small, verify continuously, preserve history, and evolve toward a safe mobile Agent.**
