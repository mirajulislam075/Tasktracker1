package com.miraj.tasktracker.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.miraj.tasktracker.MainActivity
import com.miraj.tasktracker.R
import com.miraj.tasktracker.data.model.Task
import com.miraj.tasktracker.ui.statusupdate.StatusUpdateActivity
import com.miraj.tasktracker.util.formatRelativeToDeadline

object NotificationHelper {
    const val CHANNEL_ID = "reminders"

    fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = ctx.getString(R.string.notif_channel_reminders)
            val desc = ctx.getString(R.string.notif_channel_reminders_desc)
            val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH).apply {
                description = desc
                enableVibration(true)
                setShowBadge(true)
            }
            ctx.getSystemService<NotificationManager>()?.createNotificationChannel(channel)
        }
    }

    /**
     * Post a reminder notification with "Mark done" and "Update status" actions.
     * The notification tag is the task id so multiple reminders for the same task
     * replace each other.
     */
    fun postTaskReminder(
        ctx: Context,
        task: Task,
        reminderLabel: String,
        isOverdue: Boolean = false
    ) {
        ensureChannel(ctx)

        val notifId = task.id.toInt()

        // Tap the body → open the app to the task detail
        val openIntent = Intent(ctx, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(MainActivity.EXTRA_OPEN_TASK_ID, task.id)
        }
        val openPI = PendingIntent.getActivity(
            ctx, notifId, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Mark done" — silent broadcast
        val doneIntent = Intent(ctx, MarkDoneReceiver::class.java).apply {
            putExtra(MarkDoneReceiver.EXTRA_TASK_ID, task.id)
        }
        val donePI = PendingIntent.getBroadcast(
            ctx, notifId * 10 + 1, doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Update status" — launch transparent activity with sheet
        val updateIntent = Intent(ctx, StatusUpdateActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(StatusUpdateActivity.EXTRA_TASK_ID, task.id)
        }
        val updatePI = PendingIntent.getActivity(
            ctx, notifId * 10 + 2, updateIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isOverdue) "Overdue: ${task.title}" else task.title
        val subText = task.category.label + " • " + task.assignor.ifBlank { "unassigned" }
        val body = when {
            isOverdue -> "Deadline passed. Update status or mark done."
            task.deadlineEpochMs != null -> "$reminderLabel • ${formatRelativeToDeadline(task.deadlineEpochMs)}"
            else -> reminderLabel
        }

        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setSubText(subText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openPI)
            .setAutoCancel(true)
            .addAction(0, "Mark done", donePI)
            .addAction(0, "Update status", updatePI)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        val mgr = ctx.getSystemService<NotificationManager>() ?: return
        mgr.notify(notifId, builder.build())
    }

    fun cancel(ctx: Context, taskId: Long) {
        ctx.getSystemService<NotificationManager>()?.cancel(taskId.toInt())
    }
}
