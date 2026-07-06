package com.miraj.tasktracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.miraj.tasktracker.data.model.Task
import com.miraj.tasktracker.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY completedEpochMs IS NOT NULL, deadlineEpochMs IS NULL, deadlineEpochMs ASC")
    fun observeAll(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observeById(id: Long): Flow<Task?>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE status != 'DONE' AND deadlineEpochMs IS NOT NULL AND deadlineEpochMs < :now")
    suspend fun getOverdue(now: Long): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Query("UPDATE tasks SET status = :status, completedEpochMs = :completedAt WHERE id = :id")
    suspend fun setStatus(id: Long, status: TaskStatus, completedAt: Long?)

    @Query("UPDATE tasks SET lastOverdueNudgeEpochMs = :ts WHERE id = :id")
    suspend fun setLastOverdueNudge(id: Long, ts: Long)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: Long)
}
