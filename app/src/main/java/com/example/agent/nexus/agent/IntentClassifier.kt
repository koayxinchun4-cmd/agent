package com.example.agent.nexus.agent

/**
 * Classifies a user request into a small, inspectable intent vocabulary.
 * Supports Chinese, English, and Bahasa Melayu (including code-switching).
 *
 * Match order (most specific first to avoid cross-category hijacking):
 * 1. Dangerous (safety first)
 * 2. App (specific package: / launch patterns)
 * 3. GitHub (specific platform keywords)
 * 4. File (file-specific operations)
 * 5. Memory (memory-specific verbs)
 * 6. Office (document types)
 * 7. WebResearch (general info lookup)
 * 8. Skills
 * 9. General (fallback)
 */
class IntentClassifier {

    fun classify(input: String): AgentIntent {
        val normalized = input.trim().lowercase()
        if (normalized.isBlank()) return AgentIntent.General

        return when {
            isDangerousRequest(normalized) -> AgentIntent.Dangerous
            isAppRequest(normalized) -> AgentIntent.App
            isGitHubRequest(normalized) -> AgentIntent.GitHub
            isFileRequest(normalized, input) -> AgentIntent.File
            isMemoryRequest(normalized, input) -> AgentIntent.Memory
            isOfficeRequest(normalized, input) -> AgentIntent.Office
            isWebRequest(normalized, input) -> AgentIntent.WebResearch
            containsAny(normalized, "skill", "skills", "kemahiran") || containsAny(input, "技能") -> AgentIntent.Skills
            else -> AgentIntent.General
        }
    }

    private fun isDangerousRequest(input: String): Boolean =
        containsAny(input, "delete", "remove", "uninstall", "padam", "buang", "nyahpasang", "format", "reset", "wipe", "hapus", "bin", "sampah", "格式化", "刪除", "卸载", "重置")

    private fun isAppRequest(input: String): Boolean {
        // Only match explicit app launch patterns, NOT generic "buat" (which is office/task)
        return containsAny(input, "open app", "launch app", "開啟 app", "打开 app", "buka app", "buka aplikasi") ||
        containsAny(input, "run app", "jalan app", "main app", "order", "booking", "jadual", "bayar", "duit", "teksi", "grab", "hantar", "share", "kongsi") ||
        // Package pattern
        input.contains("package:")
    }

    private fun isGitHubRequest(input: String): Boolean =
        containsAny(input, "github", "pull request", "issue", "ci/cd", "ci")

    private fun isFileRequest(input: String, original: String): Boolean =
        containsAny(input, "file", "files", "fail", "folder", "direktori", "directory", "zip", "download", "muat turun", "upload", "muat naik", "pdf", "txt", "csv", "xlsx", "docx") ||
        containsAny(original, "檔案", "文件", "下载", "上传")

    private fun isMemoryRequest(input: String, original: String): Boolean =
        containsAny(input, "memory", "remember", "forget", "ingat", "hafal", "simpan", "save", "recall", "hapus") ||
        containsAny(original, "記憶", "記住", "记住", "存档", "保存")

    private fun isOfficeRequest(input: String, original: String): Boolean =
        containsAny(input, "office", "document", "spreadsheet", "presentation", "dokumen", "hamparan", "persembahan", "report", "excel", "word", "powerpoint", "borang", "form", "buat", "create") ||
        containsAny(original, "簡報", "試算表", "報告", "报告", "表格", "文档")

    private fun isWebRequest(input: String, original: String): Boolean =
        containsAny(input, "web", "search", "research", "cari", "carian", "jelajah", "layari", "internet", "google", "cuaca", "weather", "harga", "price", "berita", "news", "pasar malam", "trend", "viral") ||
        containsAny(original, "網頁", "搜尋", "搜索", "研究")

    private fun containsAny(value: String, vararg terms: String): Boolean =
        terms.any(value::contains)
}
