package com.example.agent.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agent.nexus.agent.AgentExecution
import com.example.agent.nexus.agent.AgentProgress
import com.example.agent.nexus.agent.AgentResult
import com.example.agent.nexus.agent.AgentTask
import com.example.agent.nexus.agent.NexusAgent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface TaskUiState {
    data object Idle : TaskUiState
    data class Running(
        val task: AgentTask,
        val progress: AgentProgress? = null,
        val execution: AgentExecution? = null
    ) : TaskUiState
    data class Completed(val task: AgentTask, val execution: AgentExecution) : TaskUiState
    data class Failed(val task: AgentTask, val execution: AgentExecution) : TaskUiState
}

class TaskViewModel(
    private val agent: NexusAgent
) : ViewModel() {
    private val _uiState = MutableStateFlow<TaskUiState>(TaskUiState.Idle)
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    private var executionJob: Job? = null

    fun startTask(input: String, metadata: Map<String, String> = emptyMap()) {
        val prompt = input.trim()
        if (prompt.isEmpty()) return

        executionJob?.cancel()
        val task = AgentTask(
            id = UUID.randomUUID().toString(),
            input = prompt,
            metadata = metadata
        )
        _uiState.value = TaskUiState.Running(task)

        executionJob = viewModelScope.launch {
            val execution = agent.executeDetailed(task) { progress ->
                _uiState.value = TaskUiState.Running(
                    task = task,
                    progress = progress,
                    execution = null
                )
            }
            _uiState.value = when (execution.result) {
                is AgentResult.Success -> TaskUiState.Completed(task, execution)
                is AgentResult.Failure -> TaskUiState.Failed(task, execution)
            }
        }
    }

    fun stopTask() {
        executionJob?.cancel()
        executionJob = null
        _uiState.value = TaskUiState.Idle
    }

    fun reset() {
        stopTask()
    }
}
