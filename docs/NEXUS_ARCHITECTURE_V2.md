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

## Architecture goal

Nexus is a native Android, mobile-first AI Agent that can understand a user's goal, plan work, select models and tools, execute real tasks safely, verify outcomes, recover from failures, and improve through Skills and Memory.

Nexus is **not** a PC coding agent moved onto Android. Coding is one work capability among many.

The target is:

> **Codex-grade execution discipline + Koog-grade Kotlin Agent infrastructure + Android-native capabilities + Nexus's own multi-agent product experience.**

## Implementation map

```text
User
  -> Nexus UI
  -> AgentSession / AgentTurn
  -> Planner / ModelRouter
  -> MultiAgentCoordinator (when delegation is useful)
  -> ToolRuntime
       -> confirmation
       -> Android permission boundary
       -> cancellation propagation
       -> AgentTool
  -> AgentObservation / AgentContextStore
  -> AgentVerifier
  -> bounded retry / AgentReplanner
  -> checkpoint / execution timeline
```

## Codex lessons adopted

Nexus adopts the separation of execution from approval/sandbox policy, session-scoped approval caching, cancellation propagation, workspace boundaries, and first-class tool runtime concepts. The Android mapping is deliberate: Android capabilities and permissions replace Unix-shell assumptions.

## Koog lessons adopted

Nexus adopts Kotlin-native composable Agent boundaries, persistent state/checkpoint concepts, bounded retry, model switching without losing canonical task state, structured events, and future observability/history-compression hooks.

## Android-specific rules

- Native Android / Kotlin / Jetpack Compose.
- No Root requirement.
- Android public APIs and explicit user permissions.
- No hidden unrestricted filesystem or shell prerequisite.
- Risky or irreversible tools stay behind confirmation.
- Permission denial cannot be bypassed by silently selecting a different tool.
- Coroutine cancellation must propagate through the tool runtime.
- Mobile workspaces must enforce explicit access boundaries.

## Current foundation files

- `AgentRuntimeState.kt`: lifecycle states.
- `AgentSession.kt`: session and turn identity.
- `AgentContext.kt`: bounded observation context.
- `AgentEvents.kt`: structured timeline/tracing events.
- `AgentCheckpoint.kt`: resumable execution persistence boundary.
- `ToolApproval.kt`: session-scoped approvals.
- `ToolRuntime.kt`: permission/confirmation/cancellation-aware execution boundary.
- `MobileWorkspace.kt`: safe app-private workspace with traversal protection.
- `MultiAgentCoordinator.kt`: specialist delegation and re-planning contracts.
- `NexusAgent.kt`: existing execution path wired through `ToolRuntime`.
- `AgentArchitectureV2Test.kt`: focused safety and policy tests.

## Delivery policy

This foundation is intentionally additive. Existing `AgentTask`, `AgentPlan`, `AgentExecution`, `ToolRegistry`, `ToolPermission`, model providers, Skills, Memory, and product integrations remain usable. Deeper integration should continue through small, testable increments rather than a destructive rewrite.

## Definition of done for the architecture layer

The architecture layer is complete when every real Nexus capability can enter through the same task lifecycle, use the same tool policy boundary, emit observable evidence, respect cancellation and permission boundaries, recover within bounded limits, and remain resumable without turning the product into a PC-first coding agent.
