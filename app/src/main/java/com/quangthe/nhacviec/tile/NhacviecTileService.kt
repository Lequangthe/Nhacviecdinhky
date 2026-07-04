// SPDX-FileCopyrightText: 2026 Kapoué
// SPDX-License-Identifier: GPL-3.0-or-later

package com.quangthe.nhacviec.tile

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.quangthe.nhacviec.MainActivity
import com.quangthe.nhacviec.data.TaskDatabase
import com.quangthe.nhacviec.data.daysRemaining
import com.quangthe.nhacviec.data.isOneShotDone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NhacviecTileService : TileService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onStartListening() {
        updateTile()
    }

    override fun onClick() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val pi = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            startActivityAndCollapse(pi)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun updateTile() {
        scope.launch {
            val overdueCount = withContext(Dispatchers.IO) {
                TaskDatabase.getInstance(this@NhacviecTileService)
                    .taskDao().getAllTasksOnce()
                    .count { task ->
                        !task.isDisabled && !task.isOneShotDone &&
                        !(task.recurrenceType == "ONE_SHOT" && task.targetDate == 0L) &&
                        task.daysRemaining < 0
                    }
            }
            val tile = qsTile ?: return@launch
            tile.state = if (overdueCount > 0) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.label = getString(com.quangthe.nhacviec.R.string.app_name)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = if (overdueCount > 0)
                    getString(com.quangthe.nhacviec.R.string.tile_overdue, overdueCount)
                else
                    getString(com.quangthe.nhacviec.R.string.tile_up_to_date)
            }
            tile.updateTile()
        }
    }
}
