package com.example.agent.nexus.tool

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.agent.nexus.agent.AgentTask

/** Opens an installed Android app by package name after the user explicitly asks Nexus to do so. */
class AppAgentTool(
    private val context: Context
) : AgentTool {
    override val id: String = "app_agent"
    override val name: String = "App Agent"
    override val description: String = "Open an installed Android app by its package name."

    override suspend fun execute(task: AgentTask): ToolResult {
        val packageName = task.metadata["package"]?.trim().orEmpty()
        if (packageName.isBlank()) {
            return ToolResult.Failure("Missing app package name. Provide metadata['package']." )
        }
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                ?: return ToolResult.Failure("App is not installed or has no launch activity: $packageName")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            ToolResult.Success("已開啟 App：$packageName")
        } catch (error: SecurityException) {
            ToolResult.Failure("Android denied opening the app: $packageName", error)
        } catch (error: Exception) {
            ToolResult.Failure("無法開啟 App：$packageName", error)
        }
    }

    fun isInstalled(packageName: String): Boolean = try {
        context.packageManager.getApplicationInfo(packageName, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }
}
