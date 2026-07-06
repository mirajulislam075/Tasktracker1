package com.miraj.tasktracker.ui.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miraj.tasktracker.data.model.Assignor
import com.miraj.tasktracker.data.model.Category
import com.miraj.tasktracker.data.model.Priority
import com.miraj.tasktracker.data.model.ReminderPreset
import com.miraj.tasktracker.data.model.Task
import com.miraj.tasktracker.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EditTaskUiState(
    val taskId: Long? = null,
    val title: String = "",
    val category: Category = Category.WORK,
    val assignor: String = "",
    val priority: Priority = Priority.MEDIUM,
    val deadlineEpochMs: Long? = null,
    val notes: String = "",
    val selectedReminders: Set<Long> = emptySet(), // minutesBefore keys
    val isEdit: Boolean = false,
    val saved: Boolean = false
)

class EditTaskViewModel(
    private val repo: TaskRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(EditTaskUiState())
    val ui: StateFlow<EditTaskUiState> = _ui

    val assignors: StateFlow<List<Assignor>> = repo.observeActiveAssignors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun loadTask(id: Long) {
        viewModelScope.launch {
            val t = repo.getTask(id) ?: return@launch
            val reminders = repo.getRemindersForTaskOnce(id)
            _ui.value = _ui.value.copy(
                taskId = t.id,
                title = t.title,
                category = t.category,
                assignor = t.assignor,
                priority = t.priority,
                deadlineEpochMs = t.deadlineEpochMs,
                notes = t.notes,
                selectedReminders = reminders.map { it.minutesBefore }.toSet(),
                isEdit = true
            )
        }
    }

    fun updateTitle(v: String) { _ui.value = _ui.value.copy(title = v) }
    fun updateCategory(v: Category) { _ui.value = _ui.value.copy(category = v) }
    fun updateAssignor(v: String) { _ui.value = _ui.value.copy(assignor = v) }
    fun updatePriority(v: Priority) { _ui.value = _ui.value.copy(priority = v) }
    fun updateDeadline(v: Long?) { _ui.value = _ui.value.copy(deadlineEpochMs = v) }
    fun updateNotes(v: String) { _ui.value = _ui.value.copy(notes = v) }
    fun toggleReminder(minutesBefore: Long) {
        val cur = _ui.value.selectedReminders
        _ui.value = _ui.value.copy(
            selectedReminders = if (cur.contains(minutesBefore)) cur - minutesBefore else cur + minutesBefore
        )
    }

    fun save() {
        val s = _ui.value
        if (s.title.isBlank()) return
        viewModelScope.launch {
            val task = Task(
                id = s.taskId ?: 0L,
                title = s.title.trim(),
                category = s.category,
                assignor = s.assignor.trim(),
                priority = s.priority,
                deadlineEpochMs = s.deadlineEpochMs,
                notes = s.notes.trim()
            )
            val presets = ReminderPreset.PRESETS.filter { it.minutesBefore in s.selectedReminders }
            repo.saveTask(task, presets)
            _ui.value = _ui.value.copy(saved = true)
        }
    }
}
