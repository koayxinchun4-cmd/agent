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
            ?: return ToolResult.Failure("Please provide a valid App package name, for example: package:com.example.app")
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

        /** Pure Kotlin contract for the only Intent shape App Agent is allowed to launch. */
        data class LaunchIntentSpec(
            val packageName: String,
            val action: String = Intent.ACTION_MAIN,
            val category: String = Intent.CATEGORY_LAUNCHER,
            val flags: Int = Intent.FLAG_ACTIVITY_NEW_TASK
        )

        fun buildLaunchIntentSpec(packageName: String): LaunchIntentSpec? {
            val normalizedPackageName = packageName.trim()
            if (!isValidPackageName(normalizedPackageName)) return null
            return LaunchIntentSpec(packageName = normalizedPackageName)
        }

        /** Converts the validated, testable contract into the Android Intent used at runtime. */
        fun buildLaunchIntent(packageName: String): Intent? {
            val spec = buildLaunchIntentSpec(packageName) ?: return null
            return Intent(spec.action).apply {
                addCategory(spec.category)
                setPackage(spec.packageName)
                addFlags(spec.flags)
            }
        }
    }
}
