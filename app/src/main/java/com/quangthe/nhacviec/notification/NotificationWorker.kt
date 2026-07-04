// SPDX-FileCopyrightText: 2026 Kapoué
// SPDX-License-Identifier: GPL-3.0-or-later

package com.quangthe.nhacviec.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val taskId    = inputData.getInt(KEY_TASK_ID, -1)
        val taskTitle = inputData.getString(KEY_TASK_TITLE) ?: return Result.failure()
        val type      = inputData.getString(KEY_TYPE) ?: TYPE_J

        val body = when (type) {
            TYPE_J3 -> applicationContext.getString(com.quangthe.nhacviec.R.string.notification_body_3days)
            TYPE_J1 -> applicationContext.getString(com.quangthe.nhacviec.R.string.notification_body_tomorrow)
            else    -> applicationContext.getString(com.quangthe.nhacviec.R.string.notification_body_today)
        }
        NotificationHelper.showNotification(applicationContext, taskId, taskTitle, body)
        return Result.success()
    }

    companion object {
        const val KEY_TASK_ID    = "task_id"
        const val KEY_TASK_TITLE = "task_title"
        const val KEY_TYPE       = "type"
        const val TYPE_J         = "j"
        const val TYPE_J3        = "j3"
        const val TYPE_J1        = "j1"
    }
}
