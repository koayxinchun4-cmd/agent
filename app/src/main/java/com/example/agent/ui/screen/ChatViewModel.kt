package com.example.agent.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agent.data.local.ChatMessage
import com.example.agent.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending

    init {
        viewModelScope.launch {
            repository.getAllMessages().collect { list ->
                _messages.value = list
            }
        }
    }

    fun sendMessage(text: String) {
        if (_isSending.value) return
        viewModelScope.launch {
            _isSending.value = true
            try {
                repository.sendMessage(text)
            } catch (e: Exception) {
                val message = e.message ?: e.javaClass.simpleName
                repository.saveAssistantMessage("Nexus 请求失败：$message")
            } finally {
                _isSending.value = false
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
