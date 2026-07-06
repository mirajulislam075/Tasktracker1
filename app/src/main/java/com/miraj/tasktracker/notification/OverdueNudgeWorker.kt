package com.miraj.tasktracker.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.miraj.tasktracker.TaskTrackerApp
import java.util.concurrent.TimeUnit

class OverdueNudgeWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repo = (applicationContext as TaskTrackerApp).repository
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val overdue = repo.getOverdueTasks(now)
        overdue.forEach { task ->
            val last = task.lastOverdueNudgeEpochMs ?: 0L
            if (now - last >= oneDayMs) {
                NotificationHelper.postTaskReminder(
                    ctx = applicationContext,
                    task = task,
                    reminderLabel = "Overdue",
                    isOverdue = true
                )
                repo.setOverdueNudgeSent(task.id)
            }
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "overdue_nudge"

        fun schedule(context: Context) {
            val req = PeriodicWorkRequestBuilder<OverdueNudgeWorker>(
                6, TimeUnit.HOURS
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                req
            )
        }
    }
}
