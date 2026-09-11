package com.example.agent.nexus.skill

import java.security.MessageDigest

/** Small, format-agnostic integrity boundary for imported skill content. */
object SkillIntegrity {
    fun sha256(content: String): String = MessageDigest
        .getInstance("SHA-256")
        .digest(content.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    fun verify(content: String, expectedSha256: String?): Boolean {
        val expected = expectedSha256?.trim().orEmpty()
        if (expected.isEmpty()) return true
        require(expected.matches(HEX_SHA256)) { "invalid skill content SHA-256" }
        return sha256(content).equals(expected, ignoreCase = true)
    }

    private val HEX_SHA256 = Regex("[0-9a-fA-F]{64}")
}
