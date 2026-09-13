# GitHub Account Connection

Nexus connects a user's GitHub account so GitHub-specific tools and agents can act on the user's behalf.

## Security boundary

- Use OAuth 2.0 Authorization Code with PKCE.
- Keep the OAuth client secret out of the Android APK.
- Store access credentials only in Android secure storage backed by the Android Keystore.
- Keep GitHub credentials out of `AgentTask.metadata`, prompts, logs, and tool arguments.
- Request the smallest GitHub permission set required by the tools actually enabled.
- GitHub tools must fail closed when no GitHub account is connected.
- Disconnect must invalidate the local credential state and account presentation.

## Current implementation step

The repository now contains pure Kotlin models for connected account state and OAuth PKCE request generation. Android browser launch, callback handling, token exchange, secure credential storage, and GitHub API client wiring are intentionally separate steps so each boundary can be tested independently.
