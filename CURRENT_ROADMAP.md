# Nexus AI — Current Product Roadmap

> This is the active product roadmap for Nexus AI.
>
> The older Phase 1–7 GitHub Actions roadmap remains useful as a regression / historical verification baseline, but it is **not** the authoritative current product roadmap.

## Product North Star

Nexus AI is a native Android, mobile-first AI Agent that can understand user goals, select models and tools, execute real tasks safely, verify results, and learn through persistent Skills and Memory.

Core constraints:

- Native Android
- Kotlin + Jetpack Compose
- Phone-first
- No Root requirement
- Android public APIs and explicit user permissions
- Gemini plus multi-model routing
- Traditional Chinese (`zh-TW`), English, and Bahasa Melayu
- Security and privacy by default

## Current priorities

### 1. Agent Core Reliability

- Intent understanding
- Better task decomposition
- Planner improvements
- Model routing
- Tool calling
- Verification
- Failure-aware retry / recovery
- User confirmation for risky or irreversible actions

### 2. Mobile Agent

- File Agent using Android-safe storage access
- App Agent using explicit package / Android Intent flows
- Permission-aware execution
- Safe path and input validation
- Clear execution timeline

### 3. Multi-Model Intelligence

- Gemini provider
- OpenRouter / additional providers where appropriate
- Local fallback
- Provider availability detection
- Task-aware model selection
- Consistent model/provider abstraction

### 4. Skills + Memory

- Skill Registry
- Provenance-aware `SKILL.md` import
- Conversation Memory
- User Preferences
- Project Memory
- Task Memory
- Safe persistence with local-first defaults

### 5. Real-World Work Agents

- Web Research Agent: search, source collection, comparison, evidence-backed summaries
- Office Agent: safe document/spreadsheet/presentation workflows
- GitHub Agent: repositories, branches, files, Issues, PRs, Actions and CI/CD
- Coding Agent integration
- CI failure diagnosis and bounded repair loops

### 6. Nexus Agent Studio

- Agent configuration
- Workflow design
- Skill management
- Reusable task templates
- Agent execution history
- Controlled multi-agent collaboration

### 7. Product Experience

- Malaysia-first UX
- English / 繁體中文 / Bahasa Melayu
- Mixed-language input
- Voice as an Agent input/output mode
- Optional AI activity / Moments experience
- Optional community features with strict separation between private data and public sharing

## Delivery gates

1. Implement one small capability.
2. Add or update focused tests.
3. Run GitHub Actions and local validation as appropriate.
4. Confirm behavior against the current product contract.
5. Only then continue to the next capability.
6. APK packaging and Release remain separate delivery steps after the current product acceptance criteria are satisfied.

## Legacy roadmap policy

The legacy `.github/workflows/roadmap.yml` workflow is retained for historical regression evidence. Its Green result must not be interpreted as approval to implement every legacy phase in sequence.

The authoritative source for current direction is this file plus the current architecture/history documentation and the actual behavior of `main`.
