package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.AgentRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Nexus 智能助手", appName)
  }

  @Test
  fun `office agents and default repo test`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = AgentRepository(context)
    val agents = repository.getOfficeAgentsList()
    assertTrue(agents.isNotEmpty())
    assertNotNull(agents.find { it.id == "agent_codex_dev" })
    assertEquals("google/mesop", repository.getDefaultGitHubRepo())
  }

  @Test
  fun `agent live activity types test`() {
    val types = com.example.data.api.AgentActivityType.values()
    assertTrue(types.any { it == com.example.data.api.AgentActivityType.COMMITTING })
    assertTrue(types.any { it == com.example.data.api.AgentActivityType.PUSHING })
    assertTrue(types.any { it == com.example.data.api.AgentActivityType.PR_REVIEWING })
  }
}
