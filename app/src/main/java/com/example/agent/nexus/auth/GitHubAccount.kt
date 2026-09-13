package com.example.agent.nexus.auth

/** Represents the non-secret GitHub account state shown by Nexus UI. */
data class GitHubAccount(
    val login: String,
    val displayName: String? = null,
    val avatarUrl: String? = null
)

data class GitHubAuthState(
    val account: GitHubAccount? = null
) {
    val isConnected: Boolean get() = account != null
}
