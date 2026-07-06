package com.miraj.tasktracker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miraj.tasktracker.data.model.Assignor
import com.miraj.tasktracker.data.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repo: TaskRepository) : ViewModel() {
    val assignors: StateFlow<List<Assignor>> = repo.observeAllAssignors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun archive(a: Assignor) = viewModelScope.launch { repo.archiveAssignor(a.id, !a.archived) }
    fun delete(a: Assignor) = viewModelScope.launch { repo.deleteAssignor(a.id) }
    fun rename(a: Assignor, newName: String) = viewModelScope.launch {
        if (newName.isNotBlank() && newName != a.name) repo.renameAssignor(a, newName.trim())
    }
}
