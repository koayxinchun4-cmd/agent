package com.example.agent.nexus.memory

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
        return encodeSegment(writer.toString())
    }

    private fun decodeMetadata(value: String): Map<String, String> {
        if (value.isBlank()) return emptyMap()
        return runCatching {
            val serialized = decodeSegment(value) ?: return emptyMap()
            val properties = Properties().apply { load(serialized.reader()) }
            properties.stringPropertyNames().associateWith(properties::getProperty)
        }.getOrDefault(emptyMap())
    }

    /**
     * Hex encoding is intentionally used instead of an Android-specific API.
     * It is deterministic, platform-neutral, and keeps storage segments free
     * of '.' and '%' characters that could interfere with key parsing or LIKE.
     */
    private fun encodeSegment(value: String): String =
        value.toByteArray(Charsets.UTF_8).joinToString(separator = "") { byte ->
            "%02x".format(byte.toInt() and 0xff)
        }

    private fun decodeSegment(value: String): String? = runCatching {
        require(value.length % 2 == 0) { "hex value must have an even length" }
        val bytes = ByteArray(value.length / 2)
        value.chunked(2).forEachIndexed { index, pair ->
            bytes[index] = pair.toInt(16).toByte()
        }
        String(bytes, Charsets.UTF_8)
    }.getOrNull()

    private fun validateScope(taskId: String, key: String) {
        require(taskId.isNotBlank()) { "taskId must not be blank" }
        require(key.isNotBlank()) { "key must not be blank" }
    }

    private companion object {
        const val PREFIX = "task.memory."
    }
}
