# External Project References

This document records external sites and repositories reviewed as architectural references for Nexus AI. These references are informational only unless a future change explicitly adds a dependency.

## 1. GitHub Marketplace

- Source: https://github.com/marketplace
- Type: GitHub Apps / Actions / integrations marketplace
- Nexus relevance: useful for discovering existing GitHub integrations, automation, and developer tooling.
- Planned use: research/reference only for now; prefer least-privilege integrations and review permissions before adoption.

## 2. Model Context Protocol (MCP)

- Source: https://github.com/mcp
- Type: Model/tool integration protocol ecosystem
- Nexus relevance: potential future standard interface for connecting external tools to the Agent.
- Planned architecture: MCP Tool Registry -> MCP Adapter -> Permission/Confirmation -> Agent Tool Calling.
- Planned use: evaluate after core Tool Calling, Verification, and Failure/Recovery are stable.

## 3. RockChinQ/free-one-api

- Source: https://github.com/RockChinQ/free-one-api
- Type: OpenAI-compatible LLM API gateway / reverse-engineering project
- Nexus relevance: architectural reference for provider abstraction, health checks, unavailable-provider handling, load balancing, fallback/retry, and OpenAI-compatible interfaces.
- Planned use: reference only; do not make it a Nexus core dependency.
- Risk/license note: the repository uses AGPL-3.0 and its README describes reverse-engineered access paths. Any future integration requires a separate legal, security, and provider-policy review.

## Relationship to Nexus AI

These sources inform design decisions but are not product requirements. Nexus remains centered on native Android, Kotlin + Jetpack Compose, explicit Android permissions, safe execution, multi-model routing, verification, and user confirmation for risky actions.
