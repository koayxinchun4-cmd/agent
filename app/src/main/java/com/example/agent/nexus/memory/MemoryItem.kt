package com.example.agent.nexus.memory

/**
 * Platform-neutral representation of one memory item exposed to Agent Core.
 *
 * Concrete stores can keep their own domain models; Agent Core consumes this
 * normalized shape without depending on Room or a specific storage format.
 */
data class MemoryItem(
    val scope: MemoryScope,
    val key: String,
    val value: String,
    val metadata: Map<String, String> = emptyMap(),
    val updatedAt: Long = System.currentTimeMillis()
)
