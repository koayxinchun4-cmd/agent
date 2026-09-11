package com.example.agent.nexus.skill

/**
 * Classifies whether a skill's declared license is eligible for installation.
 * The policy is intentionally replaceable so future sources can supply their own rules.
 */
class SkillLicenseGate(
    allowedLicenses: Set<String> = DEFAULT_ALLOWED_LICENSES
) {
    private val normalizedAllowedLicenses = allowedLicenses.mapTo(mutableSetOf()) { normalize(it) }

    fun evaluate(license: String): Decision {
        val normalized = normalize(license)
        if (normalized.isBlank()) return Decision.REVIEW_REQUIRED
        return if (normalized in normalizedAllowedLicenses) {
            Decision.ALLOWED
        } else {
            Decision.REVIEW_REQUIRED
        }
    }

    sealed interface Decision {
        data object ALLOWED : Decision
        data object REVIEW_REQUIRED : Decision
    }

    private fun normalize(value: String): String =
        value.trim().lowercase().replace(Regex("\\s+"), " ")

    private companion object {
        val DEFAULT_ALLOWED_LICENSES = setOf(
            "MIT",
            "Apache-2.0",
            "BSD-2-Clause",
            "BSD-3-Clause",
            "ISC"
        )
    }
}
