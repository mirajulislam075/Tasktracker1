package com.miraj.tasktracker.data.model

enum class Category(val label: String) {
    WORK("Work"),
    AUXILIARY("Auxiliary"),
    PERSONAL("Personal");

    companion object {
        fun fromName(name: String): Category = entries.firstOrNull { it.name == name } ?: PERSONAL
    }
}

enum class Priority(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High");

    companion object {
        fun fromName(name: String): Priority = entries.firstOrNull { it.name == name } ?: MEDIUM
    }
}

enum class TaskStatus(val label: String) {
    NOT_STARTED("Not started"),
    IN_PROGRESS("In progress"),
    BLOCKED("Blocked"),
    DEFERRED("Deferred"),
    DONE("Done");

    companion object {
        fun fromName(name: String): TaskStatus = entries.firstOrNull { it.name == name } ?: NOT_STARTED
    }
}

/**
 * A reminder offset expressed in minutes before the deadline.
 * Negative values are not used; 0 means "at deadline".
 */
data class ReminderPreset(val label: String, val minutesBefore: Long) {
    companion object {
        val PRESETS = listOf(
            ReminderPreset("1 week before", 7 * 24 * 60L),
            ReminderPreset("3 days before", 3 * 24 * 60L),
            ReminderPreset("1 day before", 24 * 60L),
            ReminderPreset("3 hours before", 3 * 60L),
            ReminderPreset("1 hour before", 60L),
            ReminderPreset("At deadline", 0L),
        )
    }
}

data class DeadlinePreset(val label: String, val daysFromNow: Int) {
    companion object {
        val PRESETS = listOf(
            DeadlinePreset("End of today", 0),
            DeadlinePreset("End of tomorrow", 1),
            DeadlinePreset("In 3 days", 3),
            DeadlinePreset("In 7 days", 7),
            DeadlinePreset("In 2 weeks", 14),
        )
    }
}
