package com.miraj.tasktracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: Category,
    val assignor: String,
    val priority: Priority,
    val deadlineEpochMs: Long?,
    val notes: String = "",
    val status: TaskStatus = TaskStatus.NOT_STARTED,
    val createdEpochMs: Long = System.currentTimeMillis(),
    val completedEpochMs: Long? = null,
    val lastOverdueNudgeEpochMs: Long? = null
) {
    val completed: Boolean get() = status == TaskStatus.DONE
}
