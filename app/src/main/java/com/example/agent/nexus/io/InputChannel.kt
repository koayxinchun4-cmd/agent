package com.example.agent.nexus.io

/**
 * Receives external events and exposes them as channel-neutral messages.
 * Implementations may represent text, voice, files, schedules, or other sources.
 */
interface InputChannel {
    val id: String

    suspend fun receive(): AgentMessage?
}
