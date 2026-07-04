// SPDX-FileCopyrightText: 2026 Kapoué
// SPDX-License-Identifier: GPL-3.0-or-later

package com.quangthe.nhacviec.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.quangthe.nhacviec.MainActivity

class OpenTaskAction : ActionCallback {

    companion object {
        val KEY_TASK_ID = ActionParameters.Key<Int>("task_id")
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val taskId = parameters[KEY_TASK_ID] ?: return
        context.startActivity(
            Intent(context, MainActivity::class.java).apply {
                putExtra("task_id", taskId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}
