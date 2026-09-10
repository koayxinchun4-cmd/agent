# Real Model Provider + Repository Cleanup

## Objective

Connect the existing `ModelProvider` architecture to real Gemini and OpenRouter backends while preserving the Local fallback, then perform a compatibility-first repository cleanup.

## Scope

- Implement a Gemini `ModelProvider` using the existing Retrofit/Moshi network layer.
- Implement an OpenRouter `ModelProvider` using an OpenAI-compatible chat completion API.
- Keep `LocalModelProvider` available as a safe fallback.
- Integrate providers with `ModelProviderRegistry` and `ModelRouter`.
- Preserve existing Agent Task, Agent Loop, App Agent, Local File Agent, Memory, Skills, Settings, GitHub, and CI functionality.
- Handle unavailable providers and network/API failures without exposing secrets.
- Add focused unit tests for provider behavior, routing, and fallback.
- Remove only obsolete/duplicated repository clutter that is clearly safe to remove.
- Keep technical names English-first and Traditional Chinese explanations where useful.
- Keep Native Android/Kotlin/Jetpack Compose as the project architecture. Expo Go is not part of Nexus.

## Acceptance Criteria

1. Nexus can select a configured Gemini provider.
2. Nexus can select a configured OpenRouter provider.
3. If a selected cloud provider is unavailable, Nexus falls back safely to Local when appropriate.
4. Provider failures produce actionable, non-secret error information.
5. Existing tools and Agent Loop behavior do not regress.
6. Unit tests cover the new provider layer and fallback behavior.
7. Android CI and security checks pass.
8. Cleanup does not remove historical functionality merely because it is being refactored.

## Security

- Never commit real API keys, access tokens, or private credentials.
- Read credentials only through the existing secure configuration mechanism.
- Do not log request headers containing secrets.
- Do not silently merge or deploy production changes.

## Implementation Notes

The current repository already has Retrofit, Moshi, OkHttp, and secrets Gradle configuration. Reuse those layers instead of adding an unnecessary networking stack.
