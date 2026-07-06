package com.miraj.tasktracker.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miraj.tasktracker.data.model.TaskStatus
import com.miraj.tasktracker.ui.common.CategoryBadge
import com.miraj.tasktracker.ui.common.ChoiceChip
import com.miraj.tasktracker.ui.common.PriorityChip
import com.miraj.tasktracker.ui.theme.Amber
import com.miraj.tasktracker.ui.theme.Ink
import com.miraj.tasktracker.ui.theme.InkSoft
import com.miraj.tasktracker.ui.theme.Line
import com.miraj.tasktracker.ui.theme.Paper
import com.miraj.tasktracker.ui.theme.Paper2
import com.miraj.tasktracker.ui.theme.Rust
import com.miraj.tasktracker.util.formatDeadline
import com.miraj.tasktracker.util.formatTimestamp
import com.miraj.tasktracker.util.taskState

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    viewModel: TaskDetailViewModel,
    taskId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit
) {
    LaunchedEffect(taskId) { viewModel.load(taskId) }
    val task by viewModel.task.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val updates by viewModel.updates.collectAsStateWithLifecycle()
    var showStatusSheet by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { task?.let { onEdit(it.id) } }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = InkSoft)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = InkSoft)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper)
            )
        },
        containerColor = Paper
    ) { padding ->
        val t = task ?: return@Scaffold
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                t.title,
                style = MaterialTheme.typography.headlineMedium,
                textDecoration = if (t.completed) TextDecoration.LineThrough else null,
                color = if (t.completed) InkSoft else Ink
            )
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryBadge(t.category)
                if (t.priority != com.miraj.tasktracker.data.model.Priority.LOW) {
                    Spacer(Modifier.width(10.dp))
                    PriorityChip(t.priority)
                }
            }

            Spacer(Modifier.height(20.dp))

            InfoRow("Status", t.status.label)
            if (t.assignor.isNotBlank()) InfoRow("Assigned by / for", t.assignor)
            InfoRow("Deadline", formatDeadline(t.deadlineEpochMs))
            if (t.notes.isNotBlank()) InfoRow("Notes", t.notes, multiline = true)

            Spacer(Modifier.height(20.dp))
            SectionHeader("Reminders")
            if (reminders.isEmpty()) {
                Text("No reminders set.", style = MaterialTheme.typography.bodySmall, color = InkSoft)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    reminders.forEach { r ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .width(6.dp).height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (r.fired) InkSoft else Amber)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(r.label, style = MaterialTheme.typography.bodyMedium, color = Ink)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (r.fired) "sent" else formatTimestamp(r.fireAtEpochMs),
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSoft
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceChip(
                    label = if (t.completed) "Reopen" else "Mark done",
                    selected = false,
                    onClick = {
                        viewModel.setStatus(
                            if (t.completed) TaskStatus.NOT_STARTED else TaskStatus.DONE,
                            if (t.completed) "Reopened" else "Marked done"
                        )
                    }
                )
                ChoiceChip(
                    label = "Update status",
                    selected = false,
                    onClick = { showStatusSheet = true }
                )
            }

            Spacer(Modifier.height(24.dp))
            SectionHeader("Timeline")
            if (updates.isEmpty()) {
                Text(
                    "No updates yet. Reminders and manual updates will show here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    updates.forEach { u ->
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    u.status.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Ink,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    formatTimestamp(u.epochMs),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkSoft
                                )
                            }
                            if (u.note.isNotBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Text(u.note, style = MaterialTheme.typography.bodyMedium, color = Ink)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    if (showStatusSheet) {
        StatusUpdateDialog(
            currentStatus = task?.status ?: TaskStatus.NOT_STARTED,
            onDismiss = { showStatusSheet = false },
            onSubmit = { s, n -> viewModel.setStatus(s, n); showStatusSheet = false }
        )
    }
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete task?") },
            text = { Text("This cannot be undone. All reminders and history for this task will be removed.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.delete(onDeleted)
                }) { Text("Delete", color = Rust) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String, multiline: Boolean = false) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = InkSoft
        )
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = Ink,
            maxLines = if (multiline) Int.MAX_VALUE else 3
        )
    }
}

@Composable
private fun SectionHeader(label: String) {
    Text(
        label.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = InkSoft,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun StatusUpdateDialog(
    currentStatus: TaskStatus,
    onDismiss: () -> Unit,
    onSubmit: (TaskStatus, String) -> Unit
) {
    var status by remember { mutableStateOf(currentStatus) }
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("How's it going?") },
        text = {
            Column {
                Text("Status", style = MaterialTheme.typography.labelMedium, color = InkSoft)
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TaskStatus.entries.forEach { s ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { status = s }
                                .padding(vertical = 6.dp)
                        ) {
                            Box(
                                Modifier
                                    .width(16.dp).height(16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (status == s) Ink else Color.Transparent)
                                    .border(1.5.dp, if (status == s) Ink else InkSoft, RoundedCornerShape(8.dp))
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(s.label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("Add a note (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(3.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Ink,
                        unfocusedBorderColor = Line,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = Ink,
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(status, note) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = Color.White
    )
}
