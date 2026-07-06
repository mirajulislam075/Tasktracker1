package com.miraj.tasktracker.data.repository

import android.content.Context
import com.miraj.tasktracker.data.db.AppDatabase
import com.miraj.tasktracker.data.model.Assignor
import com.miraj.tasktracker.data.model.Reminder
import com.miraj.tasktracker.data.model.ReminderPreset
import com.miraj.tasktracker.data.model.StatusUpdate
import com.miraj.tasktracker.data.model.Task
import com.miraj.tasktracker.data.model.TaskStatus
import com.miraj.tasktracker.notification.ReminderScheduler
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val context: Context) {
    private val db = AppDatabase.get(context)
    private val taskDao = db.taskDao()
    private val assignorDao = db.assignorDao()
    private val reminderDao = db.reminderDao()
    private val statusUpdateDao = db.statusUpdateDao()
    private val scheduler = ReminderScheduler(context)

    // Tasks
    fun observeTasks(): Flow<List<Task>> = taskDao.observeAll()
    fun observeTask(id: Long): Flow<Task?> = taskDao.observeById(id)
    suspend fun getTask(id: Long): Task? = taskDao.getById(id)
    suspend fun getOverdueTasks(now: Long = System.currentTimeMillis()): List<Task> =
        taskDao.getOverdue(now)

    // Assignors
    fun observeActiveAssignors(): Flow<List<Assignor>> = assignorDao.observeActive()
    fun observeAllAssignors(): Flow<List<Assignor>> = assignorDao.observeAll()
    suspend fun archiveAssignor(id: Long, archived: Boolean) = assignorDao.setArchived(id, archived)
    suspend fun deleteAssignor(id: Long) = assignorDao.delete(id)
    suspend fun renameAssignor(a: Assignor, newName: String) =
        assignorDao.update(a.copy(name = newName))

    // Reminders
    fun observeRemindersForTask(taskId: Long): Flow<List<Reminder>> =
        reminderDao.observeForTask(taskId)
    suspend fun getRemindersForTaskOnce(taskId: Long): List<Reminder> =
        reminderDao.getForTask(taskId)

    // Status updates
    fun observeStatusUpdates(taskId: Long): Flow<List<StatusUpdate>> =
        statusUpdateDao.observeForTask(taskId)

    /**
     * Save or update a task along with its reminder set. Existing reminders are
     * cleared and re-created (and re-scheduled with the OS AlarmManager).
     */
    suspend fun saveTask(task: Task, presets: List<ReminderPreset>): Long {
        val id = if (task.id == 0L) taskDao.insert(task) else {
            taskDao.update(task); task.id
        }
        val saved = task.copy(id = id)

        // Register assignor
        if (saved.assignor.isNotBlank()) {
            if (assignorDao.findByName(saved.assignor) == null) {
                assignorDao.insert(Assignor(name = saved.assignor, useCount = 1))
            } else {
                assignorDao.bumpUse(saved.assignor)
            }
        }

        // Clear old reminders (and their alarms) and set new ones
        val oldReminders = reminderDao.getForTask(id)
        oldReminders.forEach { scheduler.cancel(it.id) }
        reminderDao.deleteForTask(id)

        val deadline = saved.deadlineEpochMs
        if (deadline != null && !saved.completed) {
            presets.forEach { p ->
                val fireAt = deadline - p.minutesBefore * 60_000L
                if (fireAt > System.currentTimeMillis() - 60_000L) {
                    val reminderId = reminderDao.insert(
                        Reminder(
                            taskId = id,
                            minutesBefore = p.minutesBefore,
                            label = p.label,
                            fireAtEpochMs = fireAt
                        )
                    )
                    scheduler.schedule(reminderId, fireAt)
                }
            }
        }
        return id
    }

    suspend fun setStatus(taskId: Long, status: TaskStatus, note: String, source: String) {
        val completedAt = if (status == TaskStatus.DONE) System.currentTimeMillis() else null
        taskDao.setStatus(taskId, status, completedAt)
        statusUpdateDao.insert(StatusUpdate(taskId = taskId, status = status, note = note, source = source))
        if (status == TaskStatus.DONE) {
            // Cancel any outstanding reminders
            reminderDao.getForTask(taskId).forEach { scheduler.cancel(it.id) }
        }
    }

    suspend fun deleteTask(taskId: Long) {
        reminderDao.getForTask(taskId).forEach { scheduler.cancel(it.id) }
        taskDao.delete(taskId)
    }

    suspend fun markReminderFired(reminderId: Long) = reminderDao.markFired(reminderId)
    suspend fun getReminder(id: Long) = reminderDao.getById(id)

    /** Re-schedule any un-fired reminders — call from boot receiver. */
    suspend fun rescheduleAllPending() {
        reminderDao.getAllPending().forEach { r ->
            if (r.fireAtEpochMs > System.currentTimeMillis()) {
                scheduler.schedule(r.id, r.fireAtEpochMs)
            }
        }
    }

    suspend fun setOverdueNudgeSent(taskId: Long) {
        taskDao.setLastOverdueNudge(taskId, System.currentTimeMillis())
    }
}
