# Nexus AI — Reference Project Study

> Study notes for public projects that overlap with Nexus AI. This document records architecture ideas and operational lessons; it does not authorize copying proprietary code, assets, branding, or unpublished material.

## Study targets

| Project | Main lesson for Nexus | Study status | Source | License observed |
|---|---|---|---|---|
| AAswordman/Operit | Android AI Agent product breadth, tools, workspace, local/cloud agent design | STUDY | https://github.com/AAswordman/Operit | LGPL-3.0 text observed in `LICENSE` |
| JetBrains/koog | Kotlin Agent Core, tools, retries, memory, provider switching, MCP, workflows, observability | STUDY | https://github.com/JetBrains/koog | Apache-2.0 text observed in `LICENSE.txt` |
| OpenClawAndroid/openclaw-android-assistant | Native Android packaging of coding-agent workflows and multi-agent UX | STUDY WITH LICENSE/PROVENANCE REVIEW | https://github.com/OpenClawAndroid/openclaw-android-assistant | MIT text observed in `LICENSE`; third-party notices referenced |
| NamashivayamS/Autonomous-CI-CD-Self-Healing-Agent | Failure detection → diagnosis → repair → verification → delivery | STUDY | https://github.com/NamashivayamS/Autonomous-CI-CD-Self-Healing-Agent | License file not confirmed in this study pass |
| PatilShreyas/debroid | Machine-readable Android runtime debugging for AI agents | STUDY | https://github.com/PatilShreyas/debroid | Apache-2.0 text observed in `LICENSE` |
| koayxinchun4-cmd/agent | Nexus itself: current architecture, implementation, CI/CD, historical recovery | PRIMARY SELF-STUDY | https://github.com/koayxinchun4-cmd/agent | GPLv3 project policy |

## Sources and evidence

Important study claims should point back to the original public source. For the current pass, the license evidence checked directly was:

- Operit `LICENSE`: LGPL v3 terms are present. Source: https://github.com/AAswordman/Operit/blob/main/LICENSE
- Koog `LICENSE.txt`: Apache License 2.0 terms are present. Source: https://github.com/JetBrains/koog/blob/main/LICENSE.txt
- OpenClaw Android `LICENSE`: MIT License is present and the file also points readers to `THIRD_PARTY_NOTICES.md`. Source: https://github.com/OpenClawAndroid/openclaw-android-assistant/blob/main/LICENSE
- Debroid `LICENSE`: Apache License 2.0 terms are present. Source: https://github.com/PatilShreyas/debroid/blob/main/LICENSE
- Self-Healing Agent README: the public README describes its self-healing workflow and setup, but this pass did not confirm a repository license file. Source: https://github.com/NamashivayamS/Autonomous-CI-CD-Self-Healing-Agent/blob/main/README.md

A source link is evidence of where the observation came from; it is not permission to copy code beyond the applicable license and other rights.

## Fork / Study Policy

### Study is not Copy & Paste

A GitHub **Fork** preserves a visible relationship to the upstream repository. It is useful when Nexus wants to:

- build or modify the public project for experiments
- run the project's own tests/CI in a separate repository namespace
- keep a reproducible study snapshot
- prepare changes that may later become an upstream PR

A Fork is **not** treated as a Nexus-original implementation.

Preferred pattern:

```text
Original project
      ↓ GitHub Fork
Nexus study fork
      ↓
Build / CI / controlled experiments
      ↓
Architecture notes + source attribution
      ↓
Nexus-native reimplementation of useful ideas
```

Do not merely download a public repository, rename it, remove attribution, and present it as Nexus code.

### Recommended fork naming

Use a name that makes its study purpose obvious, for example:

- `Operit-study`
- `koog-study`
- `openclaw-android-study`
- `self-healing-agent-study`
- `debroid-study`

The upstream project and original authors must remain clearly identified in the fork README and/or NOTICE documentation. The exact final repository name is less important than preserving the upstream Fork relationship and attribution.

### When a Fork is not necessary

For source reading, architecture comparison, README study, license review, and non-modifying CI/build observation, Nexus can study the original repository directly without creating a Fork.

### When Fork is useful

Create a study Fork before making experimental modifications to the third-party repository itself, unless the upstream project's license or repository policy says otherwise.

### Notifications

A Fork creates a GitHub relationship between repositories, but Nexus must not claim that a Fork guarantees an upstream author notification. Author visibility depends on GitHub notification settings and the events involved.

## Cross-project architecture map

```text
Android AI Agent
      │
      ├── Agent Core
      │     ├── Planner
      │     ├── Router
      │     ├── Tools
      │     ├── Memory
      │     └── Verification / Retry
      │
      ├── Android capabilities
      │     ├── Files
      │     ├── Apps
      │     ├── Web
      │     └── Voice / system integrations
      │
      ├── Coding backend
      │     ├── Aider
      │     ├── cto.new
      │     ├── Codex
      │     └── future providers
      │
      └── Engineering loop
            ├── GitHub Issue / task
            ├── code change
            ├── CI
            ├── AI repair on failure
            └── CD / artifact / release
```

## Operit — Android Agent Product Study

Focus areas:
- Android-first Agent UX
- Tool-driven task execution
- Workspace/project concepts
- Skills and MCP integration
- Memory and long-running tasks
- Local/cloud model choices
- Permission boundaries

Nexus lesson:
- Keep Android capabilities modular.
- Treat tools, Skills, memory, and model providers as composable systems.
- Make Agent Task progress visible without exposing private chain-of-thought.

Do not copy UI, assets, code, or proprietary implementation details.

## Koog — Kotlin Agent Core Study

Koog is a Kotlin-based AI Agent framework. Its public documentation highlights:
- tools and custom tool creation
- retries and fault tolerance
- persistence
- history compression
- LLM switching / multiple providers
- MCP
- memory / RAG
- streaming
- graph workflows
- tracing / observability

Nexus lesson:
- The current `AgentTool`, `ToolRegistry`, `AgentPlanner`, `ModelRouter`, `NexusAgent`, verifier, progress events, and provider abstraction should evolve toward stronger separation of concerns.
- Retry should preserve useful execution state and produce auditable status.
- Provider switching should not destroy conversation/task context.
- Future complex tasks may benefit from graph/state-machine execution rather than only linear planning.

## AnyClaw / OpenClaw Android — Mobile Coding Agent Study

Focus areas:
- putting coding agents on Android
- multi-agent selection
- embedded/local execution environments
- streaming coding conversations
- dashboard/session UX

Nexus lesson:
- Mobile coding workflows are possible without making Android itself the entire development environment.
- Keep execution boundaries explicit and permission-aware.
- Avoid adopting legally questionable third-party material merely because it is publicly visible.

## Autonomous CI/CD Self-Healing Agent — Self-Healing Study

Public project describes a pipeline of:

`failure detection → memory/RAG → diagnosis → solver → sandbox verification → PR/deployment → rollback`

Nexus lesson:
- Separate diagnosis, repair, and verification.
- Feed exact CI evidence into the repair agent rather than vague failure summaries.
- Keep repair attempts bounded.
- Preserve rollback/recovery as an explicit engineering capability.
- Do not equate a successful test with permission to deploy production.

## Debroid — Android Runtime Debugging Study

Debroid exposes Android/JDWP debugging capabilities through machine-readable CLI output so AI agents can inspect runtime state.

Nexus lesson:
- Future Android coding agents need runtime evidence, not only source-code inspection.
- A machine-readable debugging interface can become a future `AndroidDebugTool` backend.
- Runtime debugging must remain permission-gated and avoid bypassing Android security boundaries.

## Nexus Primary Self-Study

The Nexus repository is itself a study target and the primary implementation source.

Current areas to inspect continuously:

1. **Agent Core** — planner, router, tools, verifier, progress, retry.
2. **Task UX** — visible execution timeline and actionable results.
3. **Model Provider** — Gemini/OpenRouter/local abstraction.
4. **File Agent** — safe app-private file access and traversal protection.
5. **App Agent** — explicit package-based launch behavior.
6. **Skills** — parser, registry, provenance, security/license gate.
7. **Memory** — Room-backed conversation/task memory.
8. **GitHub** — repository, files, Issues, PRs, comments, account integration.
9. **CI/CD** — Android build/test/lint/security/artifacts and future controlled delivery.
10. **Coding backends** — confirmed Aider integration plus future provider-agnostic cto.new/Codex options.

## Start / Run Study

Where feasible, a study target should be started or its CI/build system exercised rather than studied only from README text.

Record:
- repository and commit/ref
- documented prerequisites
- startup/build command
- actual result
- blockers
- relevant architecture findings
- license/provenance considerations

For Nexus itself, prefer the real GitHub Actions runs and repository configuration as the source of truth for CI status. A README or old chat message is not proof that a current build passes.

## Learning Rules

- Study public architecture and behavior; do not copy proprietary implementation.
- Check the repository license before reusing code.
- Preserve attribution and notices when legally required.
- Separate observation from inference.
- Verify important claims against source code/workflows where possible.
- Convert useful lessons into Nexus-native designs and tests.
- Keep existing useful Nexus functionality during refactoring.
