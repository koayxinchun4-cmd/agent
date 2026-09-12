package com.example.agent.ui.screen

enum class NexusTimelineStatus { DONE, ACTIVE, PENDING, FAILED }

internal fun nexusTimelineStatus(
    hasCompleted: Boolean,
    isActive: Boolean,
    hasFailed: Boolean
): NexusTimelineStatus = when {
    hasFailed -> NexusTimelineStatus.FAILED
    hasCompleted -> NexusTimelineStatus.DONE
    isActive -> NexusTimelineStatus.ACTIVE
    else -> NexusTimelineStatus.PENDING
}
