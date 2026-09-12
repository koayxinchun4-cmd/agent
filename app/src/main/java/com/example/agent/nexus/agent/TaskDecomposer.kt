package com.example.agent.nexus.agent

/**
 * Deterministically decomposes a user goal into small, ordered subtasks.
 * The decomposition is intentionally inspectable so model-assisted planning
 * can replace or enrich it later without changing the plan contract.
 */
class TaskDecomposer {
    fun decompose(input: String): List<String> {
        val normalized = input.trim()
        if (normalized.isBlank()) return emptyList()

        val parts = normalized
            .split(DELIMITERS)
            .map(String::trim)
            .filter(String::isNotBlank)
            .map(::stripListMarker)
            .filter(String::isNotBlank)

        return if (parts.isEmpty()) listOf(normalized) else parts
    }

    private fun stripListMarker(value: String): String =
        value.replaceFirst(LIST_MARKER, "").trim()

    private companion object {
        val DELIMITERS = Regex("\\s*(?:\\bthen\\b|\\band then\\b|\\bafter that\\b|\\bnext\\b|;|\\n|、|，|；|然后|接著|接着|再|最後|最后)\\s*", RegexOption.IGNORE_CASE)
        val LIST_MARKER = Regex("^(?:[-*•]|\\d+[.)]|[一二三四五六七八九十]+[、.)])\\s*")
    }
}
