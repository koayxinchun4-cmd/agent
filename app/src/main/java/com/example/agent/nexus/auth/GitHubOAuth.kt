package com.example.agent.nexus.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/** Pure Kotlin OAuth 2.0 Authorization Code + PKCE values. */
data class GitHubOAuthRequest(
    val state: String,
    val codeVerifier: String,
    val codeChallenge: String,
    val authorizationUrl: String
)

object GitHubOAuth {
    private const val AUTHORIZE_ENDPOINT = "https://github.com/login/oauth/authorize"
    private const val DEFAULT_SCOPE = "read:user user:email"

    fun createAuthorizationRequest(
        clientId: String,
        redirectUri: String,
        scope: String = DEFAULT_SCOPE,
        random: SecureRandom = SecureRandom()
    ): GitHubOAuthRequest {
        require(clientId.isNotBlank()) { "GitHub client ID must not be blank" }
        require(redirectUri.isNotBlank()) { "GitHub redirect URI must not be blank" }

        val state = randomUrlSafe(random, 32)
        val verifier = randomUrlSafe(random, 64)
        val challenge = sha256Base64Url(verifier)
        val url = buildString {
            append(AUTHORIZE_ENDPOINT)
            append("?client_id=").append(urlEncode(clientId))
            append("&redirect_uri=").append(urlEncode(redirectUri))
            append("&scope=").append(urlEncode(scope))
            append("&state=").append(urlEncode(state))
            append("&allow_signup=false")
        }
        return GitHubOAuthRequest(state, verifier, challenge, url)
    }

    private fun randomUrlSafe(random: SecureRandom, byteCount: Int): String {
        val bytes = ByteArray(byteCount)
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun sha256Base64Url(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.US_ASCII))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    private fun urlEncode(value: String): String =
        java.net.URLEncoder.encode(value, Charsets.UTF_8.name()).replace("+", "%20")
}
