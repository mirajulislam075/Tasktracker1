package com.miraj.tasktracker.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [ForeignKey(
        entity = Task::class,
        parentColumns = ["id"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("taskId")]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val minutesBefore: Long,
    val label: String,
    val fireAtEpochMs: Long,
    val fired: Boolean = false
)
