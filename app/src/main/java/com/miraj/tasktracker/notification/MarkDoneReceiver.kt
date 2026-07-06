package com.miraj.tasktracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.miraj.tasktracker.TaskTrackerApp
import com.miraj.tasktracker.data.model.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MarkDoneReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1)
        if (taskId < 0) return

        val pending = goAsync()
        val repo = (context.applicationContext as TaskTrackerApp).repository

        CoroutineScope(Dispatchers.IO).launch {
            try {
                repo.setStatus(taskId, TaskStatus.DONE, "Marked done from notification", "notification")
                NotificationHelper.cancel(context, taskId)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "task_id"
    }
}
