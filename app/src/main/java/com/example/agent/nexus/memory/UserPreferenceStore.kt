package com.example.agent.nexus.memory

import com.example.agent.data.local.AgentMemory
import com.example.agent.data.local.AgentMemoryDao
import java.io.StringReader
import java.io.StringWriter
import java.util.Base64
import java.util.Properties

/**
 * Local-first preference storage backed by the existing generic AgentMemory table.
 * The Nexus contract stays independent from Room and can be replaced later.
 */
class UserPreferenceStore(
    private val dao: AgentMemoryDao
) {
    suspend fun get(key: String): UserPreference? {
        validateKey(key)
        return dao.findLatest(storageKey(key))?.let(::decode)
    }

    suspend fun set(preference: UserPreference) {
        validateKey(preference.key)
        dao.insert(
            AgentMemory(
                key = storageKey(preference.key),
                value = encode(preference),
                timestamp = preference.updatedAt
            )
        )
    }

    suspend fun remove(key: String) {
        validateKey(key)
        dao.deleteByKey(storageKey(key))
    }

    suspend fun list(): List<UserPreference> {
        return dao.findByKeyPrefix(STORAGE_PREFIX)
            .asSequence()
            .map(::decode)
            .distinctBy { it.key }
            .toList()
    }

    private fun encode(preference: UserPreference): String {
        val properties = Properties()
        properties.setProperty("key", preference.key)
        properties.setProperty("value", preference.value)
        properties.setProperty("updatedAt", preference.updatedAt.toString())
        preference.metadata.forEach { (key, value) ->
            properties.setProperty("metadata.$key", value)
        }
        val writer = StringWriter()
        properties.store(writer, null)
        return Base64.getEncoder().encodeToString(writer.toString().toByteArray(Charsets.UTF_8))
    }

    private fun decode(memory: AgentMemory): UserPreference {
        val properties = Properties()
        val decoded = String(Base64.getDecoder().decode(memory.value), Charsets.UTF_8)
        properties.load(StringReader(decoded))
        val metadata = properties.stringPropertyNames()
            .filter { it.startsWith("metadata.") }
            .associate { it.removePrefix("metadata.") to properties.getProperty(it) }
        return UserPreference(
            key = properties.getProperty("key"),
            value = properties.getProperty("value"),
            metadata = metadata,
            updatedAt = properties.getProperty("updatedAt")?.toLongOrNull() ?: memory.timestamp
        )
    }

    private fun storageKey(key: String): String = "$STORAGE_PREFIX$key"

    private fun validateKey(key: String) {
        require(key.isNotBlank()) { "preference key must not be blank" }
    }

    private companion object {
        const val STORAGE_PREFIX = "user.preference."
    }
}
