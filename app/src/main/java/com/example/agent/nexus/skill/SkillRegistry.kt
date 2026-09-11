package com.example.agent.nexus.skill

import java.io.File

class SkillRegistry(
    private val rootDirectory: File,
    private val provenanceStore: SkillProvenanceStore = SkillProvenanceStore(rootDirectory)
) {
    fun list(): List<SkillDocument> = skillFiles()
        .mapNotNull { file -> runCatching { read(file) }.getOrNull() }
        .sortedBy { it.id }

    fun get(id: String): SkillDocument? = skillFiles()
        .firstOrNull { it.parentFile?.name == id }
        ?.let { runCatching { read(it) }.getOrNull() }

    fun getProvenance(id: String): SkillProvenance? = provenanceStore.get(id)

    fun isEnabled(id: String): Boolean = get(id) != null && stateFile(id).let { file ->
        !file.isFile || runCatching { file.readText(Charsets.UTF_8).trim() == ENABLED }.getOrDefault(false)
    }

    fun enable(id: String) {
        require(get(id) != null) { "skill not found: $id" }
        stateFile(id).writeText(ENABLED, Charsets.UTF_8)
    }

    fun disable(id: String) {
        require(get(id) != null) { "skill not found: $id" }
        stateFile(id).writeText(DISABLED, Charsets.UTF_8)
    }

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

    fun install(id: String, content: String, provenance: SkillProvenance): SkillDocument {
        require(provenance.skillId == id) { "provenance skill id mismatch" }
        val document = install(id, content)
        provenanceStore.save(provenance)
        return document
    }

    fun installIfMissing(id: String, content: String): SkillDocument =
        get(id) ?: install(id, content)

    fun installIfMissing(id: String, content: String, provenance: SkillProvenance): SkillDocument =
        get(id) ?: install(id, content, provenance)

    private fun skillFiles(): List<File> = rootDirectory.listFiles()
        .orEmpty()
        .filter { it.isDirectory }
        .map { File(it, "SKILL.md") }
        .filter { it.isFile && it.length() <= MAX_SKILL_BYTES }

    private fun read(file: File): SkillDocument =
        SkillParser.parse(file.parentFile?.name ?: "unknown", file.readText(Charsets.UTF_8))

    private fun stateFile(id: String): File = File(rootDirectory, id).resolve(STATE_FILE)

    private companion object {
        const val MAX_SKILL_BYTES = 256 * 1024L
        const val STATE_FILE = ".enabled"
        const val ENABLED = "enabled"
        const val DISABLED = "disabled"
    }
}
