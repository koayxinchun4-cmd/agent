package com.example.agent.nexus.tool

/** Validates user-provided relative file paths before filesystem access. */
object FilePathValidator {
    fun validate(relativePath: String): String? {
        val path = relativePath.trim()
        if (path.isEmpty()) return "File path must not be empty"
        if (path.indexOf('\u0000') >= 0) return "File path contains an invalid null character"
        if (path.startsWith('/') || path.startsWith('\\') || WINDOWS_DRIVE_PREFIX.matches(path)) {
            return "Absolute file paths are not allowed"
        }
        if (path.split('/', '\\').any { it == "" || it == "." }) {
            return "File path contains an invalid empty or current-directory segment"
        }
        return null
    }

    private val WINDOWS_DRIVE_PREFIX = Regex("^[A-Za-z]:([/\\\\].*)?$")
}
