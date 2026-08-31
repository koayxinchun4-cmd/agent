package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import com.example.data.api.AgentActivityType
import com.example.data.api.AgentLiveActivity
import com.example.data.api.OfficeAgent
import com.example.ui.components.OfficeStatusBar
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun officeStatusBar_screenshot() {
    val sampleAgents = listOf(
      OfficeAgent(
        id = "agent_codex_dev",
        name = "Codex 研发助理",
        roleTitle = "全栈代码攻坚",
        avatarEmoji = "👨‍💻",
        description = "负责代码补丁与 Commit",
        capabilities = listOf("Git Commit", "PR"),
        defaultPromptTemplate = "请编写补丁"
      )
    )

    val activities = mapOf(
      "agent_codex_dev" to AgentLiveActivity(
        agentId = "agent_codex_dev",
        activityType = AgentActivityType.COMMITTING,
        activityTitle = "正在提交代码补丁",
        detailLog = "准备提交 README.md",
        targetRepo = "google/mesop",
        targetBranch = "main",
        progress = 0.5f,
        isBusy = true
      )
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        OfficeStatusBar(
          officeAgents = sampleAgents,
          liveActivities = activities,
          currentRepo = "google/mesop",
          currentBranch = "main",
          isGitHubLoading = false,
          onOpenOfficeStudio = {},
          onDispatchAgent = { _, _ -> },
          onExecuteQuickCommitPush = { _, _, _, _ -> }
        )
      }
    }

    composeTestRule.onNodeWithTag("office_status_bar").assertExists()
    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/office_status_bar.png")
  }
}

