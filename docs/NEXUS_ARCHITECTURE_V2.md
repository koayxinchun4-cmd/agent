# Nexus AI — Architecture v2

> Architecture proposal for the next generation of Nexus AI.
>
> This document turns the current product roadmap into an implementation-oriented Agent architecture while preserving Nexus's defining constraint: **the phone is the Agent's primary execution environment**.

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

Recommended core concepts:

- `AgentSession`
- `AgentTurn`
- `AgentTask`
- `AgentPlan`
- `AgentStep`
- `AgentExecution`
- `AgentObservation`
- `AgentResult`
- `AgentVerifier`
- `AgentRecovery`
- `AgentCancellation`

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
  -> WAITING_FOR_USER
  -> WAITING_FOR_PERMISSION
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

A coordinator decides whether a task should remain single-agent or be delegated to specialists.

Delegation must carry:

- objective
- constraints
- required tools
- permission scope
- relevant context
- expected output
- verification criteria

The coordinator owns the final result. Specialist agents must not silently expand permissions or task scope.

### Layer D — Tool Runtime

Tools should be executed through a common runtime instead of each Agent implementing its own permission and failure logic.

Recommended abstractions:

```kotlin
interface AgentTool {
    val id: String
    val risk: ToolRisk
    suspend fun execute(input: ToolInput, context: ToolContext): ToolObservation
}

interface ToolRuntime {
    suspend fun execute(
        call: ToolCall,
        context: ToolContext,
    ): ToolObservation
}

interface PermissionPolicy {
    suspend fun evaluate(call: ToolCall): PermissionDecision
}

interface ConfirmationPolicy {
    suspend fun confirm(request: ConfirmationRequest): ConfirmationDecision
}
```

The runtime should centrally handle:

- permission checks
- confirmation
- cancellation
- timeouts
- retry policy
- structured errors
- audit/event emission
- tool availability

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

Codex's runtime separates approval decisions from tool execution and supports cached session approvals and explicit forbidden states. Nexus should adopt the architectural separation, but translate it to Android permissions rather than shell sandboxing.

Recommended decisions:

```text
ALLOW
ALLOW_FOR_SESSION
NEEDS_CONFIRMATION
NEEDS_ANDROID_PERMISSION
FORBIDDEN
```

Examples:

- Read an already-authorized project file -> `ALLOW`
- Repeatedly perform the same low-risk operation -> optionally `ALLOW_FOR_SESSION`
- Send/delete/modify something consequential -> `NEEDS_CONFIRMATION`
- Access a user-selected document tree -> `NEEDS_ANDROID_PERMISSION`
- Unsupported or unsafe capability -> `FORBIDDEN`

Do not let an Agent convert a denied permission into a different tool call that bypasses the boundary.

## 5. Observation-first execution

Every tool call should return structured evidence, not only free-form text.

```kotlin
data class ToolObservation(
    val status: ObservationStatus,
    val summary: String,
    val evidence: List<EvidenceItem>,
    val artifacts: List<ArtifactRef>,
    val error: ToolError?,
)
```

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

Koog explicitly targets Kotlin/JVM and multiplatform applications and provides advanced history compression, model switching, persistence, retry, structured streaming, and observability capabilities.

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

## 13. Implementation order

Do not rewrite the whole application.

### Phase A — Agent Core Reliability

1. Introduce `AgentSession` / `AgentTurn` / `AgentStep` state.
2. Normalize tool execution through `ToolRuntime`.
3. Add structured `AgentObservation`.
4. Add cancellation and bounded retry contracts.
5. Upgrade verification to consume structured observations.

### Phase B — Mobile Runtime

6. Add permission/confirmation policy interfaces.
7. Wrap existing File Agent and App Agent behind the common runtime.
8. Introduce `NexusWorkspace` for safe mobile resource boundaries.
9. Add execution events for the timeline.

### Phase C — Recovery Intelligence

10. Add failure classification.
11. Add bounded strategy re-planning.
12. Add model switching while preserving canonical task state.

### Phase D — Multi-Agent

13. Add coordinator/delegation contracts.
14. Start with two specialists where value is clear, e.g. Research + Verifier.
15. Add broader specialist Agents only after coordination is reliable.

### Phase E — Skills + Memory

16. Connect Skills to tool availability and permission policy.
17. Persist task/project memory separately from transient execution state.
18. Add safe checkpoint/recovery behavior.

### Phase F — Advanced Work Agents

19. Office workflows.
20. GitHub workflows.
21. Coding Agent as a specialist/backend.
22. CI diagnosis and bounded repair.

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
