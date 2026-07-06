package com.miraj.tasktracker

import android.app.Application
import com.miraj.tasktracker.data.repository.TaskRepository
import com.miraj.tasktracker.notification.NotificationHelper
import com.miraj.tasktracker.notification.OverdueNudgeWorker

class TaskTrackerApp : Application() {
    lateinit var repository: TaskRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = TaskRepository(this)
        NotificationHelper.ensureChannel(this)
        OverdueNudgeWorker.schedule(this)
    }
}
