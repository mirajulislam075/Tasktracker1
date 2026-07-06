package com.miraj.tasktracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.miraj.tasktracker.data.model.Assignor
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignorDao {
    @Query("SELECT * FROM assignors WHERE archived = 0 ORDER BY useCount DESC, name ASC")
    fun observeActive(): Flow<List<Assignor>>

    @Query("SELECT * FROM assignors ORDER BY name ASC")
    fun observeAll(): Flow<List<Assignor>>

    @Query("SELECT * FROM assignors WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): Assignor?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(assignor: Assignor): Long

    @Update
    suspend fun update(assignor: Assignor)

    @Query("UPDATE assignors SET useCount = useCount + 1 WHERE name = :name")
    suspend fun bumpUse(name: String)

    @Query("UPDATE assignors SET archived = :archived WHERE id = :id")
    suspend fun setArchived(id: Long, archived: Boolean)

    @Query("DELETE FROM assignors WHERE id = :id")
    suspend fun delete(id: Long)
}
