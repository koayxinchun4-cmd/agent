package com.example.agent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.agent.data.local.AppDatabase
import com.example.agent.data.remote.GeminiApiService
import com.example.agent.data.repository.ChatRepository
import com.example.agent.ui.screen.ChatViewModel
import com.example.agent.ui.theme.AgentTheme
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化依赖
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "agent-db").build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        val apiService = retrofit.create(GeminiApiService::class.java)
        val repository = ChatRepository(db.chatDao(), apiService)

        setContent {
            AgentTheme {
                val viewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(repository))
                ChatScreen(viewModel)
            }
        }
    }
}

class ChatViewModelFactory(private val repository: ChatRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(repository) as T
    }
}
