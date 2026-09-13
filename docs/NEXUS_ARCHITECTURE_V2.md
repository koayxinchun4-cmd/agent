# Nexus AI — Architecture v2

> Architecture proposal and foundation implementation for the next generation of Nexus AI.
>
> This document turns the current product roadmap into an implementation-oriented Agent architecture while preserving Nexus's defining constraint: **the phone is the Agent's primary execution environment**.

## Status

Architecture v2 is now backed by a first implementation slice on this branch. The branch adds the core boundaries without requiring a product-wide rewrite:

- explicit `AgentSession` / `AgentTurn` lifecycle primitives;
- explicit runtime states and structured execution events;
- bounded `AgentObservation` context;
- policy-aware `ToolRuntime` with Android permission and confirmation checks;
- session-scoped tool approvals;
- cancellation-safe tool execution;
- Android-safe app-private `MobileWorkspace` path boundary;
- multi-agent specialist delegation and conservative re-planning contracts;
- resumable execution checkpoint interface with an in-memory implementation;
- existing `NexusAgent` wired through the common Tool Runtime.

This is intentionally additive so the existing Agent contracts remain usable while the runtime boundary becomes stronger.

## 1. North Star

Nexus is a native Android, mobile-first AI Agent that can understand a user's goal, plan work, select models and tools, execute real tasks safely, verify outcomes, recover from failures, and improve through Skills and Memory.

Nexus is **not** a PC coding agent moved onto Android. Coding is one work capability among many.

The architecture therefore prioritizes:

- Native Android / Kotlin / Jetpack Compose
- No Root requirement
- Android public APIs and explicit user permissions
- Mobile-first execution and UX
- Multi-agent collaboration
- Model/provider independence
- Verification and recovery as first-class behavior
- Security, privacy, cancellation, and confirmation by default

## 2. Architecture layers

```text
User
  |
  v
Nexus Product UI
  |  Chat / Agent Task / Timeline / Voice / Studio
  v
Agent Orchestrator
  |  intent -> plan -> delegate -> execute -> verify -> recover
  +-----------------------+
  |                       |
  v                       v
Agent Team            Context Manager
  |                       |
  +-----------+-----------+
              v
        Tool Runtime
              |
    +---------+---------+------------------+
    |         |         |        |         |
  Web      Files      Apps     Office    GitHub
    |         |         |        |         |
    +---------+---------+--------+---------+
              |
              v
      Android Capability Layer
   APIs / Intents / SAF / permissions
              |
              v
       Observation / Result
              |
              v
          Verifier
              |
       success / retry / re-plan
```

### Layer A — Product

Owns user interaction and presentation, not execution policy.

- Chat
- Agent Task
- Execution timeline
- Confirmation dialogs
- Voice input/output
- Agent Studio

### Layer B — Agent Core

Owns task lifecycle and orchestration.

Implemented primitives now include:

- `AgentSession`
- `AgentTurn`
- `AgentTask`
- `AgentPlan`
- `AgentExecution`
- `AgentObservation`
- `AgentResult`
- `AgentRuntimeState`
- `AgentEvent`
- `AgentCheckpointStore`
- `AgentVerifier`
- `AgentRecovery`

Lifecycle:

```text
CREATED
  -> UNDERSTANDING
  -> PLANNING
  -> EXECUTING
  -> VERIFYING
  -> COMPLETED

EXECUTING / VERIFYING
  -> RETRYING
  -> RE-PLANNING
  -> EXECUTING

Any state
  -> WAITING_FOR_PERMISSION
  -> WAITING_FOR_CONFIRMATION
  -> FAILED / CANCELLED
```

The important change from a simple sequential executor is that **failure becomes structured input to recovery and re-planning**, rather than only an error string.

### Layer C — Agent Team Runtime

Agents are specialists, not independent applications.

Example roles:

- Planner Agent
- Research Agent
- File Agent
- Android/App Agent
- Office Agent
- GitHub Agent
- Coding Agent
- Verifier Agent

`SpecialistAgent` and `MultiAgentCoordinator` now provide the delegation boundary. Delegation must carry objective, constraints, required tools, permission scope, relevant context, expected output, and verification criteria at the higher orchestration layer.

The coordinator owns the final result. Specialist agents must not silently expand permissions or task scope.

### Layer D — Tool Runtime

Tools are executed through a common runtime instead of each Agent implementing its own permission and failure logic.

The branch now provides:

```kotlin
interface ToolRuntime {
    suspend fun execute(
        sessionId: String,
        task: AgentTask,
        tool: AgentTool,
        confirmationGranted: Boolean = false,
        grantedPermissions: Set<String> = emptySet()
    ): ToolRuntimeResult
}
```

The runtime centrally handles:

- Android permission checks
- confirmation checks
- session-scoped approval caching
- coroutine cancellation propagation
- structured tool failures
- a single policy boundary before tool execution

### Layer E — Mobile Capability Layer

This is where Nexus differs fundamentally from PC-first coding agents.

Use Android-native capabilities:

- Storage Access Framework
- app-private files
- Android Intents
- PackageManager where appropriate
- notifications
- network APIs
- foreground/background execution within Android rules
- explicitly authorized accessibility flows where product policy permits

Never make Root, shell access, hidden APIs, or unrestricted filesystem access a hidden prerequisite.

## 3. Workspace model

Nexus needs a concept similar to a coding-agent workspace, but generalized for mobile.

```text
NexusWorkspace
  ├─ scope
  ├─ permissions
  ├─ resources
  ├─ task state
  └─ observations
```

The first implementation is `MobileWorkspace` plus `AppPrivateWorkspace`, with canonical-path traversal protection. A future SAF implementation can satisfy the same interface without changing the Agent Core.

A workspace may represent:

- an app-private project
- a user-selected SAF directory
- a document set
- a GitHub repository
- a research session
- an Office task

Each workspace explicitly declares its access boundary.

This lets Nexus reuse the useful idea behind coding-agent workspaces without assuming a Unix filesystem.

## 4. Approval and permissions

Borrow the separation between **tool execution** and **approval/sandbox policy** used by modern coding agents.

Codex's runtime separates approval decisions from tool execution and supports cached session approvals and explicit forbidden states. Nexus adopts the architectural separation, but translates it to Android permissions rather than shell sandboxing.

Current Nexus policy building blocks are:

```text
ALLOW
ALLOW_FOR_SESSION
NEEDS_CONFIRMATION
NEEDS_ANDROID_PERMISSION
FORBIDDEN
```

The current implementation maps these through `RiskLevel`, `ApprovalDecision`, `ToolApprovalStore`, and Android permission checks while preserving the existing public `ToolPermission` compatibility layer.

Do not let an Agent convert a denied permission into a different tool call that bypasses the boundary.

## 5. Observation-first execution

Every tool call should return structured evidence, not only free-form text.

The current implementation introduces `AgentObservation` and `AgentContextStore`. Existing `ToolResult` remains backward-compatible and is recorded into the observation stream by `NexusAgent`.

The verifier should consume observations and answer:

1. Did the requested action actually happen?
2. Is the output valid?
3. Is the result complete enough for the user's goal?
4. Should the Agent retry?
5. Should it change strategy?

This prevents “the model said it worked” from being treated as verification.

## 6. Recovery and re-planning

Use bounded recovery rather than infinite autonomous loops.

```text
Tool failure
   |
   +-- transient? --> retry same step
   |
   +-- bad input? --> repair input / retry
   |
   +-- permission? --> request permission / user confirmation
   |
   +-- strategy failure? --> re-plan
   |
   +-- unsafe/unsupported? --> stop and explain
```

The existing recovery policy remains the compatibility implementation. Architecture v2 additionally defines `AgentReplanner` and a conservative implementation so future model-assisted re-planning has a stable boundary.

Each step should have limits for:

- retry count
- execution time
- model calls
- delegated agents
- permission prompts
- total task budget

## 7. Context and Memory

Separate short-lived execution context from persistent memory.

```text
Task Context
  -> current plan
  -> step history
  -> observations
  -> failures
  -> active permissions

Persistent Memory
  -> user preferences
  -> project memory
  -> task memory
  -> approved Skills
```

`AgentContextStore` is intentionally bounded. Persistent Room memory remains a separate concern and should not be treated as the same state as an in-flight execution.

Long histories should be compressed/summarized instead of passed blindly into every model call.

This is one of the strongest ideas to learn from Koog: history compression, model switching, state persistence, retries, and observability are agent infrastructure rather than UI features.

## 8. Model routing

The Agent Core must not depend directly on Gemini.

```text
ModelRouter
  |
  +-- Gemini
  +-- OpenRouter / other providers
  +-- local fallback
```

Routing can consider:

- task type
- required tool calling
- context size
- latency
- availability
- cost/resource policy
- language

A model may change during one task without losing the canonical Agent state.

## 9. What Nexus should learn from Codex

### Adopt

- session / turn / step execution context
- first-class tool runtime
- separate approval and execution policy
- cancellation propagation
- structured tool handlers
- explicit verification/observation
- workspace boundaries
- bounded execution

### Adapt for Android

- sandbox -> Android capability/permission boundary
- filesystem workspace -> `NexusWorkspace`
- terminal -> Android-native tools
- shell approval -> confirmation + Android permission policy
- patch/edit -> safe document/file operations

### Do not copy

- Unix/terminal-first assumptions
- unrestricted local filesystem assumptions
- PC developer workflow as the top-level product
- coding-only Agent identity

## 10. What Nexus should learn from Koog

### Adopt

- Kotlin-native composable Agent architecture
- reusable strategies
- history compression
- model/provider switching
- persistent Agent state/checkpoints
- robust retry primitives
- structured streaming/events
- observability/tracing

Koog explicitly provides advanced history compression, model switching, persistence, retry, structured streaming, and observability capabilities.

### Do not copy blindly

Nexus is an Android product first. Do not introduce abstractions solely because a framework supports them. Keep the runtime small until a real Nexus capability needs the abstraction.

## 11. What Nexus should learn from Operit

Use Android-agent patterns as references for:

- mobile task execution
- app/file capabilities
- Skills
- Memory
- local/cloud model combinations
- extensible tool protocols

But preserve Nexus's own architecture, security policy, and product identity. Study is not copy.

## 12. Proposed package structure

```text
agent/
  core/
    model/
    planner/
    execution/
    verification/
    recovery/
    context/

  runtime/
    tools/
    permissions/
    confirmation/
    cancellation/
    events/

  agents/
    coordinator/
    planner/
    researcher/
    file/
    android/
    office/
    github/
    coding/
    verifier/

  capabilities/
    web/
    files/
    apps/
    office/
    github/

  intelligence/
    model/
    routing/
    providers/

  memory/
  skills/
  data/
  ui/
```

The exact package names can follow the existing project structure; this is a logical boundary, not a mandate for a large refactor.

## 13. Implementation status

| Capability | Architecture v2 | Branch implementation |
|---|---|---|
| Session / turn primitives | Defined | Implemented |
| Explicit runtime state | Defined | Implemented |
| Structured observations | Defined | Implemented |
| Common tool runtime | Defined | Implemented |
| Permission / confirmation boundary | Defined | Implemented |
| Cancellation propagation | Defined | Implemented in runtime |
| Mobile workspace | Defined | App-private implementation + traversal guard |
| Multi-agent delegation | Defined | Coordinator contract + conservative implementation |
| Failure-aware re-plan boundary | Defined | Replanner contract added |
| Checkpoint persistence | Defined | Persistence contract + in-memory implementation |
| Event / timeline stream | Defined | Event contract added |
| Model switching | Defined | Existing ModelRouter remains authoritative |
| Skills + persistent Memory | Defined | Existing modules remain separate; deeper integration follows |
| Office / Web / GitHub / Coding specialists | Defined | Existing product integrations remain separate |

## 14. Acceptance criteria for Architecture v2

Architecture v2 is successful when Nexus can:

- execute a real multi-step task on Android without Root;
- use multiple tools through one runtime;
- ask for permission/confirmation at the correct boundary;
- cancel a running task cleanly;
- verify actual results using observations;
- retry transient failures within a bound;
- re-plan when the original strategy fails;
- delegate bounded work to specialist Agents;
- switch models without losing task state;
- persist useful task/project memory safely;
- explain what happened in a clear execution timeline.

The target is not “Nexus becomes Codex.”

The target is:

> **Codex-grade execution discipline + Koog-grade Kotlin Agent infrastructure + Android-native capabilities + Nexus's own multi-agent product experience.**
