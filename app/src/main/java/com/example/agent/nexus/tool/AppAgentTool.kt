package com.example.agent.nexus.tool

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.agent.nexus.agent.AgentTask

/** Opens an installed Android app only when the task explicitly supplies a package name. */
class AppAgentTool(
    private val context: Context
) : AgentTool {
    override val id: String = "app_agent"
    override val name: String = "App Agent"
    override val description: String = "Open an installed Android app using an explicit package name."
    override val riskLevel: RiskLevel = RiskLevel.REQUIRES_CONFIRMATION

    override suspend fun execute(task: AgentTask): ToolResult {
        val packageName = extractPackageName(task)
            ?: return ToolResult.Failure("Please provide an App package name, for example: package:com.example.app")

        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                ?: return ToolResult.Failure("App cannot be opened: $packageName")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            ToolResult.Success("Opened App: $packageName")
        } catch (error: SecurityException) {
            ToolResult.Failure("Android rejected opening App: $packageName", error)
        } catch (error: Exception) {
            ToolResult.Failure("Unable to open App: $packageName", error)
        }
    }

    fun isInstalled(packageName: String): Boolean = try {
        context.packageManager.getApplicationInfo(packageName, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    companion object {
        const val PACKAGE_KEY = "package"
        private val PACKAGE_NAME_PATTERN = Regex("[A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)+")
        private val PACKAGE_PATTERN = Regex("(?:package:|套件:)\\s*([A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)+)")

        fun extractPackageName(task: AgentTask): String? {
            return task.metadata[PACKAGE_KEY]?.trim()
                ?.takeIf { PACKAGE_NAME_PATTERN.matches(it) }
                ?: PACKAGE_PATTERN.find(task.input)?.groupValues?.get(1)
        }
    }
}
