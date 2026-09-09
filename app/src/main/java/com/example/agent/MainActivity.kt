package com.example.agent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.agent.data.local.AppDatabase
import com.example.agent.data.remote.GeminiApiService
import com.example.agent.data.repository.ChatRepository
import com.example.agent.nexus.agent.NexusAgent
import com.example.agent.nexus.skill.SkillRegistry
import com.example.agent.nexus.tool.AppAgentTool
import com.example.agent.nexus.tool.LocalFileTool
import com.example.agent.nexus.tool.LocalTaskTool
import com.example.agent.nexus.tool.MemoryTool
import com.example.agent.nexus.tool.SkillTool
import com.example.agent.nexus.tool.ToolRegistry
import com.example.agent.ui.screen.ChatViewModel
import com.example.agent.ui.screen.NexusApp
import com.example.agent.ui.screen.TaskViewModel
import com.example.agent.ui.theme.AgentTheme
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS agent_memory (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "key TEXT NOT NULL, " +
                "value TEXT NOT NULL, " +
                "timestamp INTEGER NOT NULL)"
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "nexus-db"
        ).addMigrations(MIGRATION_1_2).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val repository = ChatRepository(
            db.chatDao(),
            retrofit.create(GeminiApiService::class.java)
        )

        val skillRegistry = SkillRegistry(File(filesDir, "skills"))
        runCatching {
            val bundled = assets.open("skills/android-ci-agent/SKILL.md")
                .bufferedReader()
                .use { it.readText() }
            skillRegistry.installIfMissing("android-ci-agent", bundled)
        }

        val toolRegistry = ToolRegistry(
            listOf(
                LocalTaskTool(),
                LocalFileTool(filesDir),
                AppAgentTool(applicationContext),
                MemoryTool(db.memoryDao()),
                SkillTool(skillRegistry)
            )
        )
        val nexusAgent = NexusAgent(toolRegistry)

        setContent {
            AgentTheme {
                val chatViewModel: ChatViewModel = viewModel(
                    factory = ChatViewModelFactory(repository)
                )
                val taskViewModel: TaskViewModel = viewModel(
                    factory = TaskViewModelFactory(nexusAgent)
                )
                NexusApp(chatViewModel, taskViewModel)
            }
        }
    }
}

class ChatViewModelFactory(
    private val repository: ChatRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(repository) as T
    }
}

class TaskViewModelFactory(
    private val agent: NexusAgent
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TaskViewModel(agent) as T
    }
}
