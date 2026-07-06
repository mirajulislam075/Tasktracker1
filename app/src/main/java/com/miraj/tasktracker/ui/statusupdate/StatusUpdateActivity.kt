package com.miraj.tasktracker.ui.statusupdate

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.miraj.tasktracker.TaskTrackerApp
import com.miraj.tasktracker.data.model.Task
import com.miraj.tasktracker.data.model.TaskStatus
import com.miraj.tasktracker.ui.detail.StatusUpdateDialog
import com.miraj.tasktracker.ui.theme.TaskTrackerTheme
import kotlinx.coroutines.launch

/**
 * Launched from the notification "Update status" action. Renders as a transparent
 * activity so the dialog appears over whatever the user was doing.
 */
class StatusUpdateActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
        val repo = (applicationContext as TaskTrackerApp).repository

        setContent {
            TaskTrackerTheme {
                var task by remember { mutableStateOf<Task?>(null) }
                LaunchedEffect(taskId) { task = repo.getTask(taskId) }
                val t = task
                if (t != null) {
                    StatusUpdateDialog(
                        currentStatus = t.status,
                        onDismiss = { finish() },
                        onSubmit = { s, n ->
                            lifecycleScope.launch {
                                repo.setStatus(t.id, s, n, "notification")
                                // If the reminder notification is still up, clear it
                                if (s == TaskStatus.DONE) {
                                    val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                    mgr.cancel(t.id.toInt())
                                }
                                finish()
                            }
                        }
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "task_id"
    }
}
