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

The repository now contains pure Kotlin models for connected account state, OAuth PKCE request generation, and OAuth callback parsing. Android browser launch, callback dispatch into the auth state owner, token exchange, secure credential storage, and GitHub API client wiring remain separate steps so each boundary can be tested independently.

## OAuth callback contract

The Android app registers the following deep link:

`nexus://github/oauth/callback`

GitHub redirects to that URI with either:

- Success: `code` + `state`
- Failure: `error`, optional `error_description`, and optional `state`

The callback parser validates the URI shape and required fields but does **not** trust `state` by itself. The OAuth flow owner must compare the returned state with the state generated for the active authorization request before accepting the authorization code.

The callback parser also does not exchange the code for an access token. Token exchange remains a separate step and must keep any client secret outside the Android APK.
