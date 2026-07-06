package com.miraj.tasktracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.miraj.tasktracker.TaskTrackerApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        val pending = goAsync()
        val repo = (context.applicationContext as TaskTrackerApp).repository

        CoroutineScope(Dispatchers.IO).launch {
            try {
                repo.rescheduleAllPending()
            } finally {
                pending.finish()
            }
        }
    }
}
