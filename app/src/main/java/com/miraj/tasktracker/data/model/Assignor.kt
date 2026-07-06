package com.miraj.tasktracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assignors")
data class Assignor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val useCount: Int = 0,
    val archived: Boolean = false
)
