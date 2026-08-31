package com.example.service

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Task event types triggered across the agent workspace
 */
enum class TaskCompletedEventType {
    CODE_RUN,               // Executed / checked code snippet
    DOCUMENT_EDIT,          // Modified or drafted a document / markdown
    GITHUB_COMMIT_PUSH,     // Committed & pushed code to GitHub
    GITHUB_PR_CREATE,       // Created a pull request
    GITHUB_ISSUE_SUBMIT,    // Submitted or resolved an issue
    SYSTEM_AUTOMATION_EXEC, // Ran shell/automation task
    CHAT_MESSAGE_RESOLVED   // Answered user query with skill
}

data class TaskCompletionEvent(
    val eventType: TaskCompletedEventType,
    val taskTitle: String,
    val detailSummary: String,
    val relatedSkillId: String? = null,
    val targetRepo: String? = null,
    val xpGain: Int = 35,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Background Task Observer & Event Bus
 * Observes task completions in background (e.g. document edits, code execution, GitHub pushes)
 * and automatically triggers skill progress increments and logs activity to AI Moments.
 */
object TaskCompletionObserver {
    private const val TAG = "TaskCompletionObserver"

    private val _eventsFlow = MutableSharedFlow<TaskCompletionEvent>(extraBufferCapacity = 64)
    val eventsFlow = _eventsFlow.asSharedFlow()

    // Registered listener callbacks
    private val callbacks = mutableListOf<(TaskCompletionEvent) -> Unit>()

    fun registerCallback(callback: (TaskCompletionEvent) -> Unit) {
        synchronized(callbacks) {
            if (!callbacks.contains(callback)) {
                callbacks.add(callback)
            }
        }
    }

    fun unregisterCallback(callback: (TaskCompletionEvent) -> Unit) {
        synchronized(callbacks) {
            callbacks.remove(callback)
        }
    }

    /**
     * Broadcast task completed event.
     * Invoked when user runs code, finishes editing docs, pushes to GitHub, etc.
     */
    fun notifyTaskCompleted(
        eventType: TaskCompletedEventType,
        taskTitle: String,
        detailSummary: String,
        relatedSkillId: String? = null,
        targetRepo: String? = null,
        xpGain: Int = 35
    ) {
        val event = TaskCompletionEvent(
            eventType = eventType,
            taskTitle = taskTitle,
            detailSummary = detailSummary,
            relatedSkillId = relatedSkillId,
            targetRepo = targetRepo,
            xpGain = xpGain
        )

        Log.d(TAG, "Task completed: [${event.eventType}] ${event.taskTitle} (Skill: ${event.relatedSkillId}, +${event.xpGain} XP)")

        // Emit to flow
        _eventsFlow.tryEmit(event)

        // Invoke direct callbacks
        synchronized(callbacks) {
            callbacks.forEach { callback ->
                try {
                    callback(event)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in task callback", e)
                }
            }
        }
    }
}
