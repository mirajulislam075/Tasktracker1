package com.miraj.tasktracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.miraj.tasktracker.data.model.Assignor
import com.miraj.tasktracker.data.model.Reminder
import com.miraj.tasktracker.data.model.StatusUpdate
import com.miraj.tasktracker.data.model.Task

@Database(
    entities = [Task::class, Assignor::class, Reminder::class, StatusUpdate::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun assignorDao(): AssignorDao
    abstract fun reminderDao(): ReminderDao
    abstract fun statusUpdateDao(): StatusUpdateDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "task-tracker.db"
            ).build().also { INSTANCE = it }
        }
    }
}
