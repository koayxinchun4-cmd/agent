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

- [x] Intent understanding
- [x] Better task decomposition
- [x] Planner improvements
- [x] Model routing
- [x] Tool calling
- [x] Verification
- [x] Failure-aware retry / recovery
- [x] User confirmation for risky or irreversible actions
- [x] Sequential execution of decomposed tool-backed subtasks
- [x] Verified observation accumulation and final answer synthesis
- [ ] Dynamic re-planning after a failed or partially completed subtask
- [ ] Explicit execution-state machine for Plan → Act → Observe → Diagnose → Replan

### 2. Mobile Agent

- [ ] File Agent using Android-safe storage access
- [ ] App Agent using explicit package / Android Intent flows
- [ ] Permission-aware execution
- [ ] Safe path and input validation
- [ ] Clear execution timeline

### 3. Multi-Model Intelligence

- [x] Gemini provider
- [ ] OpenRouter / additional providers where appropriate
- [x] Local fallback
- [x] Provider availability detection
- [x] Task-aware model selection
- [x] Consistent model/provider abstraction
- [ ] Model-aware context budgeting and compression

### 4. Skills + Memory

- [ ] Skill Registry
- [ ] Provenance-aware `SKILL.md` import
- [x] Conversation Memory
- [ ] User Preferences
- [x] Project Memory
- [x] Task Memory
- [x] Safe persistence with local-first defaults

### 5. Real-World Work Agents

- [ ] Web Research Agent: search, source collection, comparison, evidence-backed summaries
- [ ] Office Agent: safe document/spreadsheet/presentation workflows
- [x] GitHub Agent: repositories, branches, files, Issues, PRs, Actions and CI/CD
- [ ] Coding Agent workspace engine: repository exploration, file search/read/edit, diff generation
- [ ] Terminal / shell execution with bounded permissions
- [ ] Coding loop: Plan → Edit → Test → Diagnose → Re-edit → Re-test
- [ ] CI failure diagnosis and bounded repair loops
- [ ] Patch validation and rollback support

### 6. Nexus Agent Studio

- [ ] Agent configuration
- [ ] Workflow design
- [ ] Skill management
- [ ] Reusable task templates
- [ ] Agent execution history
- [ ] Controlled multi-agent collaboration

### 7. Product Experience

- [ ] Malaysia-first UX
- [x] English / 繁體中文 / Bahasa Melayu support foundations
- [ ] Mixed-language input
- [ ] Voice as an Agent input/output mode
- [ ] Optional AI activity / Moments experience
- [ ] Optional community features with strict separation between private data and public sharing

## Delivery gates

1. Implement one small capability.
2. Add or update focused tests.
3. Run GitHub Actions and local validation as appropriate.
4. Confirm behavior against the current product contract.
5. Only then continue to the next capability.
6. APK packaging and Release remain separate delivery steps after the current product acceptance criteria are satisfied.

## Current milestone: Agent Core → Coding Agent foundation

The multi-step execution milestone is implemented on PR #51. The next engineering milestone is to turn the existing Agent Core into a coding-capable workspace loop without bypassing the current safety, verification, confirmation, memory, and model-routing layers.

### Next implementation order

1. **Workspace abstraction** — safe repository/file read, search, write, diff, and patch operations.
2. **Terminal abstraction** — bounded command execution with explicit permissions, timeouts, output limits, and cancellation.
3. **Coding execution loop** — connect planner, workspace, terminal, verifier, recovery, and model provider into one observable loop.
4. **Context engine** — select relevant files, tests, diagnostics, and prior observations without flooding model context.
5. **Repair loop** — consume compiler/test/lint failures, diagnose root causes, make bounded edits, and re-run validation.
6. **Safety + rollback** — preview destructive changes, preserve diffs, support rollback, and require confirmation for risky actions.

## Legacy roadmap policy

The legacy `.github/workflows/roadmap.yml` workflow is retained for historical regression evidence. Its Green result must not be interpreted as approval to implement every legacy phase in sequence.

The authoritative source for current direction is this file plus the current architecture/history documentation and the actual behavior of `main`.
