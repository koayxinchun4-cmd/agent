package com.example.agent.nexus.skill

import java.io.File

class SkillRegistry(
    private val rootDirectory: File
) {
    fun list(): List<SkillDocument> = skillFiles()
        .mapNotNull { file -> runCatching { read(file) }.getOrNull() }
        .sortedBy { it.id }

    fun get(id: String): SkillDocument? = skillFiles()
        .firstOrNull { it.parentFile?.name == id }
        ?.let { runCatching { read(it) }.getOrNull() }

    fun install(id: String, content: String): SkillDocument {
        require(id.matches(Regex("[a-zA-Z0-9._-]+"))) { "invalid skill id" }
        val root = rootDirectory.canonicalFile
        val directory = File(root, id).canonicalFile
        require(directory.path.startsWith(root.path + File.separator))
        directory.mkdirs()
        val file = File(directory, "SKILL.md")
        file.writeText(content, Charsets.UTF_8)
        return SkillParser.parse(id, content)
    }

    fun installIfMissing(id: String, content: String): SkillDocument =
        get(id) ?: install(id, content)

    private fun skillFiles(): List<File> = rootDirectory.listFiles()
        .orEmpty()
        .filter { it.isDirectory }
        .map { File(it, "SKILL.md") }
        .filter { it.isFile && it.length() <= MAX_SKILL_BYTES }

    private fun read(file: File): SkillDocument =
        SkillParser.parse(file.parentFile?.name ?: "unknown", file.readText(Charsets.UTF_8))

    private companion object {
        const val MAX_SKILL_BYTES = 256 * 1024L
    }
}
