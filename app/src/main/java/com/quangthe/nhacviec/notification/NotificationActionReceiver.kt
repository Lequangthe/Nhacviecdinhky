// SPDX-FileCopyrightText: 2026 Kapoué
// SPDX-License-Identifier: GPL-3.0-or-later

package com.quangthe.nhacviec.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import com.quangthe.nhacviec.data.TaskDatabase
import com.quangthe.nhacviec.data.TaskHistory
import com.quangthe.nhacviec.widget.NhacviecWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Gère les boutons "Fait" / "Reporter +1j" des notifications.
 * Reproduit la logique de TaskViewModel.markDone / snooze hors de l'UI.
 */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
        if (taskId == -1) return
        val action = intent.action ?: return

        // Retire la notification affichée
        context.getSystemService(NotificationManager::class.java)?.cancel(taskId)

        val pending    = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db   = TaskDatabase.getInstance(appContext)
                val task = db.taskDao().getById(taskId) ?: return@launch
                val now  = System.currentTimeMillis()

                when (action) {
                    ACTION_DONE -> {
                        val done = task.copy(lastDoneAt = now, snoozedUntil = 0L)
                        db.taskDao().update(done)
                        if (task.recurrenceType != "ONE_SHOT") {
                            db.taskHistoryDao().insert(TaskHistory(taskId = task.id, doneAt = now))
                            db.taskHistoryDao().trimForTask(task.id, 6)
                            NotificationScheduler.scheduleForTask(appContext, done)
                        } else {
                            NotificationScheduler.cancelForTask(appContext, task.id)
                        }
                    }
                    ACTION_SNOOZE -> {
                        val snoozed = task.copy(snoozedUntil = now + 86_400_000L)
                        db.taskDao().update(snoozed)
                        NotificationScheduler.scheduleForTask(appContext, snoozed)
                    }
                }

                try { NhacviecWidget().updateAll(appContext) } catch (_: Exception) {}
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_DONE   = "com.quangthe.nhacviec.action.DONE"
        const val ACTION_SNOOZE = "com.quangthe.nhacviec.action.SNOOZE"
        const val EXTRA_TASK_ID = "task_id"
    }
}
