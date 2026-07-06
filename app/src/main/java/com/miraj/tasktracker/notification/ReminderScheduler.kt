package com.miraj.tasktracker.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.getSystemService

class ReminderScheduler(private val context: Context) {

    private fun pendingIntent(reminderId: Long, flags: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminderId)
        }
        return PendingIntent.getBroadcast(context, reminderId.toInt(), intent, flags)
    }

    fun schedule(reminderId: Long, fireAtEpochMs: Long) {
        val am = context.getSystemService<AlarmManager>() ?: return
        val pi = pendingIntent(
            reminderId,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                // Fallback: inexact but still fires while idle
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAtEpochMs, pi)
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAtEpochMs, pi)
            }
        } catch (se: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAtEpochMs, pi)
        }
    }

    fun cancel(reminderId: Long) {
        val am = context.getSystemService<AlarmManager>() ?: return
        val pi = pendingIntent(reminderId, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        pi?.let { am.cancel(it); it.cancel() }
    }
}
