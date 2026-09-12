package com.example.agent.nexus.memory

/**
 * A user preference kept separate from conversation content.
 * Metadata is extensible so new preference attributes do not require a contract change.
 */
data class UserPreference(
    val key: String,
    val value: String,
    val metadata: Map<String, String> = emptyMap(),
    val updatedAt: Long = System.currentTimeMillis()
)
