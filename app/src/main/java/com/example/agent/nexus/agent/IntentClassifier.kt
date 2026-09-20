package com.example.agent.nexus.agent

/**
 * Classifies a user request into a small, inspectable intent vocabulary.
 * Supports Chinese, English, and Bahasa Melayu (including code-switching).
 */
class IntentClassifier {

    fun classify(input: String): AgentIntent {
        val normalized = input.trim().lowercase()
        if (normalized.isBlank()) return AgentIntent.General

        return when {
            isDangerousRequest(normalized) -> AgentIntent.Dangerous
            isAppRequest(normalized) -> AgentIntent.App
            isWebRequest(normalized, input) -> AgentIntent.WebResearch
            isMemoryRequest(normalized, input) -> AgentIntent.Memory
            containsAny(normalized, "github", "pull request", "issue", "ci/cd", "ci") -> AgentIntent.GitHub
            isOfficeRequest(normalized, input) -> AgentIntent.Office
            containsAny(normalized, "skill", "skills", "kemahiran") || containsAny(input, "技能") -> AgentIntent.Skills
            isFileRequest(normalized, input) -> AgentIntent.File
            else -> AgentIntent.General
        }
    }

    private fun isDangerousRequest(input: String): Boolean =
        containsAny(input, "delete", "remove", "uninstall", "padam", "buang", "nyahpasang", "format", "reset", "wipe", "hapus", "bin", "sampah", "格式化", "刪除", "卸载", "重置")

    private fun isAppRequest(input: String): Boolean =
        containsAny(input, "open app", "launch app", "開啟 app", "打开 app", "buka app", "buka aplikasi") ||
        containsAny(input, "buat", "create app", "tengok app", "check app", "app ni", "guna app", "jalan", "run app", "main app", "order", "booking", "jadual", "bayar", "duit", "teksi", "grab", "hantar", "send", "share", "kongsi")

    private fun isWebRequest(input: String, original: String): Boolean =
        containsAny(input, "web", "search", "research", "cari", "carian", "jelajah", "layari", "internet", "google", "check", "cuaca", "weather", "harga", "price", "berita", "news", "maklumat", "info", "pasar malam", "trend", "viral") ||
        containsAny(original, "網頁", "搜尋", "搜索", "研究")

    private fun isMemoryRequest(input: String, original: String): Boolean =
        containsAny(input, "memory", "remember", "forget", "ingat", "hafal", "simpan", "save", "recall") ||
        containsAny(original, "記憶", "記住", "记住", "存档", "保存")

    private fun isOfficeRequest(input: String, original: String): Boolean =
        containsAny(input, "office", "document", "spreadsheet", "presentation", "dokumen", "hamparan", "persembahan", "report", "excel", "word", "powerpoint", "pdf", "borang", "form") ||
        containsAny(original, "簡報", "試算表", "報告", "报告", "表格", "文档")

    private fun isFileRequest(input: String, original: String): Boolean =
        containsAny(input, "file", "files", "fail", "dokumen", "folder", "direktori", "directory", "zip", "download", "muat turun", "upload", "muat naik") ||
        containsAny(original, "檔案", "文件", "下载", "上传")

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
    File,
    Dangerous
}
