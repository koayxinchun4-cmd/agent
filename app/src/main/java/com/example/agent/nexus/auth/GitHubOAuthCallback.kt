package com.example.agent.nexus.auth

import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/** Result returned after parsing the GitHub OAuth redirect URI. */
sealed interface GitHubOAuthCallbackResult {
    data class Success(
        val code: String,
        val state: String
    ) : GitHubOAuthCallbackResult

    data class Failure(
        val error: String,
        val description: String? = null,
        val state: String? = null
    ) : GitHubOAuthCallbackResult
}

/** Pure Kotlin callback parsing; state verification is performed by the OAuth flow owner. */
object GitHubOAuthCallback {
    const val REDIRECT_URI = "nexus://github/oauth/callback"

    fun parse(uri: String): GitHubOAuthCallbackResult {
        val parsed = runCatching { URI(uri) }.getOrElse {
            return GitHubOAuthCallbackResult.Failure("invalid_callback_uri")
        }

        if (parsed.scheme != "nexus" || parsed.host != "github" || parsed.path != "/oauth/callback") {
            return GitHubOAuthCallbackResult.Failure("invalid_callback_uri")
        }

        val params = parseQuery(parsed.rawQuery)
        val state = params["state"]
        val error = params["error"]
        if (error != null) {
            return GitHubOAuthCallbackResult.Failure(
                error = error,
                description = params["error_description"],
                state = state
            )
        }

        val code = params["code"]
        if (code.isNullOrBlank() || state.isNullOrBlank()) {
            return GitHubOAuthCallbackResult.Failure("missing_code_or_state", state = state)
        }

        return GitHubOAuthCallbackResult.Success(code = code, state = state)
    }

    private fun parseQuery(rawQuery: String?): Map<String, String> {
        if (rawQuery.isNullOrEmpty()) return emptyMap()
        return rawQuery.split('&')
            .mapNotNull { pair ->
                val parts = pair.split('=', limit = 2)
                if (parts.isEmpty() || parts[0].isEmpty()) return@mapNotNull null
                val key = decode(parts[0])
                val value = decode(parts.getOrElse(1) { "" })
                key to value
            }
            .toMap()
    }

    private fun decode(value: String): String =
        URLDecoder.decode(value, StandardCharsets.UTF_8.name())
}
