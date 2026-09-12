package com.example.agent.nexus.memory

import android.util.Base64
import com.example.agent.data.local.AgentMemory
import com.example.agent.data.local.AgentMemoryDao
import java.io.StringWriter
import java.util.Properties

/**
 * Local-first persistence for task-scoped memory.
 *
 * The store deliberately uses the generic AgentMemory table so the memory
 * infrastructure remains reusable without coupling Task Memory to a new
 * database schema.
 */
class TaskMemoryStore(
    private val dao: AgentMemoryDao
) {
    suspend fun get(taskId: String, key: String): TaskMemory? {
        validateScope(taskId, key)
        return dao.findLatest(storageKey(taskId, key))?.toTaskMemory()
    }

    suspend fun set(memory: TaskMemory) {
        validateScope(memory.taskId, memory.key)
        dao.insert(
            AgentMemory(
                key = storageKey(memory.taskId, memory.key),
                value = encode(memory.value, memory.metadata),
                timestamp = memory.updatedAt
            )
        )
    }

    suspend fun remove(taskId: String, key: String) {
        validateScope(taskId, key)
        dao.deleteByKey(storageKey(taskId, key))
    }

    suspend fun list(taskId: String): List<TaskMemory> {
        require(taskId.isNotBlank()) { "taskId must not be blank" }
        return dao.findByKeyPrefix(storagePrefix(taskId))
            .mapNotNull { it.toTaskMemory() }
            .distinctBy { it.key }
    }

    private fun AgentMemory.toTaskMemory(): TaskMemory? {
        val parsed = parseKey(key) ?: return null
        val (value, metadata) = decode(value) ?: return null
        return TaskMemory(
            taskId = parsed.first,
            key = parsed.second,
            value = value,
            metadata = metadata,
            updatedAt = timestamp
        )
    }

    private fun storageKey(taskId: String, key: String): String =
        storagePrefix(taskId) + encodeSegment(key)

    private fun storagePrefix(taskId: String): String =
        PREFIX + encodeSegment(taskId) + "."

    private fun parseKey(storageKey: String): Pair<String, String>? {
        if (!storageKey.startsWith(PREFIX)) return null
        val remainder = storageKey.removePrefix(PREFIX)
        val separator = remainder.indexOf('.')
        if (separator <= 0 || separator == remainder.lastIndex) return null
        val taskId = decodeSegment(remainder.substring(0, separator)) ?: return null
        val key = decodeSegment(remainder.substring(separator + 1)) ?: return null
        return taskId to key
    }

    private fun encode(value: String, metadata: Map<String, String>): String {
        val properties = Properties().apply {
            setProperty("value", value)
            setProperty("metadata", encodeMetadata(metadata))
        }
        return StringWriter().also { writer ->
            properties.store(writer, null)
        }.toString()
    }

    private fun decode(value: String): Pair<String, Map<String, String>>? = runCatching {
        val properties = Properties().apply { load(value.reader()) }
        val content = properties.getProperty("value") ?: return null
        val metadata = decodeMetadata(properties.getProperty("metadata").orEmpty())
        content to metadata
    }.getOrNull()

    private fun encodeMetadata(metadata: Map<String, String>): String {
        val properties = Properties()
        metadata.forEach { (key, value) -> properties.setProperty(key, value) }
        val writer = StringWriter()
        properties.store(writer, null)
        return Base64.encodeToString(writer.toString().toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

    private fun decodeMetadata(value: String): Map<String, String> {
        if (value.isBlank()) return emptyMap()
        return runCatching {
            val properties = Properties().apply {
                load(Base64.decode(value, Base64.NO_WRAP).inputStream())
            }
            properties.stringPropertyNames().associateWith(properties::getProperty)
        }.getOrDefault(emptyMap())
    }

    private fun encodeSegment(value: String): String =
        Base64.encodeToString(value.toByteArray(Charsets.UTF_8), Base64.URL_SAFE or Base64.NO_WRAP)

    private fun decodeSegment(value: String): String? = runCatching {
        String(Base64.decode(value, Base64.URL_SAFE or Base64.NO_WRAP), Charsets.UTF_8)
    }.getOrNull()

    private fun validateScope(taskId: String, key: String) {
        require(taskId.isNotBlank()) { "taskId must not be blank" }
        require(key.isNotBlank()) { "key must not be blank" }
    }

    private companion object {
        const val PREFIX = "task.memory."
    }
}
