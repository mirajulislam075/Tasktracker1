package com.miraj.tasktracker.ui.edit

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miraj.tasktracker.data.model.Category
import com.miraj.tasktracker.data.model.DeadlinePreset
import com.miraj.tasktracker.data.model.Priority
import com.miraj.tasktracker.data.model.ReminderPreset
import com.miraj.tasktracker.ui.common.ChoiceChip
import com.miraj.tasktracker.ui.theme.Ink
import com.miraj.tasktracker.ui.theme.InkSoft
import com.miraj.tasktracker.ui.theme.Line
import com.miraj.tasktracker.ui.theme.Paper
import com.miraj.tasktracker.ui.theme.Paper2
import com.miraj.tasktracker.util.endOfDay
import com.miraj.tasktracker.util.formatDeadline
import java.util.Calendar

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    viewModel: EditTaskViewModel,
    taskId: Long?,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    val state by viewModel.ui.collectAsStateWithLifecycle()
    val assignors by viewModel.assignors.collectAsStateWithLifecycle()

    LaunchedEffect(taskId) {
        if (taskId != null && taskId > 0 && state.taskId == null) viewModel.loadTask(taskId)
    }
    LaunchedEffect(state.saved) { if (state.saved) onDone() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEdit) "Edit task" else "New task", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.save() },
                        enabled = state.title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Paper),
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(if (state.isEdit) "Save" else "Add")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper)
            )
        },
        containerColor = Paper
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Title
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::updateTitle,
                placeholder = { Text("What needs doing?") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(3.dp),
                colors = fieldColors()
            )

            SectionHeader("Category")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Category.entries.forEach { c ->
                    ChoiceChip(
                        label = c.label,
                        selected = state.category == c,
                        onClick = { viewModel.updateCategory(c) }
                    )
                }
            }

            SectionHeader("Assigned by / for")
            AssignorField(
                value = state.assignor,
                options = assignors.map { it.name },
                onChange = viewModel::updateAssignor
            )

            SectionHeader("Priority")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.entries.forEach { p ->
                    ChoiceChip(
                        label = p.label,
                        selected = state.priority == p,
                        onClick = { viewModel.updatePriority(p) }
                    )
                }
            }

            SectionHeader("Deadline")
            DeadlineSection(
                current = state.deadlineEpochMs,
                onChange = viewModel::updateDeadline
            )

            SectionHeader("Reminders")
            if (state.deadlineEpochMs == null) {
                Text(
                    "Set a deadline to enable reminders.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSoft
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ReminderPreset.PRESETS.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pair.forEach { p ->
                                ChoiceChip(
                                    label = p.label,
                                    selected = p.minutesBefore in state.selectedReminders,
                                    onClick = { viewModel.toggleReminder(p.minutesBefore) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pair.size == 1) Box(Modifier.weight(1f))
                        }
                    }
                }
            }

            SectionHeader("Notes")
            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::updateNotes,
                placeholder = { Text("Optional context or details") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(3.dp),
                colors = fieldColors()
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(label: String) {
    Spacer(Modifier.height(20.dp))
    Text(
        label.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = InkSoft,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun AssignorField(
    value: String,
    options: List<String>,
    onChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val filtered = remember(value, options) {
        if (value.isBlank()) options else options.filter { it.contains(value, ignoreCase = true) && it != value }
    }
    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onChange(it)
                expanded = it.isNotBlank() && filtered.isNotEmpty()
            },
            placeholder = { Text("e.g. Operations Manager, Wife, Self") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().clickable {
                if (options.isNotEmpty()) expanded = true
            },
            shape = RoundedCornerShape(3.dp),
            colors = fieldColors()
        )
        DropdownMenu(
            expanded = expanded && filtered.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            filtered.take(6).forEach { name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = { onChange(name); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun DeadlineSection(current: Long?, onChange: (Long?) -> Unit) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            item {
                ChoiceChip(
                    label = "None",
                    selected = current == null,
                    onClick = { onChange(null) }
                )
            }
            items(DeadlinePreset.PRESETS.size) { i ->
                val p = DeadlinePreset.PRESETS[i]
                val epoch = endOfDay(p.daysFromNow)
                val isSelected = current != null && sameDayAsPreset(current, epoch)
                ChoiceChip(
                    label = p.label,
                    selected = isSelected,
                    onClick = { onChange(epoch) }
                )
            }
            item {
                ChoiceChip(
                    label = "Pick date & time",
                    selected = false,
                    onClick = {
                        val cal = Calendar.getInstance()
                        current?.let { cal.timeInMillis = it }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                cal.set(Calendar.YEAR, y)
                                cal.set(Calendar.MONTH, m)
                                cal.set(Calendar.DAY_OF_MONTH, d)
                                TimePickerDialog(
                                    context,
                                    { _, h, min ->
                                        cal.set(Calendar.HOUR_OF_DAY, h)
                                        cal.set(Calendar.MINUTE, min)
                                        cal.set(Calendar.SECOND, 0)
                                        cal.set(Calendar.MILLISECOND, 0)
                                        onChange(cal.timeInMillis)
                                    },
                                    cal.get(Calendar.HOUR_OF_DAY),
                                    cal.get(Calendar.MINUTE),
                                    false
                                ).show()
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                )
            }
        }
        if (current != null) {
            Text(
                formatDeadline(current),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Ink
            )
        }
    }
}

private fun sameDayAsPreset(a: Long, b: Long): Boolean {
    val ca = Calendar.getInstance().apply { timeInMillis = a }
    val cb = Calendar.getInstance().apply { timeInMillis = b }
    return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
            ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR) &&
            ca.get(Calendar.HOUR_OF_DAY) == cb.get(Calendar.HOUR_OF_DAY) &&
            ca.get(Calendar.MINUTE) == cb.get(Calendar.MINUTE)
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Ink,
    unfocusedBorderColor = Line,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    cursorColor = Ink,
)
