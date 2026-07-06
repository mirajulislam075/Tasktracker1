package com.miraj.tasktracker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miraj.tasktracker.data.model.Assignor
import com.miraj.tasktracker.ui.theme.Ink
import com.miraj.tasktracker.ui.theme.InkSoft
import com.miraj.tasktracker.ui.theme.Line
import com.miraj.tasktracker.ui.theme.LineSoft
import com.miraj.tasktracker.ui.theme.Paper

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val assignors by viewModel.assignors.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Assignor?>(null) }
    var deleting by remember { mutableStateOf<Assignor?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper)
            )
        },
        containerColor = Paper
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Text(
                "Assignors".uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = InkSoft,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            Text(
                "People or roles you assign tasks to. Names are saved automatically and autocomplete on the new-task screen.",
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft,
                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
            )
            if (assignors.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("—", style = MaterialTheme.typography.headlineMedium, color = Line)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "No assignors yet. Add one when you create a task.",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSoft
                    )
                }
            } else {
                LazyColumn {
                    items(assignors, key = { it.id }) { a ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    a.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (a.archived) InkSoft else Ink,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "used ${a.useCount}x${if (a.archived) " · archived" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkSoft
                                )
                            }
                            IconButton(onClick = { editing = a }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Rename", tint = InkSoft)
                            }
                            IconButton(onClick = { deleting = a }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = InkSoft)
                            }
                        }
                        androidx.compose.foundation.layout.Box(
                            Modifier.fillMaxWidth().height(1.dp).background(LineSoft)
                        )
                    }
                }
            }
        }
    }

    editing?.let { a ->
        var name by remember(a.id) { mutableStateOf(a.name) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Rename assignor") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(3.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Ink,
                        unfocusedBorderColor = Line,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = Ink,
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.rename(a, name); editing = null }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editing = null }) { Text("Cancel") }
            },
            containerColor = Color.White
        )
    }

    deleting?.let { a ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete \"${a.name}\"?") },
            text = { Text("Existing tasks that used this name will keep the text; only the saved list entry is removed.") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(a); deleting = null }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text("Cancel") }
            },
            containerColor = Color.White
        )
    }
}
