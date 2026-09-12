package com.example.agent.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.agent.nexus.agent.AgentExecution
import com.example.agent.nexus.agent.AgentProgress
import com.example.agent.nexus.agent.AgentResult
import com.example.agent.nexus.agent.AgentStepResult
import com.example.agent.nexus.tool.AppAgentTool
import com.example.agent.nexus.tool.LocalFileTool

@Composable
fun TaskScreen(
    initialPrompt: String = "",
    onBack: () -> Unit,
    viewModel: TaskViewModel
) {
    var prompt by remember { mutableStateOf(initialPrompt) }
    var appPackage by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val running = uiState is TaskUiState.Running
    val pendingConfirmation = uiState is TaskUiState.PendingConfirmation
    val finished = uiState is TaskUiState.Completed || uiState is TaskUiState.Failed
    val started = running || pendingConfirmation || finished
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            prompt = "读取并分析我选取的文件"
            viewModel.startTask(prompt, metadata = mapOf(LocalFileTool.SELECTED_URI_KEY to uri.toString()))
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Nexus Task", style = MaterialTheme.typography.headlineSmall)
                Text(if (started) "Agent workflow" else "Give Nexus a goal", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedButton(onClick = onBack) { Text("Back") }
        }

        if (!started) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(20.dp)) {
                    Text("What should Nexus do?", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(value = prompt, onValueChange = { prompt = it }, modifier = Modifier.fillMaxWidth(), minLines = 4, placeholder = { Text("例如：帮我分析这个 Android CI 失败的原因") })
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.startTask(prompt) }, enabled = prompt.isNotBlank(), modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 14.dp)) { Text("Start task") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { filePicker.launch(arrayOf("text/*", "application/json", "application/xml")) }, modifier = Modifier.fillMaxWidth()) { Text("选择文件并交给 Nexus") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = appPackage, onValueChange = { appPackage = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("App package name") }, placeholder = { Text("例如：com.android.chrome") }, supportingText = { Text("只会开启你明确指定的已安装 App") })
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = {
                            val packageName = appPackage.trim()
                            prompt = "開啟 package:$packageName"
                            viewModel.startTask(prompt, metadata = mapOf(AppAgentTool.PACKAGE_KEY to packageName))
                        },
                        enabled = appPackage.trim().isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("開啟指定 App") }
                }
            }
        } else {
            val execution = when (val state = uiState) {
                is TaskUiState.Running -> state.execution
                is TaskUiState.Completed -> state.execution
                is TaskUiState.Failed -> state.execution
                is TaskUiState.PendingConfirmation -> null
                TaskUiState.Idle -> null
            }
            val progress = (uiState as? TaskUiState.Running)?.progress
            val liveSteps = progress?.steps.orEmpty()
            val taskText = when (val state = uiState) {
                is TaskUiState.Running -> state.task.input
                is TaskUiState.Completed -> state.task.input
                is TaskUiState.Failed -> state.task.input
                is TaskUiState.PendingConfirmation -> state.task.input
                TaskUiState.Idle -> prompt
            }

            TaskSummary(taskText, uiState)
            if (uiState is TaskUiState.PendingConfirmation) {
                val state = uiState as TaskUiState.PendingConfirmation
                NexusConfirmationCard(toolName = state.request.tool.name, action = state.request.tool.description, onCancel = viewModel::cancelConfirmation, onConfirm = viewModel::confirmTask)
            } else {
                NexusAgentTimeline(execution = execution, progress = progress)
                CurrentAction(execution, progress, uiState)
                if (running && liveSteps.isNotEmpty()) {
                    Text("Live execution", style = MaterialTheme.typography.titleMedium)
                    liveSteps.takeLast(3).forEach { step -> LiveStepRow(step) }
                }
                if (finished) {
                    val resultText = when (val state = uiState) {
                        is TaskUiState.Completed -> (state.execution.result as AgentResult.Success).text
                        is TaskUiState.Failed -> (state.execution.result as AgentResult.Failure).message
                        else -> ""
                    }
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                        Column(Modifier.padding(18.dp)) {
                            Text("Result", style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.height(6.dp))
                            Text(resultText, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    when {
                        running -> viewModel.stopTask()
                        pendingConfirmation -> viewModel.cancelConfirmation()
                        else -> viewModel.reset()
                    }
                    prompt = ""
                    appPackage = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (running) "Stop task" else if (pendingConfirmation) "Cancel task" else "New task") }
        }
    }
}

@Composable
private fun TaskSummary(text: String, state: TaskUiState) {
    val status = when (state) {
        is TaskUiState.Running -> "Running · Agent is executing"
        is TaskUiState.PendingConfirmation -> "Waiting for your confirmation"
        is TaskUiState.Completed -> "Completed"
        is TaskUiState.Failed -> "Execution failed"
        TaskUiState.Idle -> "Ready"
    }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(18.dp)) {
            Text("Task", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Text(text, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(14.dp))
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(status, modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun CurrentAction(execution: AgentExecution?, progress: AgentProgress?, state: TaskUiState) {
    val liveStep = progress?.step?.step
    val action = when {
        state is TaskUiState.Completed -> "Task completed"
        state is TaskUiState.Failed -> "Task failed"
        liveStep == "understand_request" -> "Understanding request…"
        liveStep == "plan" -> "Planning execution path…"
        liveStep?.startsWith("use_tool:") == true -> "Executing ${liveStep.substringAfter("use_tool:").substringBefore("#attempt")}…"
        liveStep == "verify" -> "Verifying result…"
        liveStep == "answer" -> "Preparing final answer…"
        execution == null -> "Starting Agent…"
        else -> "Planning next action…"
    }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(18.dp)) {
            Text("Current action", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Text(action, style = MaterialTheme.typography.titleMedium)
            execution?.plan?.let {
                Spacer(Modifier.height(8.dp))
                Text("Route: ${it.route} · Tool: ${it.toolId ?: "none"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            progress?.let {
                Spacer(Modifier.height(6.dp))
                Text(it.step.output, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LiveStepRow(step: AgentStepResult) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(if (step.success) "✓" else "!", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.size(10.dp))
        Column(Modifier.weight(1f)) {
            Text(step.step, style = MaterialTheme.typography.bodyMedium)
            Text(step.output, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
