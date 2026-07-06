package com.miraj.tasktracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.miraj.tasktracker.data.model.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE taskId = :taskId ORDER BY minutesBefore DESC")
    fun observeForTask(taskId: Long): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE taskId = :taskId ORDER BY minutesBefore DESC")
    suspend fun getForTask(taskId: Long): List<Reminder>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Long): Reminder?

    @Query("SELECT * FROM reminders WHERE fired = 0")
    suspend fun getAllPending(): List<Reminder>

    @Insert
    suspend fun insert(reminder: Reminder): Long

    @Query("UPDATE reminders SET fired = 1 WHERE id = :id")
    suspend fun markFired(id: Long)

    @Query("DELETE FROM reminders WHERE taskId = :taskId")
    suspend fun deleteForTask(taskId: Long)
}
