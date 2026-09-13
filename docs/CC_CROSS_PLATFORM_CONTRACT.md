# CC Cross-Platform Agent Contract

Status: **FOUNDATION**

CC Android and CC Mobile Web are two native entry points to the same Agent product. They must exchange the same task/session/step/progress/result contract even when their available tools differ.

## 1. Platform boundary

```text
CC Android ─────┐
                ├── Shared Agent Contract ── CC Agent API ── Agent Runtime
CC Mobile Web ──┘
```

The contract is JSON-compatible and intentionally independent of Android/Kotlin UI classes.

## 2. Shared objects

### AgentTask

```json
{
  "id": "task_123",
  "sessionId": "session_123",
  "input": "整理今天最重要的三件事",
  "createdAt": "2026-09-13T00:00:00Z",
  "metadata": {
    "locale": "zh-TW",
    "client": "android"
  }
}
```

Required: `id`, `sessionId`, `input`, `createdAt`.

### AgentSession

```json
{
  "id": "session_123",
  "taskId": "task_123",
  "state": "EXECUTING",
  "turnIndex": 2,
  "updatedAt": "2026-09-13T00:00:03Z"
}
```

Allowed `state` values:

`CREATED`, `UNDERSTANDING`, `PLANNING`, `EXECUTING`, `VERIFYING`, `RETRYING`, `WAITING_FOR_USER`, `WAITING_FOR_PERMISSION`, `WAITING_FOR_TOOL`, `COMPLETED`, `FAILED`.

### AgentStep

```json
{
  "id": "step_1",
  "index": 0,
  "kind": "tool",
  "status": "COMPLETED",
  "toolId": "web.search",
  "attempt": 1,
  "output": "...",
  "success": true
}
```

`toolId` is optional for non-tool steps. Platform-specific tools must be capability-declared instead of silently assumed to exist everywhere.

### AgentProgress

```json
{
  "type": "agent.progress",
  "sessionId": "session_123",
  "sequence": 7,
  "state": "EXECUTING",
  "step": { "id": "step_1", "index": 0, "kind": "tool", "status": "COMPLETED", "attempt": 1, "success": true },
  "steps": []
}
```

`sequence` is monotonically increasing within a session. Clients can use it to resume after reconnecting.

### AgentResult

```json
{
  "type": "agent.result",
  "sessionId": "session_123",
  "status": "COMPLETED",
  "text": "整理完成。",
  "steps": [],
  "completedAt": "2026-09-13T00:00:05Z"
}
```

## 3. Transport contract

The first backend boundary should support:

- `POST /v1/tasks` — create a task and return its session identity.
- `GET /v1/sessions/{sessionId}` — fetch the latest session snapshot.
- `GET /v1/sessions/{sessionId}/events?after={sequence}` — resume progress events.
- `POST /v1/sessions/{sessionId}/input` — add user input / continue a waiting session.
- `POST /v1/sessions/{sessionId}/approve` — submit an explicit approval decision.
- `POST /v1/sessions/{sessionId}/cancel` — request bounded cancellation.

A future implementation may use HTTP + SSE/WebSocket for live progress. The wire contract must remain stable regardless of transport.

## 4. Capability negotiation

The shared core is synchronized; capabilities are not forced to be identical.

```json
{
  "client": "android",
  "capabilities": [
    "web.search",
    "files.user_selected",
    "android.intent",
    "notifications"
  ]
}
```

Web should advertise only portable capabilities. Android can advertise permission-gated device capabilities. The runtime must reject unavailable tools rather than pretending they exist.

## 5. Security rules

- Never put a Gemini/OpenRouter provider secret in `web/index.html`, browser JavaScript, or a public repository.
- Provider credentials belong behind the Agent API boundary or in secure user-controlled configuration.
- Tool approval, permission checks, cancellation and verification remain runtime concerns; clients only present and submit decisions.
- Private Android data is never synchronized merely because a session is synchronized.
- Cross-client sync carries task state and explicitly shareable observations, not arbitrary device contents.

## 6. Existing Android mapping

The current Android Agent already has an immutable `AgentSession` and bounded `AgentTurn`, plus `AgentProgress` snapshots. The cross-platform DTOs intentionally mirror these concepts without importing Android implementation classes.

This document is the contract foundation. The next backend milestone is a small authenticated Agent API implementation that can serve both clients.
