# Real Model Providers v2

## What changed

Nexus now has native `ModelProvider` adapters for Gemini and OpenRouter while keeping the deterministic Local provider as a safe fallback.

## Architecture

```text
NexusAgent
   ↓
ModelRouter
   ↓
ModelProviderRegistry
   ├── GeminiModelProvider
   ├── OpenRouterModelProvider
   └── LocalModelProvider
```

### Provider abstraction

`ModelProvider` is an Interface（介面）. Agent Core calls `generate(prompt)` without knowing the HTTP API details.

### Availability

A remote provider is available only when its API key is a real-looking configured value. Placeholder values in `.env.example` never trigger remote calls.

### Failure handling

Remote generation uses a bounded timeout. HTTP/client failures are wrapped as `ProviderRequestException`, while blank responses are rejected instead of being treated as successful output.

The Agent keeps `LocalModelProvider` available so missing credentials do not make the whole Agent path unusable.

## Why this design

This follows lessons from the reference-project study:

- Koog: keep model execution behind a provider abstraction and make failure/retry behavior explicit.
- Operit: separate model/backend concerns from mobile tool execution.
- Self-Healing Agent: verify failures instead of silently accepting bad output.
- Debroid: prefer deterministic, machine-readable failure signals for debugging.

## Important limitation

The normal Chat screen still uses the existing Gemini `ChatRepository` path. This task wires the provider abstraction into `NexusAgent` first; a later task can unify Chat and Agent conversations behind the same router.

## Security

- Real API keys belong in local `.env` or GitHub Secrets.
- Placeholder keys remain in `.env.example`.
- No real credentials are committed.
- No automatic merge or deployment is introduced by this change.
