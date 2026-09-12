# Nexus AI — Design Principles

## Intelligence vs. Streaming

Nexus AI distinguishes **Agent intelligence** from **response delivery experience**.

### Intelligence / IQ

These capabilities improve how well the Agent understands, plans, executes, and verifies tasks:

- Intent understanding
- Task decomposition
- Planner
- Model routing
- Tool calling
- Verification
- Failure-aware recovery
- Conversation / Project / Task Memory
- Skills

### Incremental Streaming / UX

Incremental streaming improves response delivery and interaction experience. It does **not** directly make the model more intelligent.

- A non-streaming response waits for the complete result before delivery.
- Incremental streaming delivers generated chunks as they become available.
- Streaming should remain transport- and UI-independent through the `OutputChannel` abstraction.
- Future implementations may support text rendering, TTS, notifications, progress events, or other output modes without coupling Agent Core to a specific transport.

### Architecture Principle

> **Make the Agent smarter through reasoning, tools, memory, verification, and routing; make it feel faster through incremental streaming. Do not confuse delivery latency with intelligence.**

This principle should guide capability prioritization and architecture decisions as Nexus AI evolves.
