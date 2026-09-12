package com.example.agent.nexus.agent

/**
 * Classifies a user request into a small, inspectable intent vocabulary.
 * This keeps intent understanding deterministic while leaving room for a
 * model-assisted classifier later.
 */
class IntentClassifier {
    fun classify(input: String): AgentIntent {
        val normalized = input.trim().lowercase()
        if (normalized.isBlank()) return AgentIntent.General

        return when {
            isAppRequest(normalized) -> AgentIntent.App
            containsAny(normalized, "github", "pull request", "issue", "ci/cd", "ci") -> AgentIntent.GitHub
            containsAny(normalized, "office", "document", "spreadsheet", "presentation") ||
                containsAny(input, "簡報", "試算表") -> AgentIntent.Office
            containsAny(normalized, "memory", "remember", "forget") || containsAny(input, "記憶", "記住") -> AgentIntent.Memory
            containsAny(normalized, "skill", "skills") || containsAny(input, "技能") -> AgentIntent.Skills
            containsAny(normalized, "web", "search", "research") || containsAny(input, "網頁", "搜尋", "搜索", "研究") -> AgentIntent.WebResearch
            containsAny(normalized, "file", "files") || containsAny(input, "檔案", "文件") -> AgentIntent.File
            else -> AgentIntent.General
        }
    }

    private fun isAppRequest(input: String): Boolean =
        (input.contains("open app") || input.contains("launch app") ||
            input.contains("開啟 app") || input.contains("打开 app"))

    private fun containsAny(value: String, vararg terms: String): Boolean =
        terms.any(value::contains)
}

enum class AgentIntent {
    General,
    App,
    GitHub,
    Office,
    Memory,
    Skills,
    WebResearch,
    File
}
