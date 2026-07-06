package com.miraj.tasktracker.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miraj.tasktracker.data.model.Reminder
import com.miraj.tasktracker.data.model.StatusUpdate
import com.miraj.tasktracker.data.model.Task
import com.miraj.tasktracker.data.model.TaskStatus
import com.miraj.tasktracker.data.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetailUiState(
    val task: Task? = null,
    val reminders: List<Reminder> = emptyList(),
    val updates: List<StatusUpdate> = emptyList()
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class TaskDetailViewModel(private val repo: TaskRepository) : ViewModel() {

    private val taskId = MutableStateFlow<Long?>(null)

    val task: StateFlow<Task?> = taskId.flatMapLatest { id ->
        if (id == null) kotlinx.coroutines.flow.flowOf(null) else repo.observeTask(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val reminders: StateFlow<List<Reminder>> = taskId.flatMapLatest { id ->
        if (id == null) kotlinx.coroutines.flow.flowOf(emptyList()) else repo.observeRemindersForTask(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val updates: StateFlow<List<StatusUpdate>> = taskId.flatMapLatest { id ->
        if (id == null) kotlinx.coroutines.flow.flowOf(emptyList()) else repo.observeStatusUpdates(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun load(id: Long) { taskId.value = id }

    fun setStatus(status: TaskStatus, note: String) {
        val id = taskId.value ?: return
        viewModelScope.launch {
            repo.setStatus(id, status, note, "manual")
        }
    }

    fun delete(onDone: () -> Unit) {
        val id = taskId.value ?: return
        viewModelScope.launch {
            repo.deleteTask(id)
            onDone()
        }
    }
}
