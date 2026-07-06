package com.miraj.tasktracker.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

fun endOfDay(daysFromNow: Int): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, daysFromNow)
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

fun formatDeadline(epochMs: Long?): String {
    if (epochMs == null) return "No deadline"
    val cal = Calendar.getInstance().apply { timeInMillis = epochMs }
    val now = Calendar.getInstance()
    val tomorrow = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
    val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(epochMs))
    return when {
        sameDay(cal, now) -> "Today, $time"
        sameDay(cal, tomorrow) -> "Tomorrow, $time"
        else -> SimpleDateFormat("EEE, MMM d, h:mm a", Locale.getDefault()).format(Date(epochMs))
    }
}

fun formatRelativeToDeadline(deadlineEpochMs: Long): String {
    val now = System.currentTimeMillis()
    val diffMs = deadlineEpochMs - now
    val past = diffMs < 0
    val absMs = abs(diffMs)
    val mins = absMs / 60_000L
    val hours = mins / 60
    val days = hours / 24
    val label = when {
        days >= 2 -> "$days days"
        days == 1L -> "1 day"
        hours >= 2 -> "$hours hours"
        hours == 1L -> "1 hour"
        mins >= 2 -> "$mins mins"
        else -> "moments"
    }
    return if (past) "$label overdue" else "in $label"
}

fun formatTimestamp(epochMs: Long): String =
    SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(epochMs))

fun taskState(deadlineEpochMs: Long?, completed: Boolean): String {
    if (completed) return "done"
    if (deadlineEpochMs == null) return "someday"
    val now = Calendar.getInstance()
    val d = Calendar.getInstance().apply { timeInMillis = deadlineEpochMs }
    if (sameDay(d, now)) return "today"
    return if (deadlineEpochMs < System.currentTimeMillis()) "overdue" else "upcoming"
}

private fun sameDay(a: Calendar, b: Calendar): Boolean =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
    a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
