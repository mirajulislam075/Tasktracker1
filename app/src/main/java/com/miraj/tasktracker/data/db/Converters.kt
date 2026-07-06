package com.miraj.tasktracker.data.db

import androidx.room.TypeConverter
import com.miraj.tasktracker.data.model.Category
import com.miraj.tasktracker.data.model.Priority
import com.miraj.tasktracker.data.model.TaskStatus

class Converters {
    @TypeConverter fun categoryToString(c: Category): String = c.name
    @TypeConverter fun stringToCategory(s: String): Category = Category.fromName(s)

    @TypeConverter fun priorityToString(p: Priority): String = p.name
    @TypeConverter fun stringToPriority(s: String): Priority = Priority.fromName(s)

    @TypeConverter fun statusToString(s: TaskStatus): String = s.name
    @TypeConverter fun stringToStatus(s: String): TaskStatus = TaskStatus.fromName(s)
}
