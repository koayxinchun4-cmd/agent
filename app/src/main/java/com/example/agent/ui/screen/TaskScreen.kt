package com.example.agent.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
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
import com.example.agent.nexus.agent.AgentResult
import com.example.agent.nexus.agent.AgentStepResult

private enum class TaskStepStatus { DONE, ACTIVE, PENDING, FAILED }

private data class TaskStep(
    val title: String,
    val detail: String,
    val status: TaskStepStatus
)

@Composable
fun TaskScreen(
    initialPrompt: String = "",
    onBack: () -> Unit,
    viewModel: TaskViewModel
) {
    var prompt by remember { mutableStateOf(initialPrompt) }
    val uiState by viewModel.uiState.collectAsState()
    val running = uiState is TaskUiState.Running
    val finished = uiState is TaskUiState.Completed || uiState is TaskUiState.Failed
    val started = running || finished

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Nexus Task", style = MaterialTheme.typography.headlineSmall)
                Text(
                    if (started) "Agent workflow" else "Give Nexus a goal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(onClick = onBack) { Text("返回") }
        }

        if (!started) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("What should Nexus do?", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        placeholder = { Text("例如：帮我分析这个 Android CI 失败的原因") }
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.startTask(prompt) },
                        enabled = prompt.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) { Text("Start task") }
                }
            }
        } else {
            val execution = when (val state = uiState) {
                is TaskUiState.Running -> state.execution
                is TaskUiState.Completed -> state.execution
                is TaskUiState.Failed -> state.execution
                TaskUiState.Idle -> null
            }
            val taskText = when (val state = uiState) {
                is TaskUiState.Running -> state.task.input
                is TaskUiState.Completed -> state.task.input
                is TaskUiState.Failed -> state.task.input
                TaskUiState.Idle -> prompt
            }

            TaskSummary(taskText, uiState)
            Text("Agent workflow", style = MaterialTheme.typography.titleLarge)
            taskSteps(execution).forEachIndexed { index, step ->
                TaskStepCard(step, isLast = index == taskSteps(execution).lastIndex)
            }

            CurrentAction(execution, uiState)

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

            OutlinedButton(
                onClick = {
                    if (running) viewModel.stopTask() else viewModel.reset()
                    prompt = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (running) "停止任务" else "新建任务") }
        }
    }
}

@Composable
private fun TaskSummary(text: String, state: TaskUiState) {
    val status = when (state) {
        is TaskUiState.Running -> "运行中 · Agent 正在执行"
        is TaskUiState.Completed -> "已完成"
        is TaskUiState.Failed -> "执行失败"
        TaskUiState.Idle -> "准备中"
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

private fun taskSteps(execution: AgentExecution?): List<TaskStep> {
    val steps = execution?.steps.orEmpty()
    val has = { name: String -> steps.any { it.step == name || it.step.startsWith(name) } }
    val failed = { name: String -> steps.lastOrNull { it.step == name || it.step.startsWith(name) }?.success == false }
    val active = if (execution == null) "understand_request" else null
    return listOf(
        TaskStep("理解需求", "分析目标与约束", if (has("understand_request")) TaskStepStatus.DONE else TaskStepStatus.ACTIVE),
        TaskStep("制定计划", "拆分任务与选择执行路径", if (has("use_tool:") || has("answer")) TaskStepStatus.DONE else if (active == null) TaskStepStatus.ACTIVE else TaskStepStatus.PENDING),
        TaskStep("执行工具", "调用可用 Tools / Models", when { failed("use_tool:") -> TaskStepStatus.FAILED; has("use_tool:") -> TaskStepStatus.DONE; else -> TaskStepStatus.PENDING }),
        TaskStep("验证结果", "检查执行结果是否满足目标", if (has("verify")) if (failed("verify")) TaskStepStatus.FAILED else TaskStepStatus.DONE else TaskStepStatus.PENDING),
        TaskStep("完成", "整理最终结果并交给你", if (has("answer")) if (failed("answer")) TaskStepStatus.FAILED else TaskStepStatus.DONE else TaskStepStatus.PENDING)
    )
}

@Composable
private fun CurrentAction(execution: AgentExecution?, state: TaskUiState) {
    val action = when {
        state is TaskUiState.Completed -> "Task completed"
        state is TaskUiState.Failed -> "Task failed"
        execution == null -> "Starting Agent…"
        execution.steps.lastOrNull()?.step?.startsWith("use_tool:") == true -> "Executing tool…"
        execution.steps.lastOrNull()?.step == "verify" -> "Verifying result…"
        else -> "Planning next action…"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text("Current action", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Text(action, style = MaterialTheme.typography.titleMedium)
            execution?.plan?.let {
                Spacer(Modifier.height(8.dp))
                Text("Route: ${it.route} · Tool: ${it.toolId ?: "none"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TaskStepCard(step: TaskStep, isLast: Boolean) {
    val (symbol, containerColor) = when (step.status) {
        TaskStepStatus.DONE -> "✓" to MaterialTheme.colorScheme.secondaryContainer
        TaskStepStatus.ACTIVE -> "●" to MaterialTheme.colorScheme.primaryContainer
        TaskStepStatus.PENDING -> "○" to MaterialTheme.colorScheme.surfaceVariant
        TaskStepStatus.FAILED -> "!" to MaterialTheme.colorScheme.errorContainer
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(36.dp).background(containerColor, CircleShape), contentAlignment = Alignment.Center) {
                Text(symbol, style = MaterialTheme.typography.titleMedium)
            }
            if (!isLast) {
                Box(modifier = Modifier.padding(vertical = 3.dp).size(width = 2.dp, height = 28.dp).background(MaterialTheme.colorScheme.outlineVariant))
            }
        }
        Spacer(Modifier.size(12.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 13.dp)) {
                Text(step.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(3.dp))
                Text(step.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
