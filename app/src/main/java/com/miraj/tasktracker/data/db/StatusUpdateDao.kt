package com.miraj.tasktracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.miraj.tasktracker.data.model.StatusUpdate
import kotlinx.coroutines.flow.Flow

@Dao
interface StatusUpdateDao {
    @Query("SELECT * FROM status_updates WHERE taskId = :taskId ORDER BY epochMs DESC")
    fun observeForTask(taskId: Long): Flow<List<StatusUpdate>>

    @Insert
    suspend fun insert(update: StatusUpdate): Long
}
