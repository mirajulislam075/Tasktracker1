package com.miraj.tasktracker.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "status_updates",
    foreignKeys = [ForeignKey(
        entity = Task::class,
        parentColumns = ["id"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("taskId")]
)
data class StatusUpdate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val status: TaskStatus,
    val note: String = "",
    val epochMs: Long = System.currentTimeMillis(),
    val source: String = "manual" // "manual", "reminder", "notification"
)
