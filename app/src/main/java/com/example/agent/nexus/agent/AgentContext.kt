package com.example.agent.nexus.agent

/** Observation emitted by a tool, verifier, model, or Android capability. */
data class AgentObservation(
    val source: String,
    val summary: String,
    val success: Boolean = true
)

/** Small bounded context store to keep long-running mobile tasks inexpensive. */
class AgentContextStore(
    private val maxObservations: Int = 32,
    private val maxSummaryLength: Int = 1200
) {
    private val observations = ArrayDeque<AgentObservation>()

    fun add(observation: AgentObservation) {
        observations += observation.copy(summary = observation.summary.take(maxSummaryLength))
        while (observations.size > maxObservations) observations.removeFirst()
    }

    fun snapshot(): List<AgentObservation> = observations.toList()

    /** Returns a compact prompt-safe representation without mutating the store. */
    fun compact(separator: String = "\n"): String = observations.joinToString(separator) {
        "[${it.source}] ${it.summary}"
    }
}
