package com.example.agent.nexus.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class GitHubOAuthCallbackTest {
    @Test
    fun parsesSuccessfulCallback() {
        val result = GitHubOAuthCallback.parse(
            "${GitHubOAuthCallback.REDIRECT_URI}?code=abc123&state=state-123"
        )

        assertEquals(
            GitHubOAuthCallbackResult.Success("abc123", "state-123"),
            result
        )
    }

    @Test
    fun parsesGithubErrorCallback() {
        val result = GitHubOAuthCallback.parse(
            "${GitHubOAuthCallback.REDIRECT_URI}?error=access_denied&error_description=User%20cancelled&state=state-123"
        )

        assertEquals(
            GitHubOAuthCallbackResult.Failure(
                error = "access_denied",
                description = "User cancelled",
                state = "state-123"
            ),
            result
        )
    }

    @Test
    fun rejectsWrongCallbackUri() {
        val result = GitHubOAuthCallback.parse(
            "https://example.com/oauth/callback?code=abc&state=state"
        )

        assertEquals(
            GitHubOAuthCallbackResult.Failure(error = "invalid_callback_uri"),
            result
        )
    }

    @Test
    fun rejectsMissingCodeOrState() {
        val result = GitHubOAuthCallback.parse(
            "${GitHubOAuthCallback.REDIRECT_URI}?code=abc"
        )

        assertEquals(
            GitHubOAuthCallbackResult.Failure(error = "missing_code_or_state"),
            result
        )
    }
}
