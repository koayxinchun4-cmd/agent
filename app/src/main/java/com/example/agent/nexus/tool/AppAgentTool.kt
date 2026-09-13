package com.example.agent.nexus.tool

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.agent.nexus.agent.AgentTask

/** Opens an installed Android app only when the task explicitly supplies a validated package name. */
class AppAgentTool(
    private val context: Context
) : AgentTool {
    override val id: String = "app_agent"
    override val name: String = "App Agent"
    override val description: String = "Open an installed Android app using an explicit package name."
    override val riskLevel: RiskLevel = RiskLevel.REQUIRES_CONFIRMATION

    override suspend fun execute(task: AgentTask): ToolResult {
        val packageName = extractPackageName(task)
            ?: return ToolResult.Failure("Please provide a valid App package name, for example: package:com.example.app")

        val intent = buildLaunchIntent(packageName)
        return try {
            if (context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) == null) {
                return ToolResult.Failure("App cannot be opened: $packageName")
            }
            context.startActivity(intent)
            ToolResult.Success("Opened App: $packageName")
        } catch (error: SecurityException) {
            ToolResult.Failure("Android rejected opening App: $packageName", error)
        } catch (error: Exception) {
            ToolResult.Failure("Unable to open App: $packageName", error)
        }
    }

    fun isInstalled(packageName: String): Boolean {
        if (!isValidPackageName(packageName)) return false
        return try {
            context.packageManager.getApplicationInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    companion object {
        const val PACKAGE_KEY = "package"
        private val PACKAGE_NAME_PATTERN = Regex("[A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)+")
        private val PACKAGE_PATTERN = Regex("(?:package:|套件:)\\s*([A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)+)")

        fun isValidPackageName(packageName: String): Boolean =
            PACKAGE_NAME_PATTERN.matches(packageName.trim())

        fun extractPackageName(task: AgentTask): String? {
            return task.metadata[PACKAGE_KEY]?.trim()
                ?.takeIf(::isValidPackageName)
                ?: PACKAGE_PATTERN.find(task.input)?.groupValues?.get(1)
        }

        /** Builds the only Intent shape App Agent is allowed to launch. */
        fun buildLaunchIntent(packageName: String): Intent? {
            if (!isValidPackageName(packageName)) return null
            return Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(packageName.trim())
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }
}
