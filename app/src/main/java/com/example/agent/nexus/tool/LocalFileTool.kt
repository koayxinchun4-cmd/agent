package com.example.agent.nexus.tool

import android.content.ContentResolver
import android.net.Uri
import com.example.agent.nexus.agent.AgentTask
import java.io.File

/**
 * Safe File capability. It supports Nexus private app storage and, when the
 * user explicitly selects a document, Android Storage Access Framework (SAF).
 */
class LocalFileTool(
    private val rootDirectory: File,
    private val contentResolver: ContentResolver? = null
) : AgentTool {
    override val id: String = "file_agent"
    override val name: String = "Local File Agent"
    override val description: String = "Lists or reads files inside Nexus private storage or a user-selected SAF document."

    override suspend fun execute(task: AgentTask): ToolResult {
        val selectedUri = task.metadata[SELECTED_URI_KEY]
        if (selectedUri != null) {
            if (contentResolver == null) return ToolResult.Failure("SAF reader is not initialized")
            return SafFileReader { uri ->
                contentResolver.openInputStream(Uri.parse(uri))
            }.read(selectedUri)
        }

        val input = task.input.trim()
        if (input.isEmpty()) return ToolResult.Failure("File task input must not be empty")

        return runCatching {
            when {
                isReadRequest(input) -> readRequestedFile(input)
                else -> listFiles()
            }
        }.getOrElse { ToolResult.Failure("File tool execution failed: ${it.message ?: "unknown error"}", it) }
    }

    private fun listFiles(): ToolResult {
        val entries = rootDirectory.walkTopDown()
            .filter { it.isFile }
            .take(MAX_LIST_ENTRIES)
            .map { it.relativeTo(rootDirectory).path }
            .toList()

        val body = if (entries.isEmpty()) {
            "Nexus private storage currently has no files."
        } else {
            entries.joinToString("\n")
        }

        return ToolResult.Success("Nexus private file list\n$body")
    }

    private fun readRequestedFile(input: String): ToolResult {
        val relativePath = extractReadPath(input)
            ?: return ToolResult.Failure("Please provide the relative file path to read")
        FilePathValidator.validate(relativePath)?.let { return ToolResult.Failure(it) }

        val target = File(rootDirectory, relativePath).canonicalFile
        val root = rootDirectory.canonicalFile
        if (target != root && !target.path.startsWith(root.path + File.separator)) {
            return ToolResult.Failure("Access outside the Nexus private directory is not allowed")
        }
        if (!target.isFile) {
            return ToolResult.Failure("File not found: $relativePath")
        }
        if (target.length() > MAX_READ_BYTES) {
            return ToolResult.Failure("File is too large to read (limit ${MAX_READ_BYTES / 1024} KB)")
        }

        return ToolResult.Success("File read complete: $relativePath\n\n${target.readText(Charsets.UTF_8)}")
    }

    private fun isReadRequest(input: String): Boolean =
        input.contains(READ_MARKER) || input.contains(ENGLISH_READ_MARKER, ignoreCase = true)

    private fun extractReadPath(input: String): String? {
        val chineseMarker = input.indexOf(READ_MARKER)
        if (chineseMarker >= 0) {
            return input.substring(chineseMarker + READ_MARKER.length).trim().ifEmpty { null }
        }

        val englishMatch = ENGLISH_READ_REGEX.find(input) ?: return null
        return englishMatch.groupValues[1].trim().ifEmpty { null }
    }

    companion object {
        const val SELECTED_URI_KEY = "selected_uri"
        const val READ_MARKER = "读取"
        const val ENGLISH_READ_MARKER = "read"
        private val ENGLISH_READ_REGEX = Regex("^\\s*read\\s+(.+)$", RegexOption.IGNORE_CASE)
        const val MAX_LIST_ENTRIES = 100
        const val MAX_READ_BYTES = 256 * 1024L
    }
}
