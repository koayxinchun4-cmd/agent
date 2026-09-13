package com.example.agent.nexus.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubOAuthTest {
    @Test
    fun createsPkceAuthorizationRequest() {
        val request = GitHubOAuth.createAuthorizationRequest(
            clientId = "Iv1.example",
            redirectUri = "com.example.agent://github/callback"
        )

        assertTrue(request.state.isNotBlank())
        assertTrue(request.codeVerifier.isNotBlank())
        assertTrue(request.codeChallenge.isNotBlank())
        assertTrue(request.authorizationUrl.startsWith("https://github.com/login/oauth/authorize?"))
        assertTrue(request.authorizationUrl.contains("client_id=Iv1.example"))
        assertTrue(request.authorizationUrl.contains("code_challenge=" + request.codeChallenge))
        assertTrue(request.authorizationUrl.contains("state=" + request.state))
    }

    @Test
    fun connectedStateReflectsAccountPresence() {
        assertFalse(GitHubAuthState().isConnected)
        assertTrue(GitHubAuthState(GitHubAccount("cc")).isConnected)
        assertEquals("cc", GitHubAuthState(GitHubAccount("cc")).account?.login)
    }
}
