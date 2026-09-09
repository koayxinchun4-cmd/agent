# Nexus AI — Reference Project Study

> Study notes for public projects that overlap with Nexus AI. This document records architecture ideas and operational lessons; it does not authorize copying proprietary code, assets, branding, or unpublished material.

## Study targets

| Project | Main lesson for Nexus | Study status |
|---|---|---|
| AAswordman/Operit | Android AI Agent product breadth, tools, workspace, local/cloud agent design | STUDY |
| JetBrains/koog | Kotlin Agent Core, tools, retries, memory, provider switching, MCP, workflows, observability | STUDY |
| OpenClawAndroid/openclaw-android-assistant | Native Android packaging of coding-agent workflows and multi-agent UX | STUDY WITH LICENSE/PROVENANCE REVIEW |
| NamashivayamS/Autonomous-CI-CD-Self-Healing-Agent | Failure detection → diagnosis → repair → verification → delivery | STUDY |
| PatilShreyas/debroid | Machine-readable Android runtime debugging for AI agents | STUDY |
| koayxinchun4-cmd/agent | Nexus itself: current architecture, implementation, CI/CD, historical recovery | PRIMARY SELF-STUDY |

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
