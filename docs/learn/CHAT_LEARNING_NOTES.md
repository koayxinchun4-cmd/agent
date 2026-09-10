# Nexus AI — Chat Learning Notes

> Purpose: preserve the learning from the Nexus AI project discussion in this chat, so the project itself can serve as a practical learning environment.

## 1. Learning context

- The project owner is self-learning rather than coming from a dedicated software-development course.
- Main self-study language preference: **English-first**.
- Network security coursework/context is primarily **Chinese-based**.
- Nexus documentation can use Traditional Chinese for explanation while keeping technical terms in English first.
- For technical terms, prefer the pattern: **English term — 繁體中文 explanation**.
- Do not assume prior knowledge of Android, Kotlin, Gradle, GitHub Actions, or AI-agent architecture.

## 2. How Nexus should teach while building

Nexus should be developed as a learning-by-building project rather than requiring all technologies to be mastered before implementation.

Preferred loop:

```text
Nexus task / bug
    ↓
identify the concept needed
    ↓
explain the concept briefly
    ↓
implement the change
    ↓
run tests / CI / security checks
    ↓
explain the result
    ↓
next task
```

The goal is understanding the architecture and reasoning, not blindly copying code.

## 3. Technical areas encountered

### Kotlin
**Kotlin — Android application programming language.**

Learning targets:
- variables and types
- functions
- classes
- interfaces
- inheritance / composition
- collections
- null-safety
- exceptions
- `suspend` functions
- coroutines
- basic generics

### Jetpack Compose
**Jetpack Compose — declarative Android UI toolkit.**

Learning targets:
- `@Composable`
- state
- screen structure
- event callbacks
- ViewModel-driven UI state
- lists / timelines

### Android SDK
**Android SDK — APIs and tools used to build Android applications.**

Learning targets:
- Activity
- Intent
- Package visibility
- permissions
- Storage Access Framework (SAF)
- app-private storage
- lifecycle basics

Safety principle: mobile automation must use public Android APIs and explicit user authorization. No root, private APIs, or hidden automation.

### Gradle / Kotlin DSL
**Gradle — build automation system.**

**Kotlin DSL — Kotlin-based Gradle configuration format.**

Learning targets:
- `build.gradle.kts`
- dependencies
- plugins
- SDK versions
- JVM target
- build / test / lint tasks
- Gradle wrapper

Useful commands:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
./gradlew :app:assembleDebug
```

### Git
**Git — version control system.**

Learning targets:
- repository
- commit
- branch
- checkout / switch
- diff
- push
- merge
- branch cleanup

Preferred project rule:
- one meaningful task → one branch
- merge completed work into `main`
- delete obsolete merged branches
- do not create branches unnecessarily

Example Bash format that should be shown during future work:

```bash
# Check current branch and working tree
git status

# Switch to the task branch
git switch feature/example

# Review the change
git diff

# Commit the change
git add .
git commit -m "fix: describe the bug fix"

# Push the branch
git push origin feature/example
```

The Bash is primarily for visibility and learning when the assistant performs the operation through GitHub tooling; it should be clearly labelled when it is illustrative rather than something the user needs to execute locally.

## 4. GitHub and CI/CD

**GitHub — hosted Git repository and collaboration platform.**

Important concepts:
- repository
- branch
- Pull Request (PR)
- Issue
- review
- Actions workflow

**CI — Continuous Integration（持續整合）.**

CI verifies changes automatically, such as:
- unit tests
- Android lint
- Gradle wrapper validation
- security checks
- APK build when packaging is intentionally enabled

**CD — Continuous Delivery / Deployment（持續交付／部署）.**

Nexus should keep production publication and deployment explicitly controlled. CI can be automated; merging or production deployment should not happen silently.

Preferred flow:

```text
Coding Agent
    ↓
Git branch
    ↓
Pull Request
    ↓
CI + Security
    ↓
AI analyzes failures
    ↓
Fix
    ↓
CI Green
    ↓
Review / controlled merge
    ↓
CD / release when explicitly appropriate
```

## 5. AI / LLM concepts

**LLM — Large Language Model（大型語言模型）.**

Nexus is not only a chat UI. It is being designed around an Agent architecture.

Key concepts:
- Model Provider
- Model Router
- Tool
- Tool Registry
- Agent Planner
- Agent Loop
- Verifier
- Memory
- Skill
- execution state

### Model Provider
A common interface allows Gemini, OpenRouter, and Local models to be treated consistently.

Conceptual structure:

```text
NexusAgent
   ↓
ModelRouter
   ↓
ModelProvider
   ├── Gemini
   ├── OpenRouter
   └── Local
```

### Agent Loop
Nexus Agent Loop concept:

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
  ↓
RETRYING (when needed)
  ↓
COMPLETED / FAILED
```

Other possible waiting states:
- `WAITING_FOR_USER`
- `WAITING_FOR_PERMISSION`
- `WAITING_FOR_TOOL`

## 6. Nexus tools and architecture

A **Tool — 工具** is an executable capability exposed to the Agent.

Examples already encountered:
- Local File Tool
- Memory Tool
- Skills Tool
- App Agent
- GitHub-related tools

A **Tool Registry — 工具註冊表** stores and executes available tools.

A **Repository pattern — Repository 模式** separates application logic from data/network implementation so providers and storage can evolve without rewriting the Agent core.

## 7. Skills and Memory

**Skill — 技能模組** is reusable structured instructions/capability information, typically represented by `SKILL.md`.

Nexus learning points:
- parse Skill documents
- register skills
- select skills for tasks
- keep provenance/license information

**Memory — 記憶系統** stores useful agent/application context.

Nexus currently uses Room/SQLite-oriented storage concepts for chat and memory.

Learning targets:
- database entity
- DAO
- repository
- persistence
- conversation history
- cleanup / archive

## 8. API / networking concepts

Nexus uses HTTP APIs for services such as AI providers.

Important concepts:
- HTTP request / response
- endpoint
- authentication
- API key
- JSON
- timeout
- retry
- error handling

Security rule:
- never commit real API keys
- use GitHub Secrets / environment configuration
- if a real key is exposed, revoke or rotate it

## 9. Bug-fixing workflow learned in this chat

When CI is red, do not guess.

Preferred process:

```text
Check current status
    ↓
identify exact failing workflow
    ↓
identify failing Job
    ↓
identify failing Step
    ↓
inspect logs
    ↓
find root cause
    ↓
make minimal safe fix
    ↓
run CI / Security
    ↓
confirm Green
```

Important distinction:

```text
Application bug
    ≠
Build/CI configuration bug
```

A CI failure can come from workflow configuration even when Kotlin/Android code is correct. The Gradle wrapper validation incident taught us to inspect the exact failing workflow step before changing application code.

## 10. Comment vs common

These two English words are easy to confuse but have different meanings.

### `comment`
**Comment — 註解／留言.**

`comment out` means to disable code temporarily by turning it into a comment instead of deleting it.

Kotlin / Java:

```kotlin
// temporarily disabled
```

YAML / GitHub Actions:

```yaml
# temporarily disabled
```

Important: YAML uses `#` for comments; `//` is not the YAML comment syntax.

### `common`
**Common — 共同的／共用的／常見的.**

Examples:

```text
common code = 共用程式碼
common problem = 常見問題
```

Memory trick:

```text
comment = 💬 留言／註解
common  = 👥 共同／共用
```

## 11. Roadmap execution and APK policy

Nexus uses a Phase 1–7 roadmap. During a full roadmap execution, the priority is **roadmap correctness and Green verification**, not producing an APK on every workflow run.

Preferred sequence:

```text
Phase 1
  ↓ verify
Phase 2
  ↓ verify
Phase 3
  ↓ verify
Phase 4
  ↓ verify
Phase 5
  ↓ verify
Phase 6
  ↓ verify
Phase 7
  ↓ verify
ALL GREEN
  ↓
Build APK
  ↓
Release when explicitly appropriate
```

During roadmap execution, APK packaging steps can be **commented out** rather than deleted. This keeps the configuration recoverable and makes the workflow easier to understand.

This separation reduces CI noise:

```text
Roadmap verification
    ≠
APK packaging
```

The APK is a final packaging deliverable after the roadmap reaches the required Green state, not a mandatory artifact for every roadmap step.

## 12. Workflow restart lesson

When a workflow needs a clean restart, create a dedicated branch from the current clean `main` rather than carrying stale or oversized historical changes into the new run.

Example:

```text
main
  ↓
feature/roadmap-all-no-apk
  ↓
Roadmap ALL execution
```

The restart workflow should:
- run Phase 1–7 sequentially
- verify after each phase
- preserve existing functionality
- keep APK packaging disabled during roadmap execution
- use `#` comments to disable unused workflow steps
- keep security and permission boundaries
- only enable APK packaging after the roadmap is Green

## 13. Reference projects studied

Nexus architecture discussions examined public projects including:
- AAswordman/Operit — Android AI agent platform
- JetBrains/koog — Kotlin AI Agent framework
- OpenClawAndroid/openclaw-android-assistant — Android coding-agent packaging
- NamashivayamS/Autonomous-CI-CD-Self-Healing-Agent — self-healing CI/CD concepts
- PatilShreyas/debroid — headless Android debugging for agents
- Nexus AI itself — primary project to study and build
- WPVibe — WordPress integration example showing Agent tools, permissions, authorization, preview, and publish-oriented workflows

Study principle:

```text
Study concepts
   ≠
copy source code
```

Respect original licenses, attribution, and provenance. Prefer independent reimplementation of useful concepts.

## 14. Coding-language learning map

The project currently touches more technologies than the project owner originally studied. The practical priority is:

1. Kotlin
2. Git / GitHub
3. Android / Compose
4. Gradle
5. HTTP / JSON / APIs
6. Coroutines
7. Room / SQLite
8. AI Agent architecture
9. GitHub Actions / CI/CD
10. Security / secrets

Do not attempt to master all ten before continuing Nexus development.

## 15. Communication format for future Nexus work

For meaningful changes, explain:

```text
🔎 Current status
🎯 Task
📁 Files changed
🧠 Why this change is needed
💻 Bash / Git commands
📚 Technical terms
🧪 Tests / CI
🟢 Result
➡️ Next task
```

Use English-first technical terminology with Traditional Chinese explanations so the documentation matches the owner's English-first self-study background while remaining easy to follow in Traditional Chinese.

## 16. What the project owner does not need to do

The owner does not need to:
- already know professional Android development
- already know Kotlin deeply
- manually reproduce every GitHub operation
- understand every Gradle detail immediately
- learn every AI architecture concept before implementation

The project can be both a working product and a structured learning path.
