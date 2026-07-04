// SPDX-FileCopyrightText: 2026 Kapoué
// SPDX-License-Identifier: GPL-3.0-or-later

package com.quangthe.nhacviec.notification

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.quangthe.nhacviec.data.Task
import com.quangthe.nhacviec.data.effectiveDueAt
import com.quangthe.nhacviec.data.isOneShotDone
import com.quangthe.nhacviec.data.isSnoozed
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun scheduleForTask(context: Context, task: Task) {
        cancelForTask(context, task.id)
        if (task.isDisabled) return
        if (task.isOneShotDone) return
        if (task.recurrenceType == "ONE_SHOT" && task.targetDate == 0L) return

        val wm       = WorkManager.getInstance(context)
        val hour     = NotificationPreference.getHour(context)
        val dueMillis = task.effectiveDueAt

        // Notification Jour J
        val delayJ = delayUntilHour(dueMillis, hour)
        if (delayJ > 0) {
            wm.enqueueUniqueWork(
                "maintask_j_${task.id}",
                ExistingWorkPolicy.REPLACE,
                buildRequest(task, NotificationWorker.TYPE_J, delayJ)
            )
        }

        // Notification J-advance (pas pour ONE_SHOT, ni pour une tâche reportée à une date :
        // « reporté à X » = préviens-moi le jour X, pas en avance)
        if (task.recurrenceType != "ONE_SHOT" && !task.isSnoozed) {
            val advanceDays = if (NotificationPreference.getAdvance(context) == "J1") 1 else 3
            val advType     = if (advanceDays == 1) NotificationWorker.TYPE_J1 else NotificationWorker.TYPE_J3
            val delayAdv    = delayUntilHour(dueMillis - advanceDays * 86_400_000L, hour)
            if (delayAdv > 0) {
                wm.enqueueUniqueWork(
                    "maintask_jadv_${task.id}",
                    ExistingWorkPolicy.REPLACE,
                    buildRequest(task, advType, delayAdv)
                )
            }
        }
    }

    fun cancelForTask(context: Context, taskId: Int) {
        val wm = WorkManager.getInstance(context)
        wm.cancelUniqueWork("maintask_j_$taskId")
        wm.cancelUniqueWork("maintask_j3_$taskId")   // ancienne clé — migration
        wm.cancelUniqueWork("maintask_jadv_$taskId")
    }

    private fun buildRequest(task: Task, type: String, delayMs: Long) =
        OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(
                Data.Builder()
                    .putInt(NotificationWorker.KEY_TASK_ID, task.id)
                    .putString(NotificationWorker.KEY_TASK_TITLE, task.title)
                    .putString(NotificationWorker.KEY_TYPE, type)
                    .build()
            )
            .build()

    private fun delayUntilHour(targetDayMillis: Long, hour: Int): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = targetDayMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis - System.currentTimeMillis()
    }
}
