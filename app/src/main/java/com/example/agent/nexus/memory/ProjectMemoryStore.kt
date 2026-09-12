package com.example.agent.nexus.memory

import com.example.agent.data.local.AgentMemory
import com.example.agent.data.local.AgentMemoryDao
import java.io.StringReader
import java.io.StringWriter
import java.util.Base64
import java.util.Properties

/**
 * Local-first project memory storage backed by the generic AgentMemory table.
 * The Nexus contract remains independent from Room and the storage encoding is internal.
 */
class ProjectMemoryStore(
    private val dao: AgentMemoryDao
) {
    suspend fun get(projectId: String, key: String): ProjectMemory? {
        validateProjectId(projectId)
        validateKey(key)
        return dao.findLatest(storageKey(projectId, key))?.let(::decode)
    }

    suspend fun set(memory: ProjectMemory) {
        validateProjectId(memory.projectId)
        validateKey(memory.key)
        dao.insert(
            AgentMemory(
                key = storageKey(memory.projectId, memory.key),
                value = encode(memory),
                timestamp = memory.updatedAt
            )
        )
    }

    suspend fun remove(projectId: String, key: String) {
        validateProjectId(projectId)
        validateKey(key)
        dao.deleteByKey(storageKey(projectId, key))
    }

    suspend fun list(projectId: String): List<ProjectMemory> {
        validateProjectId(projectId)
        val prefix = storagePrefix(projectId)
        return dao.findByKeyPrefix(prefix)
            .asSequence()
            .map(::decode)
            .distinctBy { it.key }
            .toList()
    }

    private fun encode(memory: ProjectMemory): String {
        val properties = Properties()
        properties.setProperty("projectId", memory.projectId)
        properties.setProperty("key", memory.key)
        properties.setProperty("value", memory.value)
        properties.setProperty("updatedAt", memory.updatedAt.toString())
        memory.metadata.forEach { (key, value) ->
            properties.setProperty("metadata.$key", value)
        }
        val writer = StringWriter()
        properties.store(writer, null)
        return Base64.getEncoder().encodeToString(writer.toString().toByteArray(Charsets.UTF_8))
    }

    private fun decode(memory: AgentMemory): ProjectMemory {
        val properties = Properties()
        val decoded = String(Base64.getDecoder().decode(memory.value), Charsets.UTF_8)
        properties.load(StringReader(decoded))
        val metadata = properties.stringPropertyNames()
            .filter { it.startsWith("metadata.") }
            .associate { it.removePrefix("metadata.") to properties.getProperty(it) }
        return ProjectMemory(
            projectId = properties.getProperty("projectId"),
            key = properties.getProperty("key"),
            value = properties.getProperty("value"),
            metadata = metadata,
            updatedAt = properties.getProperty("updatedAt")?.toLongOrNull() ?: memory.timestamp
        )
    }

    private fun storageKey(projectId: String, key: String): String =
        "${storagePrefix(projectId)}${encodeSegment(key)}"

    private fun storagePrefix(projectId: String): String =
        "$STORAGE_PREFIX${encodeSegment(projectId)}."

    private fun encodeSegment(value: String): String =
        Base64.getUrlEncoder().withoutPadding()
            .encodeToString(value.toByteArray(Charsets.UTF_8))

    private fun validateProjectId(projectId: String) {
        require(projectId.isNotBlank()) { "project id must not be blank" }
    }

    private fun validateKey(key: String) {
        require(key.isNotBlank()) { "project memory key must not be blank" }
    }

    private companion object {
        const val STORAGE_PREFIX = "project.memory."
    }
}
