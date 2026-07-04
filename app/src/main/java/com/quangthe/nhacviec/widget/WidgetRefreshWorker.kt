// SPDX-FileCopyrightText: 2026 Kapoué
// SPDX-License-Identifier: GPL-3.0-or-later

package com.quangthe.nhacviec.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.quangthe.nhacviec.notification.MidnightScheduler

class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            NhacviecWidget().updateAll(applicationContext)
        } catch (e: Exception) {
            android.util.Log.e("MainTask", "Midnight widget refresh failed", e)
        }
        MidnightScheduler.schedule(applicationContext)
        return Result.success()
    }
}
