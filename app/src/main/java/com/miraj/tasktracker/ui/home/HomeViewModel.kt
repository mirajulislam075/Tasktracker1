package com.miraj.tasktracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miraj.tasktracker.data.model.Category
import com.miraj.tasktracker.data.model.Task
import com.miraj.tasktracker.data.model.TaskStatus
import com.miraj.tasktracker.data.repository.TaskRepository
import com.miraj.tasktracker.util.taskState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryCount(
    val category: Category,
    val overdue: Int,
    val today: Int,
    val upcoming: Int,
    val open: Int
)

data class HomeUiState(
    val tasks: List<Task> = emptyList(),
    val counts: List<CategoryCount> = emptyList(),
    val filter: HomeFilter = HomeFilter.OPEN,
    val categoryFilter: Category? = null
) {
    val filtered: List<Task> get() {
        val scoped = if (categoryFilter != null) tasks.filter { it.category == categoryFilter } else tasks
        return when (filter) {
            HomeFilter.OPEN -> scoped.filter { !it.completed }
            HomeFilter.OVERDUE -> scoped.filter { !it.completed && taskState(it.deadlineEpochMs, false) == "overdue" }
            HomeFilter.TODAY -> scoped.filter { !it.completed && taskState(it.deadlineEpochMs, false) == "today" }
            HomeFilter.UPCOMING -> scoped.filter { !it.completed && taskState(it.deadlineEpochMs, false) == "upcoming" }
            HomeFilter.SOMEDAY -> scoped.filter { !it.completed && it.deadlineEpochMs == null }
            HomeFilter.DONE -> scoped.filter { it.completed }
        }.sortedWith(compareBy(
            { if (it.completed) 1 else 0 },
            {
                when (taskState(it.deadlineEpochMs, it.completed)) {
                    "overdue" -> 0; "today" -> 1; "upcoming" -> 2; "someday" -> 3; else -> 4
                }
            },
            { it.deadlineEpochMs ?: Long.MAX_VALUE }
        ))
    }
}

enum class HomeFilter(val label: String) {
    OPEN("Open"),
    OVERDUE("Overdue"),
    TODAY("Today"),
    UPCOMING("Upcoming"),
    SOMEDAY("Someday"),
    DONE("Done")
}

class HomeViewModel(private val repo: TaskRepository) : ViewModel() {
    private val filter = MutableStateFlow(HomeFilter.OPEN)
    private val categoryFilter = MutableStateFlow<Category?>(null)

    val state: StateFlow<HomeUiState> = combine(
        repo.observeTasks(), filter, categoryFilter
    ) { tasks, f, cf ->
        HomeUiState(
            tasks = tasks,
            counts = buildCounts(tasks),
            filter = f,
            categoryFilter = cf
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun setFilter(f: HomeFilter) { filter.value = f }
    fun toggleCategory(c: Category) {
        categoryFilter.value = if (categoryFilter.value == c) null else c
    }
    fun clearCategoryFilter() { categoryFilter.value = null }

    fun quickComplete(task: Task) {
        viewModelScope.launch {
            val next = if (task.completed) TaskStatus.NOT_STARTED else TaskStatus.DONE
            repo.setStatus(task.id, next, if (next == TaskStatus.DONE) "Marked done" else "Re-opened", "manual")
        }
    }

    private fun buildCounts(tasks: List<Task>): List<CategoryCount> {
        return Category.entries.map { cat ->
            val open = tasks.filter { it.category == cat && !it.completed }
            var overdue = 0; var today = 0; var upcoming = 0
            open.forEach {
                when (taskState(it.deadlineEpochMs, false)) {
                    "overdue" -> overdue++
                    "today" -> today++
                    "upcoming" -> upcoming++
                }
            }
            CategoryCount(cat, overdue, today, upcoming, open.size)
        }
    }
}
