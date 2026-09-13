package com.example.agent.nexus.agent

/** Event types for the mobile execution timeline and future tracing sinks. */
sealed interface AgentEvent {
    val sessionId: String
    val timestampEpochMillis: Long

    data class StateChanged(
        override val sessionId: String,
        val from: AgentRuntimeState,
        val to: AgentRuntimeState,
        val reason: String? = null,
        override val timestampEpochMillis: Long = System.currentTimeMillis()
    ) : AgentEvent

    data class ObservationRecorded(
        override val sessionId: String,
        val observation: AgentObservation,
        override val timestampEpochMillis: Long = System.currentTimeMillis()
    ) : AgentEvent

    data class ToolCompleted(
        override val sessionId: String,
        val toolId: String,
        val success: Boolean,
        override val timestampEpochMillis: Long = System.currentTimeMillis()
    ) : AgentEvent
}

fun interface AgentEventSink {
    fun emit(event: AgentEvent)
}
