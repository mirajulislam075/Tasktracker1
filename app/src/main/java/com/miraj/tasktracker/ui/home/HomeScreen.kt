package com.miraj.tasktracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miraj.tasktracker.data.model.Category
import com.miraj.tasktracker.data.model.Task
import com.miraj.tasktracker.ui.common.CategoryBadge
import com.miraj.tasktracker.ui.common.PriorityChip
import com.miraj.tasktracker.ui.theme.Amber
import com.miraj.tasktracker.ui.theme.Ink
import com.miraj.tasktracker.ui.theme.InkSoft
import com.miraj.tasktracker.ui.theme.Line
import com.miraj.tasktracker.ui.theme.LineSoft
import com.miraj.tasktracker.ui.theme.Paper
import com.miraj.tasktracker.ui.theme.Paper2
import com.miraj.tasktracker.ui.theme.Rust
import com.miraj.tasktracker.ui.theme.RustSoft
import com.miraj.tasktracker.util.formatDeadline
import com.miraj.tasktracker.util.taskState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddTask: () -> Unit,
    onOpenTask: (Long) -> Unit,
    onOpenSettings: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val todayLabel = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Tasks", style = MaterialTheme.typography.headlineLarge)
                        Text(
                            todayLabel.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = InkSoft
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = InkSoft)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTask,
                containerColor = Ink,
                contentColor = Paper,
                shape = RoundedCornerShape(4.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add task")
            }
        },
        containerColor = Paper
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Category board — the signature
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.counts.forEach { cc ->
                    CategoryCard(
                        count = cc,
                        active = state.categoryFilter == cc.category,
                        onClick = { viewModel.toggleCategory(cc.category) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Filter tabs
            FilterRow(
                current = state.filter,
                counts = state,
                onSelect = viewModel::setFilter
            )

            // Optional category filter banner
            if (state.categoryFilter != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Paper2)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Filtered to ${state.categoryFilter!!.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSoft
                    )
                    Text(
                        "Show all",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { viewModel.clearCategoryFilter() }
                    )
                }
            }

            val list = state.filtered
            if (list.isEmpty()) {
                EmptyState(state.filter)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(list, key = { it.id }) { task ->
                        TaskRow(
                            task = task,
                            onToggle = { viewModel.quickComplete(task) },
                            onClick = { onOpenTask(task.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    count: CategoryCount,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasOverdue = count.overdue > 0
    val bg = if (hasOverdue) RustSoft else Color.White
    val borderColor = when {
        active && hasOverdue -> Rust
        active -> Ink
        hasOverdue -> Rust
        else -> Line
    }
    val (big, bigLabel, alarm) = when {
        count.overdue > 0 -> Triple(count.overdue, "overdue", true)
        count.today > 0 -> Triple(count.today, "due today", false)
        count.open > 0 -> Triple(count.open, "open", false)
        else -> Triple(0, "clear", false)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(bg)
            .border(
                width = if (active) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(3.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Text(
                count.category.label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = if (hasOverdue) Rust else InkSoft
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    big.toString(),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        alarm -> Rust
                        big == 0 -> Color(0xFFB8B3A5)
                        else -> Ink
                    }
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    bigLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (alarm) Rust else InkSoft,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            Text(
                "${count.today} today · ${count.upcoming} upcoming",
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft
            )
        }
    }
}

@Composable
private fun FilterRow(
    current: HomeFilter,
    counts: HomeUiState,
    onSelect: (HomeFilter) -> Unit
) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp)
    ) {
        HomeFilter.entries.forEach { f ->
            val active = f == current
            val n = countFor(counts, f)
            Column(
                modifier = Modifier
                    .clickable { onSelect(f) }
                    .padding(end = 20.dp, top = 6.dp, bottom = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        f.label,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (active) Ink else InkSoft
                    )
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (active) Ink else Paper2)
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            n.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (active) Paper else InkSoft
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .width(if (active) 32.dp else 0.dp)
                        .background(Ink)
                )
            }
        }
    }
    // Bottom line under the whole strip
    Box(Modifier.fillMaxWidth().height(1.dp).background(Line))
}

private fun countFor(state: HomeUiState, f: HomeFilter): Int {
    val scoped = if (state.categoryFilter != null) state.tasks.filter { it.category == state.categoryFilter } else state.tasks
    return when (f) {
        HomeFilter.OPEN -> scoped.count { !it.completed }
        HomeFilter.OVERDUE -> scoped.count { !it.completed && taskState(it.deadlineEpochMs, false) == "overdue" }
        HomeFilter.TODAY -> scoped.count { !it.completed && taskState(it.deadlineEpochMs, false) == "today" }
        HomeFilter.UPCOMING -> scoped.count { !it.completed && taskState(it.deadlineEpochMs, false) == "upcoming" }
        HomeFilter.SOMEDAY -> scoped.count { !it.completed && it.deadlineEpochMs == null }
        HomeFilter.DONE -> scoped.count { it.completed }
    }
}

@Composable
private fun TaskRow(task: Task, onToggle: () -> Unit, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Custom checkbox
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .width(20.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(if (task.completed) Ink else Color.White)
                .border(1.5.dp, if (task.completed) Ink else InkSoft, RoundedCornerShape(3.dp))
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            if (task.completed) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Paper, modifier = Modifier.width(14.dp).height(14.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                task.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (task.completed) InkSoft else Ink
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryBadge(task.category)
                if (task.assignor.isNotBlank()) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        task.assignor,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSoft
                    )
                }
            }
            if (task.deadlineEpochMs != null) {
                Spacer(Modifier.height(3.dp))
                val state = taskState(task.deadlineEpochMs, task.completed)
                val (dot, dlColor) = when (state) {
                    "overdue" -> "● " to Rust
                    "today" -> "● " to Amber
                    else -> "" to InkSoft
                }
                Text(
                    "$dot${formatDeadline(task.deadlineEpochMs)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = dlColor,
                    fontWeight = if (state == "overdue" || state == "today") FontWeight.SemiBold else FontWeight.Normal
                )
            }
            if (task.priority != com.miraj.tasktracker.data.model.Priority.LOW) {
                Spacer(Modifier.height(3.dp))
                PriorityChip(task.priority)
            }
        }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(LineSoft))
}

@Composable
private fun EmptyState(filter: HomeFilter) {
    val msg = when (filter) {
        HomeFilter.OPEN -> "No open tasks. Tap + to add one."
        HomeFilter.OVERDUE -> "Nothing overdue. Nice."
        HomeFilter.TODAY -> "Nothing due today."
        HomeFilter.UPCOMING -> "No upcoming tasks."
        HomeFilter.SOMEDAY -> "No undated tasks."
        HomeFilter.DONE -> "No completed tasks yet."
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("—", fontSize = 36.sp, color = Line)
        Spacer(Modifier.height(8.dp))
        Text(msg, style = MaterialTheme.typography.bodyMedium, color = InkSoft)
    }
}

