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
import com.example.agent.BuildConfig
import com.example.agent.data.local.AppDatabase
import com.example.agent.data.remote.GeminiApiService
import com.example.agent.data.remote.OpenRouterApiService
import com.example.agent.data.repository.ChatRepository
import com.example.agent.nexus.agent.GeminiModelProvider
import com.example.agent.nexus.agent.ModelAvailability
import com.example.agent.nexus.agent.ModelProviderRegistry
import com.example.agent.nexus.agent.ModelRouter
import com.example.agent.nexus.agent.NexusAgent
import com.example.agent.nexus.agent.OpenRouterModelProvider
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

        val moshiFactory = MoshiConverterFactory.create()
        val geminiRetrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(moshiFactory)
            .build()
        val openRouterRetrofit = Retrofit.Builder()
            .baseUrl("https://openrouter.ai/")
            .addConverterFactory(moshiFactory)
            .build()

        val repository = ChatRepository(
            db.chatDao(),
            geminiRetrofit.create(GeminiApiService::class.java)
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

        val geminiProvider = GeminiModelProvider(
            apiService = geminiRetrofit.create(GeminiApiService::class.java),
            apiKey = BuildConfig.GEMINI_API_KEY
        )
        val openRouterProvider = OpenRouterModelProvider(
            apiService = openRouterRetrofit.create(OpenRouterApiService::class.java),
            apiKey = BuildConfig.OPENROUTER_API_KEY
        )
        val providerRegistry = ModelProviderRegistry(
            listOf(
                geminiProvider,
                openRouterProvider,
                com.example.agent.nexus.agent.LocalModelProvider()
            )
        )
        val modelRouter = ModelRouter(
            ModelAvailability(
                geminiAvailable = geminiProvider.isAvailable,
                openRouterAvailable = openRouterProvider.isAvailable
            )
        )
        val nexusAgent = NexusAgent(
            toolRegistry = toolRegistry,
            modelRouter = modelRouter,
            modelProviders = providerRegistry
        )

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
