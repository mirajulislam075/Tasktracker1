package com.miraj.tasktracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.miraj.tasktracker.TaskTrackerApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1)
        if (reminderId < 0) return

        val pending = goAsync()
        val repo = (context.applicationContext as TaskTrackerApp).repository

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminder = repo.getReminder(reminderId) ?: return@launch
                val task = repo.getTask(reminder.taskId) ?: return@launch
                if (task.completed) return@launch

                NotificationHelper.postTaskReminder(
                    ctx = context,
                    task = task,
                    reminderLabel = reminder.label
                )
                repo.markReminderFired(reminderId)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
    }
}
