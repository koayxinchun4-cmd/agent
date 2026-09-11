package com.example.agent.nexus.skill

import java.io.File
import java.util.Properties

class SkillProvenanceStore(
    private val rootDirectory: File
) {
    fun get(skillId: String): SkillProvenance? {
        val file = provenanceFile(skillId)
        if (!file.isFile) return null
        return runCatching {
            val properties = Properties().apply {
                file.inputStream().use(::load)
            }
            SkillProvenance(
                skillId = properties.required("skillId"),
                skillName = properties.required("skillName"),
                sourceRepository = properties.required("sourceRepository"),
                sourcePath = properties.required("sourcePath"),
                sourceCommitOrVersion = properties.required("sourceCommitOrVersion"),
                license = properties.required("license"),
                originalAuthor = properties.required("originalAuthor"),
                importDate = properties.required("importDate"),
                modificationStatus = properties.required("modificationStatus"),
                nexusChanges = properties.required("nexusChanges"),
                removalStatus = properties.required("removalStatus"),
                contentSha256 = properties.getProperty("contentSha256")?.takeIf { it.isNotBlank() }
            )
        }.getOrNull()
    }

    fun save(provenance: SkillProvenance) {
        val directory = File(rootDirectory, provenance.skillId)
        directory.mkdirs()
        val properties = Properties().apply {
            setProperty("skillId", provenance.skillId)
            setProperty("skillName", provenance.skillName)
            setProperty("sourceRepository", provenance.sourceRepository)
            setProperty("sourcePath", provenance.sourcePath)
            setProperty("sourceCommitOrVersion", provenance.sourceCommitOrVersion)
            setProperty("license", provenance.license)
            setProperty("originalAuthor", provenance.originalAuthor)
            setProperty("importDate", provenance.importDate)
            setProperty("modificationStatus", provenance.modificationStatus)
            setProperty("nexusChanges", provenance.nexusChanges)
            setProperty("removalStatus", provenance.removalStatus)
            provenance.contentSha256?.takeIf { it.isNotBlank() }?.let {
                setProperty("contentSha256", it)
            }
        }
        provenanceFile(provenance.skillId).outputStream().use { properties.store(it, "Nexus Skill provenance") }
    }

    private fun provenanceFile(skillId: String): File = File(rootDirectory, skillId).resolve(PROVENANCE_FILE)

    private fun Properties.required(key: String): String =
        getProperty(key)?.takeIf { it.isNotBlank() } ?: throw IllegalStateException("missing provenance field: $key")

    private companion object {
        const val PROVENANCE_FILE = ".provenance.properties"
    }
}
