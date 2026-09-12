# Nexus Execution Context

## Responsibility

`AgentExecutionContext` is the immutable context boundary for one Agent execution.

It carries:

- `taskId`: identity of the current task.
- `projectId`: optional project scope.
- `memory`: read-only scoped memory available to Agent Core.
- `metadata`: extensible execution metadata.

## Design Rules

- Agent Core depends on this contract, not on Room or a concrete memory store.
- Memory remains read-only inside the execution context.
- Metadata is extensible so future input/output channels do not require redesigning the core context.
- The context does not define provider-specific prompts or hardcode current tools.
- A blank task ID is rejected because execution identity is required for safe tracing and task-scoped state.

## Data Flow

```text
Input Channel
    -> AgentTask
    -> MemoryContextProvider
    -> AgentExecutionContext
    -> Planner / Model Router / Tool Execution
```

This is a context boundary, not a storage layer and not a model request format.
