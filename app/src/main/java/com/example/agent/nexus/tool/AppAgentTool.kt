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

    override suspend fun execute(task: AgentTask): ToolResult {
        val packageName = extractPackageName(task)
            ?: return ToolResult.Failure("請提供要開啟的 App package name，例如：package:com.example.app")

        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                ?: return ToolResult.Failure("找不到可開啟的 App：$packageName")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            ToolResult.Success("已開啟 App：$packageName")
        } catch (error: SecurityException) {
            ToolResult.Failure("Android 拒絕開啟 App：$packageName", error)
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

    companion object {
        const val PACKAGE_KEY = "package"
        private val PACKAGE_PATTERN = Regex("(?:package:|套件:)\\s*([A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)+)")

        fun extractPackageName(task: AgentTask): String? {
            return task.metadata[PACKAGE_KEY]?.trim()
                ?.takeIf { it.matches(PACKAGE_PATTERN) }
                ?: PACKAGE_PATTERN.find(task.input)?.groupValues?.get(1)
        }
    }
}
