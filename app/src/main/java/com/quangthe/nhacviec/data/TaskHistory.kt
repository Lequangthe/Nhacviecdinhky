// SPDX-FileCopyrightText: 2026 Kapoué
// SPDX-License-Identifier: GPL-3.0-or-later

package com.quangthe.nhacviec.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_history")
data class TaskHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskId: Int,
    val doneAt: Long
)
