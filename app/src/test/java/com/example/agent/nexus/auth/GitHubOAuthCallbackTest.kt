package com.example.agent.nexus.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GitHubOAuthCallbackTest {
    @Test
    fun parsesSuccessfulCallback() {
        val result = GitHubOAuthCallback.parse(
            "${GitHubOAuthCallback.REDIRECT_URI}?code=abc123&state=state-123"
        )

        val success = assertIs<GitHubOAuthCallbackResult.Success>(result)
        assertEquals("abc123", success.code)
        assertEquals("state-123", success.state)
    }

    @Test
    fun parsesGithubErrorCallback() {
        val result = GitHubOAuthCallback.parse(
            "${GitHubOAuthCallback.REDIRECT_URI}?error=access_denied&error_description=User%20cancelled&state=state-123"
        )

        val failure = assertIs<GitHubOAuthCallbackResult.Failure>(result)
        assertEquals("access_denied", failure.error)
        assertEquals("User cancelled", failure.description)
        assertEquals("state-123", failure.state)
    }

    @Test
    fun rejectsWrongCallbackUri() {
        val result = GitHubOAuthCallback.parse(
            "https://example.com/oauth/callback?code=abc&state=state"
        )

        val failure = assertIs<GitHubOAuthCallbackResult.Failure>(result)
        assertEquals("invalid_callback_uri", failure.error)
    }

    @Test
    fun rejectsMissingCodeOrState() {
        val result = GitHubOAuthCallback.parse(
            "${GitHubOAuthCallback.REDIRECT_URI}?code=abc"
        )

        val failure = assertIs<GitHubOAuthCallbackResult.Failure>(result)
        assertEquals("missing_code_or_state", failure.error)
    }
}
