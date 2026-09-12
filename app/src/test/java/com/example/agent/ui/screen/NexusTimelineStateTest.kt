package com.example.agent.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Test

class NexusTimelineStateTest {
    @Test
    fun failed_state_takes_priority() {
        assertEquals(
            NexusTimelineStatus.FAILED,
            nexusTimelineStatus(hasCompleted = true, isActive = true, hasFailed = true)
        )
    }

    @Test
    fun completed_state_takes_priority_over_active() {
        assertEquals(
            NexusTimelineStatus.DONE,
            nexusTimelineStatus(hasCompleted = true, isActive = true, hasFailed = false)
        )
    }

    @Test
    fun active_state_is_used_when_not_completed() {
        assertEquals(
            NexusTimelineStatus.ACTIVE,
            nexusTimelineStatus(hasCompleted = false, isActive = true, hasFailed = false)
        )
    }

    @Test
    fun pending_is_the_default_state() {
        assertEquals(
            NexusTimelineStatus.PENDING,
            nexusTimelineStatus(hasCompleted = false, isActive = false, hasFailed = false)
        )
    }
}
